package ar.com.hipotecario.canal.buhobank;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.formulario.ApiFormulario;
import ar.com.hipotecario.backend.servicio.api.notificaciones.ApiNotificaciones;
import ar.com.hipotecario.backend.servicio.api.notificaciones.EnvioEmail;
import ar.com.hipotecario.backend.servicio.api.notificaciones.EnvioSMS;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono;
import ar.com.hipotecario.backend.servicio.api.seguridad.ApiSeguridad;
import ar.com.hipotecario.backend.servicio.api.seguridad.OTP;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.util.Validadores;

public class BBValidacion extends Modulo {

	public static Boolean estadoOk(Objeto obj) {
		if (obj == null)
			return false;
		String estado = obj.string(CanalBuhoBank.ESTADO);
		if (empty(estado))
			return false;
		return "0".equals(estado);
	}

	public static Object enviarOtpMail(ContextoBB contexto) {
		
		String otpMail = generarOtp(contexto);

		if (contexto.esProduccion()) {			
			EnvioEmail envioMail = ApiNotificaciones.envioOtpEmailBB(contexto, contexto.sesion().mail, otpMail, contexto.sesion().nombre).tryGet();
			if (envioMail == null) {
				contexto.sesion().actualizarEstado("ERROR_ENVIAR_OTP_MAIL");
				LogBB.error(contexto, "ENVIO_MAIL");
				return respuesta("ENVIO_MAIL");
			}
		}
		
		SesionBB sesion = contexto.sesion();
		sesion.fechaEnvioOtpMail = Fecha.ahora();
		sesion.claveOtp = otpMail;
		sesion.intentosOtp = sesion.intentosOtp + 1;
		sesion.estado = "ENVIAR_OTP_MAIL_OK";
		sesion.saveSesion();
		
		LogBB.evento(contexto, "ENVIAR_OTP_MAIL_OK");

		Objeto respuesta = respuesta();
		if (!contexto.esProduccion()) {
			respuesta.set("otp", otpMail);
		}
		
		return respuesta.set("proximoReenvioEn", sesion.tiempoReenvioOtp());
	}

	public static Objeto enviarOtpSms(ContextoBB contexto) {
		
		String otpSms = generarOtp(contexto);
		
		if (contexto.esProduccion()) {
			EnvioSMS envioSMS = ApiNotificaciones.envioOtpSms(contexto, contexto.sesion().telefono(), otpSms).tryGet();
			if (envioSMS == null) {
				contexto.sesion().actualizarEstado("ERROR_ENVIAR_OTP_SMS");
				LogBB.error(contexto, "ENVIO_SMS");
				return respuesta("ENVIO_SMS");
			}
		}

		SesionBB sesion = contexto.sesion();
		sesion.fechaEnvioOtpSms = Fecha.ahora();
		sesion.claveOtp = otpSms;
		sesion.intentosOtp = sesion.intentosOtp + 1;
		sesion.estado = "ENVIAR_OTP_SMS_OK";
		sesion.saveSesion();
		
		LogBB.evento(contexto, "ENVIAR_OTP_SMS_OK");

		Objeto respuesta = respuesta();
		if (!contexto.esProduccion()) {
			respuesta.set("otp", otpSms);
		}
		
		return respuesta.set("proximoReenvioEn", sesion.tiempoReenvioOtp());
	}
		
		
	private static String generarOtp(ContextoBB contexto) {
		if(!contexto.sesion().getParamOtpV2(contexto)) {
			return random(100000, 999999).toString().substring(0, 6);
		}

		OTP respuestaOtp = ApiSeguridad.generarOTP(contexto, contexto.sesion().usuarioOtp()).tryGet();
		if(!empty(respuestaOtp)) {
			SesionBB sesion = contexto.sesion();
			sesion.stateId = respuestaOtp.stateId;
			sesion.cookie = respuestaOtp.cookie;
			return respuestaOtp.clave;
		}
		
		return null;
	}
	
	public static Objeto guardartelefono(ContextoBB contexto) {
		String codArea = contexto.parametros.string("codArea");
		String celular = contexto.parametros.string("celular");

		if (codArea.equals("15") || codArea.equals("54")) {
			codArea = "11";
		}

		if (!Telefono.esTelefonoValido(codArea, celular)) {
			contexto.sesion().actualizarEstado("ERROR_GUARDAR_CELULAR");
			LogBB.error(contexto, "CELULAR_INVALIDO", contexto.parametros);
			return respuesta("CELULAR_INVALIDO");
		}

		if (contexto.esProduccion()) {
			if (esTelefonoFinalizado(contexto)) {
				contexto.sesion().actualizarEstado("ERROR_GUARDAR_CELULAR");
				LogBB.error(contexto, "CELULAR_CLIENTE");
				return respuesta("CELULAR_CLIENTE");
			}
		}
		
		SesionBB sesion = contexto.sesion();
		sesion.codArea = codArea;
		sesion.celular = celular;
		sesion.intentosOtp = 0;
		sesion.estado = "GUARDAR_TELEFONO_OK";
		sesion.telefonoOtpValidado = false;

		Objeto respuesta = respuesta();
		if(sesion.esFlujoTcv()){
			boolean tieneCelularOtpValidado = tieneCelularOtpValidado(contexto);
			sesion.telefonoOtpValidado = tieneCelularOtpValidado;
			respuesta.set("validarTelefono", !tieneCelularOtpValidado);
		}

		sesion.save();

		LogBB.evento(contexto, "GUARDAR_TELEFONO_OK", contexto.parametros);
		return respuesta;
	}

