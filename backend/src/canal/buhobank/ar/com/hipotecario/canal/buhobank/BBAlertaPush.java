package ar.com.hipotecario.canal.buhobank;

import java.util.ArrayList;
import java.util.List;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.SendResponse;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.servicio.api.notificaciones.ApiNotificaciones;
import ar.com.hipotecario.backend.servicio.api.notificaciones.EnvioEmail;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.buhobank.UsuariosPushBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.UsuariosPushBuhobank.UsuarioPushBuhobank;
import ar.com.hipotecario.backend.servicio.sql.esales.AlertaPushUsuariosEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.AlertaPushUsuariosEsales.AlertaPushUsuarioEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.AlertasPushEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.AlertasPushEsales.AlertaPushEsales;
import ar.com.hipotecario.canal.buhobank.firebase.Firebase;

public class BBAlertaPush extends Modulo {

	public static void ejecutarRemarketing(ContextoBB contexto) {
		logProceso(contexto, "inicio proceso de alertas push");
		
		SqlBuhoBank.ejecutarHistoricoPush(contexto, Fecha.ahora().restarDias(3)).tryGet();

		AlertasPushEsales alertasDescripcion = SqlEsales.obtenerAlertasPush(contexto).tryGet();
		guardarPushAbandonoBySesion(contexto, GeneralBB.COD_ALERTA_PRIMER_FLUJO_VU, alertasDescripcion);
		guardarPushAbandonoBySesion(contexto, GeneralBB.COD_ALERTA_POST_OFERTA_INICIA, alertasDescripcion);
		guardarPushAbandonoBySesion(contexto, GeneralBB.COD_ALERTA_POST_OFERTA_CREDITICIA, alertasDescripcion);

		enviarAlertasPush(contexto);
		enviarAlertasMail(contexto);
		
		logProceso(contexto, "finaliza proceso de alertas push");
	}

	private static void enviarAlertasMail(ContextoBB contexto) {
		AlertaPushUsuariosEsales alertasPush = SqlEsales.alertasMailPendientes(contexto).tryGet();
		if (alertasPush == null || alertasPush.size() == 0) {
			return;
		}

		AlertaPushUsuariosEsales alertasMailHabilitadas = new AlertaPushUsuariosEsales();
		List<String> cuilsUsuarios = new ArrayList<String>();
		for (int i = 0; i < alertasPush.size(); i++) {
			
			AlertaPushUsuarioEsales alerta = alertasPush.get(i);
			if (!cuilsUsuarios.contains(alerta.cuil)) {
				alertasMailHabilitadas.add(alerta);
				cuilsUsuarios.add(alerta.cuil);
			}
		}

		for (int i = 0; i < alertasMailHabilitadas.size(); i++) {

			AlertaPushUsuarioEsales alerta = alertasMailHabilitadas.get(i);

			String urlStore = GeneralBB.URL_STORE_DEFAULT;
			if (GeneralBB.PLATAFORMA_ANDROID.equals(alerta.plataforma)) {
				urlStore = GeneralBB.URL_STORE_ANDROID;
			}

			if (GeneralBB.PLATAFORMA_IOS.equals(alerta.plataforma)) {
				urlStore = GeneralBB.URL_STORE_IOS;
			}

			String template = EnvioEmail.plantilla(contexto, alerta.plantillaMail);
			if (empty(alerta.plantillaMail) || empty(template)) {
				SqlEsales.actualizarEstadoAlerta(contexto, alerta.id, GeneralBB.ESTADO_ENVIO_ALERTA_ERROR, GeneralBB.TIPO_ALERTA_MAIL).tryGet();
			} else {

				EnvioEmail envioMail = ApiNotificaciones.envioEmailBB(contexto, alerta.plantillaMail, alerta.asuntoMail, alerta.mail, urlStore).tryGet();
				if (envioMail == null) {
					SqlEsales.actualizarEstadoAlerta(contexto, alerta.id, GeneralBB.ESTADO_ENVIO_ALERTA_ERROR, GeneralBB.TIPO_ALERTA_MAIL).tryGet();
				} else {
					SqlEsales.actualizarEstadoAlerta(contexto, alerta.id, GeneralBB.ESTADO_ENVIO_ALERTA_OK, GeneralBB.TIPO_ALERTA_MAIL).tryGet();
				}
			}

		}
	}

	public static void guardarPushAbandonoBySesion(ContextoBB contexto, String codigoAlerta, AlertasPushEsales alertasDescripcion) {
		AlertaPushEsales notificacion = alertasDescripcion.buscarCodigo(codigoAlerta);
		if (notificacion == null) {
			return;
		}

		UsuariosPushBuhobank abandonos = SqlEsales.obtenerAbandonos(contexto, notificacion).tryGet();
		guardarAbandonos(contexto, abandonos, notificacion, alertasDescripcion);
	}

