package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.servicio.ProductosService;
import ar.com.hipotecario.canal.homebanking.servicio.RestContexto;
import ar.com.hipotecario.canal.homebanking.servicio.RestNotificaciones;
import ar.com.hipotecario.canal.homebanking.servicio.RestOmnicanalidad;
import ar.com.hipotecario.canal.homebanking.servicio.RestPersona;
import ar.com.hipotecario.canal.homebanking.servicio.RestVenta;
import ar.com.hipotecario.canal.homebanking.servicio.SqlAdelanto;
import ar.com.hipotecario.canal.homebanking.ventas.ResolucionMotor;
import ar.com.hipotecario.canal.homebanking.ventas.Solicitud;
import ar.com.hipotecario.canal.homebanking.ventas.SolicitudCuentaCorriente;

public class HBAdelanto {

	public static Respuesta catalogoPreguntaRespuesta(ContextoHB contexto) {
		
		if (contexto.idCobis() == null || contexto.idCobis().isEmpty()) {
			return Respuesta.estado("SIN_SESION");
		}

		if (!ConfigHB.bool("prendido_adelanto_bh")) {
			return Respuesta.estado("ERROR_ADELANTO_NO_DISPONIBLE");
		}

		List<Objeto> catalogo = SqlAdelanto.preguntasFrecuentes(contexto);
		if (catalogo.isEmpty()) {
			return new Respuesta();
		}

		return new Respuesta().set("adelantoBH", catalogo);
	}

	public static Respuesta solicitudAdelanto(ContextoHB contexto) {
		Solicitud solicitud = null;
		String idSolicitud = null;
		String idAdelanto = null;

		if (!ConfigHB.bool("prendido_adelanto_bh")) {
			return Respuesta.estado("ERROR_ADELANTO_NO_DISPONIBLE");
		}

		if (ConfigHB.esOpenShift()) {
			RestOmnicanalidad.limpiarSolicitudes(contexto, ConfigHB.longer("solicitud_dias_vigente", 30L), false, false, true);
		}

		Respuesta validador = validadorCliente(contexto);
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
			return Respuesta.estado("ERROR_SIN_CA");
		}

		SolicitudCuentaCorriente solicitudAdelanto = solicitud.generarAdelanto(contexto, idSolicitud, cajaAhorro.numero());
		idAdelanto = solicitudAdelanto.Id;

		if (idAdelanto == null || "".equals(idAdelanto)) {
			Solicitud.logOriginacion(contexto, idSolicitud, "statusAdelanto", null, "ERROR_ADELANTO_VACIO");
			return Respuesta.estado("ERROR_ADELANTO_VACIO");
		}