	public static Object validarOtpMail(ContextoBB contexto) {
		String otpMail = contexto.parametros.string("otp");
		
		SesionBB sesion = contexto.sesion();

		if (!validarOtp(contexto, otpMail)) {
			sesion.actualizarEstado("ERROR_VALIDAR_OTP_MAIL");
			LogBB.error(contexto, "OTP_INVALIDO", "MAIL || otpCliente: " + otpMail + " | otpBB: " + sesion.claveOtp);
			return respuesta("OTP_INVALIDO");
		}

		if (!sesion.validoOtpMail()) {
			sesion.actualizarEstado("ERROR_VALIDAR_OTP_MAIL");
			LogBB.error(contexto, "OTP_INVALIDO");
			return respuesta("OTP_INVALIDO");
		}

		sesion.claveOtp = "";
		sesion.stateId = "";
		sesion.cookie = "";
		sesion.emailOtpValidado = true;
		sesion.intentosOtp = 0;
		sesion.estado = "VALIDAR_OTP_MAIL_OK";
		sesion.save();

		LogBB.evento(contexto, "VALIDAR_OTP_MAIL_OK");
		return respuesta();
	}

	public static Object validarOtpSms(ContextoBB contexto) {
		String otpSms = contexto.parametros.string("otp");

		SesionBB sesion = contexto.sesion();
		String datos = "SMS || otpCliente: " + otpSms + " | otpBB: " + sesion.claveOtp;

		if (!validarOtp(contexto, otpSms)) {
			sesion.actualizarEstado("ERROR_VALIDAR_OTP_SMS");
			LogBB.error(contexto, "OTP_INVALIDO", datos);
			return respuesta("OTP_INVALIDO");
		}

		if (!sesion.validoOtpSms()) {
			sesion.actualizarEstado("ERROR_VALIDAR_OTP_SMS");
			LogBB.error(contexto, "OTP_INVALIDO");
			return respuesta("OTP_INVALIDO");
		}
		
		sesion.claveOtp = "";
		sesion.stateId = "";
		sesion.cookie = "";
		sesion.telefonoOtpValidado = true;
		sesion.intentosOtp = 0;
		sesion.estado = "VALIDAR_OTP_SMS_OK";
		sesion.save();

		LogBB.evento(contexto, "VALIDAR_OTP_SMS_OK");
		return respuesta();
	}
	
	private static Boolean validarOtp(ContextoBB contexto, String otp) {
		SesionBB sesion = contexto.sesion();
		if(!sesion.getParamOtpV2(contexto)) {
			return otp.equals(sesion.claveOtp);
		}
		
		if(otp.equals(sesion.claveOtp)) {
			return ApiSeguridad.validarOtp(contexto, sesion.usuarioOtp(), otp, sesion.stateId, sesion.cookie).tryGet();			
		}
		
		return false;
	}

	public static Objeto aceptartyc(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		sesion.aceptartyc = "aceptar";
		sesion.estado = "TYC_ACEPTADO";
		sesion.saveSesion();

		LogBB.evento(contexto, "TYC_ACEPTADO");
		return respuesta();
	}