	public static void guardarAbandonos(ContextoBB contexto, UsuariosPushBuhobank abandonos, AlertaPushEsales notificacion, AlertasPushEsales alertasDescripcion) {

		if (abandonos != null && abandonos.size() > 0) {
			for (UsuarioPushBuhobank abandono : abandonos) {

				if (empty(abandono.tokenFirebase) && notificacion.pushHabilitado) {
					AlertaPushUsuarioEsales ultimoToken = SqlEsales.obtenerUltimoToken(contexto, abandono.cuil).tryGet();
					if (ultimoToken != null) {
						abandono.tokenFirebase = ultimoToken.tokenFirebase;

						if (empty(abandono.mail)) {
							abandono.mail = ultimoToken.mail;
						}
					}
				}

				if (empty(abandono.mail) && notificacion.mailHabilitado) {
					AlertaPushUsuarioEsales ultimoMail = SqlEsales.obtenerUltimoMail(contexto, abandono.cuil).tryGet();
					if (ultimoMail != null) {
						abandono.mail = ultimoMail.mail;
					}
				}

				Boolean guardarToken = false;
				Boolean guardarMail = false;

				if (notificacion.pushHabilitado && !empty(abandono.tokenFirebase)) {
					AlertaPushUsuarioEsales alertaPush = SqlEsales.obtenerAlertaPushUsuarioByToken(contexto, notificacion.codigoAlerta, abandono.tokenFirebase).tryGet();
					if (alertaPush == null) {
						guardarToken = true;
					}
				}

				if (notificacion.mailHabilitado && !empty(abandono.mail) && !empty(notificacion.plantillaMail) && !empty(notificacion.asuntoMail)) {
					AlertaPushUsuarioEsales alertaMail = SqlEsales.obtenerAlertaPushUsuarioByMail(contexto, notificacion.codigoAlerta, abandono.mail).tryGet();
					if (alertaMail == null) {
						guardarMail = true;
					}
				}

				if (guardarToken || guardarMail) {
					SqlEsales.guardarNuevaAlertaPush(contexto, notificacion.codigoAlerta, guardarToken ? abandono.tokenFirebase : null, guardarMail ? abandono.mail : null, abandono.cuil, abandono.momento, notificacion.pushHabilitado ? null : GeneralBB.ESTADO_ENVIO_ALERTA_DESHABILITADO, notificacion.mailHabilitado ? null : GeneralBB.ESTADO_ENVIO_ALERTA_DESHABILITADO, !empty(abandono.plataforma) ? abandono.plataforma.toUpperCase() : "ANDROID").tryGet();
				}
			}
		}
	}

	public static void enviarAlertasPush(ContextoBB contexto) {
		AlertaPushUsuariosEsales alertasPush = SqlEsales.alertasPushPendientes(contexto).tryGet();
		if (alertasPush == null || alertasPush.size() == 0) {
			return;
		}
		
		AlertaPushUsuariosEsales alertasPushHabilitadas = new AlertaPushUsuariosEsales();
		List<String> cuilsUsuarios = new ArrayList<String>();
		for (int i = 0; i < alertasPush.size(); i++) {
			
			AlertaPushUsuarioEsales alerta = alertasPush.get(i);
			if (!cuilsUsuarios.contains(alerta.cuil)) {
				alertasPushHabilitadas.add(alerta);
				cuilsUsuarios.add(alerta.cuil);
			}
		}

		BatchResponse respuestaFirebase = enviarFirebase(contexto, alertasPushHabilitadas);
		guardarRespuestaFirebase(contexto, alertasPushHabilitadas, respuestaFirebase);
	}

	public static BatchResponse enviarFirebase(ContextoBB contexto, AlertaPushUsuariosEsales alertasPush) {

		if (alertasPush == null || alertasPush.size() == 0)
			return null;

		BatchResponse responseFirebase = null;

		Firebase firebase = new Firebase();

		try {
			responseFirebase = firebase.enviarNotificacionesPush(contexto, alertasPush);
		} catch (FirebaseMessagingException e) {
			e.printStackTrace();
		}

		return responseFirebase;
	}

	public static void guardarRespuestaFirebase(ContextoBB contexto, AlertaPushUsuariosEsales alertasPush, BatchResponse respuestaFirebase) {

		if (respuestaFirebase == null || alertasPush == null || alertasPush.size() == 0) {
			return;
		}

		List<SendResponse> respuestasEnvio = respuestaFirebase.getResponses();

		for (Integer i = 0; i < respuestasEnvio.size(); i++) {
			if (respuestasEnvio.get(i).isSuccessful()) {
				SqlEsales.actualizarEstadoAlerta(contexto, alertasPush.get(i).id, GeneralBB.ESTADO_ENVIO_ALERTA_OK, GeneralBB.TIPO_ALERTA_PUSH).tryGet();
			} else {
				SqlEsales.actualizarEstadoAlerta(contexto, alertasPush.get(i).id, GeneralBB.ESTADO_ENVIO_ALERTA_ERROR, GeneralBB.TIPO_ALERTA_PUSH).tryGet();
			}
		}
	}

	public static BatchResponse enviarPushIndividual(ContextoBB contexto, String tokenFirebase, String codigoAlerta) {
		AlertasPushEsales alertas = SqlEsales.obtenerAlertasPush(contexto).tryGet();
		AlertaPushEsales alerta = alertas.buscarCodigo(codigoAlerta);
		if (alerta == null) {
			return null;
		}

		List<String> tokensFirebase = new ArrayList<String>();
		tokensFirebase.add(tokenFirebase);

		String titulo = alerta.titulo;
		String texto = alerta.texto;
		String url = empty(alerta.url) ? "" : alerta.url;

		Firebase firebase = new Firebase();
		BatchResponse responseFirebase = null;
		try {
			responseFirebase = firebase.enviarNotificacionesPushIndividual(contexto, tokensFirebase, titulo, texto, null, url);
		} catch (FirebaseMessagingException e) {
			e.printStackTrace();
		}

		return responseFirebase;
	}
}