		// EVALUAR SOLICITUD
		Respuesta respuesta = new Respuesta();
		ResolucionMotor evaluacionSolicitud = solicitud.ejecutarMotor(contexto);
		if (evaluacionSolicitud.esAprobadoAmarillo() || evaluacionSolicitud.esAmarillo() || evaluacionSolicitud.esSAmarillo()) {
			Solicitud.logOriginacion(contexto, idSolicitud, "ejecutarMotor", null, "ROJO - :" + evaluacionSolicitud.ResolucionId);
			respuesta.set("color", "ROJO");
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

	public static Respuesta consultarAdelanto(ContextoHB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud");
		String cuentaCorrienteId = contexto.parametros.string("idAdelanto");
		Respuesta respuesta = new Respuesta();

		ApiResponse adelantoGet = RestVenta.consultarSolicitudAdelanto(contexto, idSolicitud, cuentaCorrienteId);
		if (adelantoGet.hayError()) {
			Solicitud.logOriginacion(contexto, idSolicitud, "consultarSolicitudAdelanto", adelantoGet, "");
			return Respuesta.error();
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
			return Respuesta.error();
		}

		Solicitud.logOriginacion(contexto, idSolicitud, "FIN_consultarAdelanto", null, respuesta.toJson());
		return respuesta;
	}

	private static Respuesta validadorCliente(ContextoHB contexto) {

		if (contexto.persona().edad() < 18)
			return Respuesta.estado("MENOR_DE_EDAD");

		Cuenta CC = contexto.cuentaCorrienteTitular();
		if (Objects.nonNull(CC) && !"ADE".equalsIgnoreCase(CC.categoria())) {
			return Respuesta.estado("POSEE_CUENTA_CORRIENTE");
		}

		Cuenta CA = contexto.cajaAhorroTitularPesos();
		if (Objects.isNull(CA)) {
			return Respuesta.estado("NO_POSEE_CAJA_AHORRO");
		}

		if (Objects.nonNull(CA)) {
			if (!categoriaCA(CA.categoria())) {
				return Respuesta.estado("CAJA_AHORRO_NO_VALIDA");
			}
			contexto.parametros.set(CA.id());
			Respuesta cuentaBloqueada = HBCuenta.cajaAhorroBloqueos(contexto);
			if (!cuentaBloqueada.hayError() && cuentaBloqueada.bool("tieneBloqueos")) {
				return Respuesta.estado("CAJA_AHORRO_NO_VALIDA");
			}
			if ("C".equalsIgnoreCase(CA.usoFirma())) {
				return Respuesta.estado("CAJA_AHORRO_NO_VALIDA");
			}
		}

		if (Objects.nonNull(contexto.AdelantoTitular())) {
			return Respuesta.estado("ADELANTO_VIGENTE");
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

	public static Respuesta finalizarSolicitud(ContextoHB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud");

		Respuesta valida = validarFinalizarSolicitud(contexto);
		if (valida != null && valida.hayError()) {
			return valida;
		}

		ApiResponse responseConsultarSolicitud = RestOmnicanalidad.consultarSolicitud(contexto, idSolicitud);
		if (responseConsultarSolicitud.hayError()) {
			return Respuesta.estado("ERROR_CONSULTA_SOLICITUD");
		}
		boolean encuentraIdCobis = false;
		for (Objeto integrante : responseConsultarSolicitud.objetos("Datos").get(0).objetos("Integrantes")) {
			if (contexto.idCobis().equals(integrante.string("IdCobis")))
				encuentraIdCobis = true;
		}
		if (!encuentraIdCobis) {
			return Respuesta.estado("ERROR_ID_COBIS");
		}

		ApiResponse response = RestVenta.finalizarSolicitud(contexto, idSolicitud);
		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			try {
				String mensajeCliente = response.objetos("Errores").get(0).string("MensajeCliente");
				return Respuesta.estado(mensajeCliente);
			} catch (Exception e) {
			}

			if (response.objetos("Errores").get(0).string("MensajeDesarrollador").contains("FAULTCODE:40003 FAULTMSJ:Producto bancario deshabilitado")) {
				return new Respuesta().setEstado("ERROR_CORRIENDO_BATCH");
			}
			return Respuesta.error();
		}

		ProductosService.eliminarCacheProductos(contexto);
		if (ConfigHB.esOpenShift()) {
			RestOmnicanalidad.limpiarSolicitudes(contexto, ConfigHB.longer("solicitud_dias_vigente", 30L), true, false, true);
		}

		Objeto salida = new Objeto().set("idSolicitud", idSolicitud);
		contexto.parametros.set("nemonico", "FINALIZA_ADE");
		Util.contador(contexto);
		Solicitud.logOriginacion(contexto, idSolicitud, "FIN_finalizarSolicitud", null, salida.toJson());
		enviaEmail(contexto);
		contexto.sesion.aceptaTyC = false;
		contexto.sesion.validaRiesgoNet = false;
		contexto.limpiarSegundoFactor();
		return Respuesta.exito();

	}

	private static Respuesta validarFinalizarSolicitud(ContextoHB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud");

		if (Objeto.anyEmpty(idSolicitud)) {
			return Respuesta.parametrosIncorrectos();
		}
		if (!contexto.sesion.validaRiesgoNet) {
			return Respuesta.estado("REQUIERE_VALIDAR_RIESGO_NET");
		}
		if (!contexto.sesion.aceptaTyC) {
			return Respuesta.estado("REQUIERE_ACEPTAR_TyC");
		}
		if (!contexto.validaSegundoFactor("adelanto")) {
			return Respuesta.estado("REQUIERE_SEGUNDO_FACTOR");
		}
		if (RestContexto.cambioDetectadoParaNormativoPPV2(contexto, true)) {
			return Respuesta.estado("CAMBIO_INFO_PERSONAL_IMPORTANTE_PRESTAMO");
		}

		return Respuesta.exito();
	}

	private static void enviaEmail(ContextoHB contexto) {
		try {
			Objeto parametros = new Objeto();
			parametros.set("Subject", "Solicitaste un Adelanto BH");
			parametros.set("NOMBRE", contexto.persona().nombre());
			parametros.set("APELLIDO", contexto.persona().apellido());
			parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
			Date hoy = new Date();
			parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
			parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
			parametros.set("CANAL", "Home Banking");
			parametros.set("TITULAR_CANAL", contexto.persona().apellido());
			parametros.set("NOMBRE_PRESTAMO", "personal");
			parametros.set("MOSTRAR_ARREPENTIMIENTO", "true");

			if ("true".equals(ConfigHB.string("salesforce_prendido_alta_prestamo")) && HBSalesforce.prendidoSalesforceAmbienteBajoConFF(contexto)) {
				var parametrosSf = Objeto.fromJson(contexto.sesion.cache.get(ConfigHB.string("salesforce_alta_prestamo")));

				String salesforce_alta_prestamo = ConfigHB.string("salesforce_alta_prestamo");
				parametros.set("MONTO_PRESTAMO", parametrosSf.string("MONTO_PRESTAMO"));
				if(parametrosSf.string("FECHA_VENCIMIENTO_CUOTA") != null) {
			        LocalDate today = LocalDate.now();
			        LocalDate fechaVencimiento = LocalDate.of(today.getYear(), today.getMonth().plus(1), Integer.parseInt(parametrosSf.string("FECHA_VENCIMIENTO_CUOTA")));
			        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
					parametros.set("FECHA_VENCIMIENTO_CUOTA", fechaVencimiento.format(formatter).toString());
				}
				parametros.set("MONTO_CUOTA", parametrosSf.string("MONTO_CUOTA"));
				parametros.set("NUMERO_CUOTA", "1");
				new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_alta_prestamo, parametros));
			}
			else
				RestNotificaciones.envioMail(contexto, ConfigHB.string("doppler_alta_prestamo"), parametros);


			String celular = contexto.persona().celular();
			if (celular != null && !"".equals(celular)) {
				RestNotificaciones.sendSms(contexto, celular, "Solicitaste un Adelanto BH desde el canal Home Banking si no fuiste vos llamanos al 0810 222 7777", "");
			}
			contexto.sesion.cache.remove(ConfigHB.string("salesforce_alta_prestamo"));
		} catch (Exception e) {
		}
	}

}