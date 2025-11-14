package ar.com.hipotecario.mobile.api;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.ResolucionMotor;
import ar.com.hipotecario.mobile.negocio.Solicitud;
import ar.com.hipotecario.mobile.negocio.SolicitudCuentaCorriente;
import ar.com.hipotecario.mobile.servicio.ProductosService;
import ar.com.hipotecario.mobile.servicio.RestOmnicanalidad;
import ar.com.hipotecario.mobile.servicio.RestPersona;
import ar.com.hipotecario.mobile.servicio.RestVenta;
import ar.com.hipotecario.mobile.servicio.SqlAdelanto;

public class MBAdelanto {

	public static RespuestaMB catalogoPreguntaRespuesta(ContextoMB contexto) {

		if (!ConfigMB.bool("prendido_adelanto_bh")) {
			return RespuestaMB.estado("ERROR_ADELANTO_NO_DISPONIBLE");
		}

		List<Objeto> catalogo = SqlAdelanto.preguntasFrecuentes(contexto);
		if (catalogo.isEmpty()) {
			return new RespuestaMB();
		}

		return new RespuestaMB().set("adelantoBH", catalogo);
	}

	public static RespuestaMB solicitudAdelanto(ContextoMB contexto) {
		Solicitud solicitud = null;
		String idSolicitud = null;
		String idAdelanto = null;

		if (!ConfigMB.bool("prendido_adelanto_bh")) {
			return RespuestaMB.estado("ERROR_ADELANTO_NO_DISPONIBLE");
		}

		if (ConfigMB.esOpenShift()) {
			RestOmnicanalidad.limpiarSolicitudes(contexto, ConfigMB.longer("solicitud_dias_vigente", 30L), false, false, true);
		}

		RespuestaMB validador = validadorCliente(contexto);
		if (Objects.nonNull(validador)) {
			return validador;
		}

		solicitud = Solicitud.generarSolicitud(contexto);
		idSolicitud = solicitud.Id;
		String cuitConyuge = contexto.persona().idEstadoCivil().equals("C") ? RestPersona.cuitConyuge(contexto) : null;
		String cuit = contexto.persona().cuit();
		solicitud = solicitud.generarIntegrantes(contexto, cuit, cuitConyuge);

		Cuenta cajaAhorro = contexto.cajaAhorroTitularPesos();
		if (cajaAhorro == null) {
			return RespuestaMB.estado("ERROR_SIN_CA");
		}

		SolicitudCuentaCorriente solicitudAdelanto = solicitud.generarAdelanto(contexto, idSolicitud, cajaAhorro.numero());
		idAdelanto = solicitudAdelanto.Id;

		if (idAdelanto == null || "".equals(idAdelanto)) {
			Solicitud.logOriginacion(contexto, idSolicitud, "statusAdelanto", null, "ERROR_ADELANTO_VACIO");
			return RespuestaMB.estado("ERROR_ADELANTO_VACIO");
		}
		// EVALUAR SOLICITUD
		RespuestaMB respuesta = new RespuestaMB();
		ResolucionMotor evaluacionSolicitud = solicitud.ejecutarMotor(contexto);
		if (evaluacionSolicitud.esAprobadoAmarillo() || evaluacionSolicitud.esAmarillo() || evaluacionSolicitud.esSAmarillo()) {
			Solicitud.logOriginacion(contexto, idSolicitud, "ejecutarMotor", null, "ROJO - :" + evaluacionSolicitud.ResolucionId);
			respuesta.set("color", "AMARILLO");
			return respuesta;
		}
		if (evaluacionSolicitud.esRojo()) {
			Solicitud.logOriginacion(contexto, idSolicitud, "ejecutarMotor", null, "ROJO");
			respuesta.set("color", "ROJO");
			return respuesta;
		}

		contexto.parametros.set("idSolicitud", idSolicitud);
		contexto.parametros.set("idAdelanto", idAdelanto);
		respuesta = consultarAdelanto(contexto);
		respuesta.set("color", "VERDE");//
		Solicitud.logOriginacion(contexto, idSolicitud, "FIN_solicitarAdelanto", null, respuesta.toJson());
		return respuesta;
	}