	public static Objeto guardarContacto(ContextoBB contexto) {
		String mail = contexto.parametros.string("mail");
		String codArea = contexto.parametros.string("codArea");
		String celular = contexto.parametros.string("celular");

		if (codArea.equals("15") || codArea.equals("54")) {
			codArea = "11";
		}

		if (!Telefono.esTelefonoValido(codArea, celular)) {
			contexto.sesion().actualizarEstado("ERROR_GUARDAR_CONTACTO");
			LogBB.error(contexto, "CELULAR_INVALIDO", contexto.parametros);
			return respuesta("CELULAR_INVALIDO");
		}

		if (!Validadores.esMailValido(mail)) {
			contexto.sesion().actualizarEstado("ERROR_GUARDAR_CONTACTO");
			LogBB.error(contexto, "MAIL_INVALIDO", contexto.parametros);
			return respuesta("MAIL_INVALIDO");
		}

		if (contexto.esProduccion()) {
			if (esMailFinalizado(contexto, mail)) {
				contexto.sesion().actualizarEstado("ERROR_GUARDAR_CONTACTO");
				LogBB.error(contexto, "MAIL_CLIENTE");
				return respuesta("MAIL_CLIENTE");
			}

			if (esTelefonoFinalizado(contexto)) {
				contexto.sesion().actualizarEstado("ERROR_GUARDAR_CONTACTO");
				LogBB.error(contexto, "CELULAR_CLIENTE");
				return respuesta("CELULAR_CLIENTE");
			}
		}
		
		SesionBB sesion = contexto.sesion();
		sesion.mail = mail;
		sesion.codArea = codArea;
		sesion.celular = celular;
		sesion.intentosOtp = 0;
		sesion.estado = "GUARDAR_CONTACTO_OK";
		sesion.telefonoOtpValidado = false;
		sesion.emailOtpValidado = false;

		Objeto respuesta = respuesta();
		if(sesion.esFlujoTcv()){
			boolean tieneCelularOtpValidado = tieneCelularOtpValidado(contexto);
			boolean tieneMailOtpValidado = tieneMailOtpValidado(contexto);
			sesion.telefonoOtpValidado = tieneCelularOtpValidado;
			sesion.emailOtpValidado = tieneMailOtpValidado;

			respuesta.set("validarTelefono", !tieneCelularOtpValidado);
			respuesta.set("validarEmail", !tieneMailOtpValidado);
		}

		sesion.save();

		LogBB.evento(contexto, "GUARDAR_CONTACTO_OK", contexto.parametros);
		return respuesta;
	}
	
	private static boolean esMailFinalizado(ContextoBB contexto, String mail) {
		return SqlEsales.existeMailFinalizado(contexto, contexto.sesion().cuil, mail).tryGet(false);
	}

	private static boolean esTelefonoFinalizado(ContextoBB contexto) {
		String codArea = contexto.parametros.string("codArea");
		String celular = contexto.parametros.string("celular");

		String codigoArea = Telefono.obtenerCodigoArea(codArea);
		String caracteristica = Telefono.obtenerCaracteristica(codArea, celular);
		String numero = Telefono.obtenerNumero(codArea, celular);

		return SqlEsales.existeTelefonoFinalizado(contexto, contexto.sesion().cuil, codigoArea, caracteristica, numero).tryGet(false);
	}

	public static Object guardarMail(ContextoBB contexto) {
		String mail = contexto.parametros.string("mail");
		
		if (!Validadores.esMailValido(mail)) {
			contexto.sesion().actualizarEstado("ERROR_GUARDAR_MAIL");
			LogBB.error(contexto, "MAIL_INVALIDO", contexto.parametros);
			return respuesta("MAIL_INVALIDO");
		}
	
		if (contexto.esProduccion()) {
			if (esMailFinalizado(contexto, mail)) {
				contexto.sesion().actualizarEstado("ERROR_GUARDAR_MAIL");
				LogBB.error(contexto, "MAIL_CLIENTE");
				return respuesta("MAIL_CLIENTE");
			}
		}

		SesionBB sesion = contexto.sesion();
		sesion.mail = mail;
		sesion.intentosOtp = 0;
		sesion.estado = "GUARDAR_MAIL_OK";
		sesion.emailOtpValidado = false;

		Objeto respuesta = respuesta();
		if(sesion.esFlujoTcv()){
			boolean tieneMailOtpValidado = tieneMailOtpValidado(contexto);
			sesion.emailOtpValidado = tieneMailOtpValidado;
			respuesta.set("validarEmail", !tieneMailOtpValidado);
		}

		sesion.save();

		LogBB.evento(contexto, "GUARDAR_MAIL_OK", contexto.parametros);
		return respuesta;
	}
	
	public static Boolean tieneCelularOtpValidado(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		String codArea = sesion.codArea;
		String celular = sesion.celular;

		String codigoArea = Telefono.obtenerCodigoArea(codArea);
		String caracteristica = Telefono.obtenerCaracteristica(codArea, celular);
		String numero = Telefono.obtenerNumero(codArea, celular);

		return SqlEsales.tieneTelefonoOtpValidado(contexto, sesion.cuil, codigoArea, caracteristica, numero).tryGet(false);
	}
	
	public static Boolean tieneMailOtpValidado(ContextoBB contexto) {
		return SqlEsales.tieneMailOtpValidado(contexto, contexto.sesion().cuil, contexto.sesion().mail).tryGet(false);
	}

	public static Objeto aceptartycOferta(ContextoBB contexto) {
		LogBB.evento(contexto, "TYC_OFERTA_ACEPTADO");
		return respuesta();
	}
}