	public static RespuestaMB consultarAdelanto(ContextoMB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud");
		String cuentaCorrienteId = contexto.parametros.string("idAdelanto");
		RespuestaMB respuesta = new RespuestaMB();

		ApiResponseMB adelantoGet = RestVenta.consultarSolicitudAdelanto(contexto, idSolicitud, cuentaCorrienteId);
		if (adelantoGet.hayError()) {
			Solicitud.logOriginacion(contexto, idSolicitud, "consultarSolicitudAdelanto", adelantoGet, "");
			return RespuestaMB.error();
		}

		if (adelantoGet.objetos("Datos") != null && adelantoGet.objetos("Datos").size() > 0) {
			Objeto item = adelantoGet.objetos("Datos").get(0);
			respuesta.set("idSolicitud", idSolicitud);
			respuesta.set("idAdelanto", cuentaCorrienteId);
			BigDecimal monto = BigDecimal.ZERO;
			respuesta.set("monto", monto);
			if (item.existe("Acuerdo")) {
				Objeto acuerdo = item.objeto("Acuerdo");
				monto = acuerdo.bigDecimal("ValorAutorizado");
				respuesta.set("moneda", Formateador.moneda(item.string("Moneda")));
				respuesta.set("simboloMoneda", Formateador.simboloMoneda(item.string("Moneda")));
				respuesta.set("monto", monto);
				respuesta.set("montoFormateado", Formateador.importe(monto));
				respuesta.set("plazo", acuerdo.integer("Plazo"));
				respuesta.set("tipoDescubierto", acuerdo.string("TipoDescubierto"));
			}
		} else {
			Solicitud.logOriginacion(contexto, idSolicitud, "consultarAdelanto", adelantoGet, "");
			return RespuestaMB.error();
		}

		Solicitud.logOriginacion(contexto, idSolicitud, "FIN_consultarAdelanto", null, respuesta.toJson());
		return respuesta;
	}

	public static RespuestaMB finalizarSolicitud(ContextoMB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud");

		RespuestaMB valida = Solicitud.validarFinalizarSolicitud(contexto);
		if (valida != null && valida.hayError()) {
			return valida;
		}

		ApiResponseMB responseConsultarSolicitud = RestOmnicanalidad.consultarSolicitud(contexto, idSolicitud);
		if (responseConsultarSolicitud.hayError()) {
			return RespuestaMB.estado("ERROR_CONSULTA_SOLICITUD");
		}
		boolean encuentraIdCobis = false;
		for (Objeto integrante : responseConsultarSolicitud.objetos("Datos").get(0).objetos("Integrantes")) {
			if (contexto.idCobis().equals(integrante.string("IdCobis")))
				encuentraIdCobis = true;
		}
		if (!encuentraIdCobis) {
			return RespuestaMB.estado("ERROR_ID_COBIS");
		}

		ApiResponseMB response = RestVenta.finalizarSolicitud(contexto, idSolicitud);
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			try {
				String mensajeCliente = response.objetos("Errores").get(0).string("MensajeCliente");
				return RespuestaMB.estado(mensajeCliente);
			} catch (Exception e) {
			}

			if (response.objetos("Errores").get(0).string("MensajeDesarrollador").contains("FAULTCODE:40003 FAULTMSJ:Producto bancario deshabilitado")) {
				return new RespuestaMB().setEstado("ERROR_CORRIENDO_BATCH");
			}
			return RespuestaMB.error();
		}

		ProductosService.eliminarCacheProductos(contexto);
		if (ConfigMB.esOpenShift()) {
			RestOmnicanalidad.limpiarSolicitudes(contexto, ConfigMB.longer("solicitud_dias_vigente", 30L), true, false, true);
		}

		Objeto salida = new Objeto().set("idSolicitud", idSolicitud);
		contexto.parametros.set("nemonico", "FINALIZA_ADE");
		Util.contador(contexto);
		Solicitud.logOriginacion(contexto, idSolicitud, "FIN_finalizarSolicitud", null, salida.toJson());
		enviaEmail(contexto);
		contexto.sesion().setAceptaTyC(false);
		contexto.sesion().setValidaRiesgoNet(false);
		contexto.limpiarSegundoFactor();
		return RespuestaMB.exito();

	}

	private static RespuestaMB validadorCliente(ContextoMB contexto) {

		if (contexto.persona().edad() < 18)
			return RespuestaMB.estado("MENOR_DE_EDAD");

		Cuenta CC = contexto.cuentaCorrienteTitular();
		if (Objects.nonNull(CC) && !"ADE".equalsIgnoreCase(CC.categoria())) {
			return RespuestaMB.estado("POSEE_CUENTA_CORRIENTE");
		}

		Cuenta CA = contexto.cajaAhorroTitularPesos();
		if (Objects.isNull(CA)) {
			return RespuestaMB.estado("NO_POSEE_CAJA_AHORRO");
		}

		if (Objects.nonNull(CA)) {
			if (!categoriaCA(CA.categoria())) {
				return RespuestaMB.estado("CAJA_AHORRO_NO_VALIDA");
			}
			contexto.parametros.set(CA.id());
			RespuestaMB cuentaBloqueada = MBCuenta.cajaAhorroBloqueos(contexto);
			if (!cuentaBloqueada.hayError() && cuentaBloqueada.bool("tieneBloqueos")) {
				return RespuestaMB.estado("CAJA_AHORRO_NO_VALIDA");
			}
			if ("C".equalsIgnoreCase(CA.usoFirma())) {
				return RespuestaMB.estado("CAJA_AHORRO_NO_VALIDA");
			}
		}

		if (Objects.nonNull(contexto.AdelantoTitular())) {
			return RespuestaMB.estado("ADELANTO_VIGENTE");
		}

		return null;
	}

	private static Boolean categoriaCA(String categoria) {

//		CAR - C.A.RACING
//		CI  - CUENTA INVERSOR
//		D   - NORMAL
//		EV  - CUENTA SUELDO VIP
//		K   - CUENTA SUELDO
//		L   - CUENTA SUELDO BH
//		M   - JUBILADOS 
//		MOV - CAJA DE AHORRO MOVIL
//		PBH - PAQUETE BH FACIL
//		PCA - PROCREAR1

		if (Objeto.setOf("K", "EV", "M", "L", "MOV", "PBH", "PCA", "D", "CI", "CAR").contains(categoria))
			return true;

		return false;
	}

	//Inicialmente estaba apagado el envio de mail en mobile, se activa únicamente salesforce
	private static void enviaEmail(ContextoMB contexto) {
		try {
			if ( MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
				Objeto parametros = new Objeto();
				parametros.set("Subject", "Aviso de desembolso Adelanto de Sueldo");
				parametros.set("NOMBRE", contexto.persona().nombre());
				parametros.set("MONTO", contexto.parametros.bigDecimal("monto"));
				parametros.set("CUENTA", "******" + contexto.cuenta(contexto.parametros.string("cuenta")).ultimos4digitos());
				Date fechaLimite = new Date(Calendar.DATE + 45);
				parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(fechaLimite));

				String salesforce_desembolso_adelanto = ConfigMB.string("salesforce_desembolso_adelanto");
				parametros.set("ID_COBIS", contexto.idCobis());
				parametros.set("ISMOBILE", contexto.esMobile());
				parametros.set("APELLIDO", contexto.persona().apellido());
				parametros.set("MONTO_ADELANTO", contexto.parametros.bigDecimal("monto"));
				parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
				new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_desembolso_adelanto, parametros));
			}
		} catch (Exception e) {
		}

	}

}