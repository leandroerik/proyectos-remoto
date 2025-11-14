package ar.com.hipotecario.mobile.servicio;

import java.util.Date;

import com.google.gson.Gson;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.excepcion.ApiVentaExceptionMB;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.ResolucionMotor;
import ar.com.hipotecario.mobile.negocio.SolicitudAumentoLimite;

public class RestAumentoLimiteTC {

	/* ========== SOLICITUDES ========== */
	public static ApiResponseMB consultarSolicitudes(ContextoMB contexto, Long cantidadDias) {
		ApiRequestMB request = ApiMB.request("VentasConsultarSolicitudes", "ventas_windows", "GET", "/solicitudes", contexto);
		request.query("cuil", contexto.persona().cuit());
		String fechaDesde = Fecha.restarDias(new Date(), cantidadDias, "yyyyMMdd");
		request.query("fechadesde", fechaDesde);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB consultarSolicitud(ContextoMB contexto, String idSolicitud) {
		ApiRequestMB request = ApiMB.request("VentasConsultarSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("idSolicitud", idSolicitud);
		return ApiMB.response(request, contexto.idCobis(), idSolicitud);
	}

	public static ApiResponseMB actualizarCanalSolicitud(ContextoMB contexto, String idSolicitud) {
		ApiRequestMB requestSolicitud = ApiMB.request("VentasConsultarSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}", contexto);
		requestSolicitud.headers.put("X-Handle", idSolicitud);
		requestSolicitud.path("idSolicitud", idSolicitud);
		ApiResponseMB responseSolicitud = ApiMB.response(requestSolicitud, contexto.idCobis(), idSolicitud);
		if (responseSolicitud.hayError()) {
			return responseSolicitud;
		}

//		Objeto datos = responseSolicitud.objetos("Datos").get(0);

		ApiRequestMB request = ApiMB.request("VentasPutCanal", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.body("TipoOperacion", "03");
		request.body("CanalOriginacion1", 2);
		request.body("CanalOriginacion2", ConfigMB.integer("api_venta_canalOriginacion2"));
		request.body("CanalOriginacion3", "HOBCCS");
		request.body("CanalVenta1", 12);
		request.body("CanalVenta2", 1);
		request.body("CanalVenta3", 1);
		request.body("CanalVenta4", ConfigMB.string("api_venta_canalVenta4"));
		request.body("Oficina", "0");
		request.body("Simulacion", false);
		return ApiMB.response(request, contexto.idCobis(), idSolicitud);
	}

	public static ApiResponseMB generarSolicitud(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("VentasGenerarSolicitud", "ventas_windows", "POST", "/solicitudes", contexto);
		request.headers.put("X-Handle", "0");
		request.body("TipoOperacion", "02");
		request.body("CanalOriginacion1", ConfigMB.integer("api_venta_canalOriginacion1"));
		request.body("CanalOriginacion2", ConfigMB.integer("api_venta_canalOriginacion2"));
		request.body("CanalOriginacion3", ConfigMB.string("api_venta_canalOriginacion3"));
		request.body("CanalVenta1", ConfigMB.string("api_venta_canalVenta1"));
		request.body("CanalVenta2", ConfigMB.string("api_venta_canalVenta2"));
		request.body("CanalVenta3", ConfigMB.string("api_venta_canalVenta3"));
		request.body("CanalVenta4", ConfigMB.string("api_venta_canalVenta4"));
		request.body("Oficina", "0");
		// TODO probando usar el usuario de MB
		request.headers.put("X-Usuario", ConfigMB.string("configuracion_usuario_mb"));
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ResolucionMotor ejecutarMotor(ContextoMB contexto, String idSolicitud, Boolean solicitaComprobarIngresos) {
		ApiRequestMB request = ApiMB.request("VentasEvaluarSolicitud", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/resoluciones", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		// request.body("solicitudId", idSolicitud);
		request.body("TipoOperacion", "03");
		request.body("FlagSolicitaAprobacionCentralizada", false);
		request.body("FlagSolicitaValidarIdentidad", false);
		request.body("FlagSolicitaComprobarIngresos", false);
		request.body("FlagSolicitaAprobacionEstandard", false);
		request.body("FlagSolicitaExcepcion", false);
		request.body("FlagSolicitaEvaluarMercadoAbierto", false);
		request.body("EsPlanSueldo", false);
		request.body("FlagSolicitaExcepcionCRM", false);

		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

		if (response.hayError() || !response.objetos("Errores").isEmpty()) {
			throw new ApiVentaExceptionMB(response);
		}

		Objeto datos = response.objetos("Datos").get(0);
		ResolucionMotor resolucionMotor = (new Gson()).fromJson(datos.toJson(), ResolucionMotor.class);
		return resolucionMotor;
	}

	public static ApiResponseMB simularFinalizarSolicitud(ContextoMB contexto, String idSolicitud) {
		ApiRequestMB request = ApiMB.request("VentasFinalizarSolicitud", "ventas_windows", "GET", "/solicitudes/{SolicitudId}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.query("estado", "validar");
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB finalizarSolicitud(ContextoMB contexto, String idSolicitud) {
		ApiRequestMB request = ApiMB.request("VentasFinalizarSolicitud", "ventas_windows", "GET", "/solicitudes/{SolicitudId}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.query("estado", "finalizar");
		// TODO probando usar el usuario de MB
		request.headers.put("X-Usuario", ConfigMB.string("configuracion_usuario_mb"));
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB desistirSolicitud(ContextoMB contexto, String idSolicitud) {
		ApiRequestMB request = ApiMB.request("VentasDesistirSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("idSolicitud", idSolicitud);
		request.query("estado", "desistir");
		// TODO probando usar el usuario de MB
		request.headers.put("X-Usuario", ConfigMB.string("configuracion_usuario_mb"));
		return ApiMB.response(request, contexto.idCobis());
	}

	/* ========== INTEGRANTES ========== */
	public static ApiResponseMB generarIntegrante(ContextoMB contexto, String idSolicitud) {
		ApiRequestMB request = ApiMB.request("VentasGenerarIntegrante", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/integrantes", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.body("Secuencia", 0);
		request.body("SolicitudId", idSolicitud);
		request.body("NumeroTributario", contexto.persona().cuit());
		request.body("TipoOperacion", "02");
		// TODO probando usar el usuario de MB
		request.headers.put("X-Usuario", ConfigMB.string("configuracion_usuario_mb"));
		ApiResponseMB response = ApiMB.response(request, contexto.idCobis());

		if (contexto.persona().idEstadoCivil().equals("C")) {
			String cuitConyuge = RestPersona.cuitConyugeSinFechaFinRelacion(contexto);
			if (cuitConyuge != null) {
				ApiRequestMB requestConyuge = ApiMB.request("VentasGenerarIntegrante", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/integrantes", contexto);
				requestConyuge.headers.put("X-Handle", idSolicitud);
				requestConyuge.path("SolicitudId", idSolicitud);
				requestConyuge.body("NumeroTributario", Long.valueOf(cuitConyuge));
				requestConyuge.body("TipoOperacion", "03");
				// TODO probando usar el usuario de MB
				requestConyuge.headers.put("X-Usuario", ConfigMB.string("configuracion_usuario_mb"));
				ApiMB.response(requestConyuge, contexto.idCobis());
			}
		}
		return response;
	}

	public static ApiResponseMB generarAumentoLimiteTC(ContextoMB contexto, String idSolicitud, String cuenta) {
		// String idCuenta = contexto.parametros.string("idCuenta");
		Objeto integrante = new Objeto();
		integrante.set("numeroDocumentoTributario", contexto.persona().cuit()); // 20942942495
		integrante.set("rol", "T");
		ApiRequestMB request = ApiMB.request("VentasGenerarAumentoLimiteTC", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/inclusionModificacion", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.body("Cuenta", cuenta);
		request.add("Integrantes", integrante);
		request.body("IdProductoFrontEnd", 17);
		request.body("TipoOperacion", "02"); // Se manda 02 para que no haga todas las validaciones
		// TODO probando usar el usuario de MB
		request.headers.put("X-Usuario", ConfigMB.string("configuracion_usuario_mb"));
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB actualizarAumentoLimiteTC(ContextoMB contexto, String idSolicitud, String cuenta, SolicitudAumentoLimite solicitudAumentoLimite) {
		Objeto integrante = new Objeto();
		integrante.set("numeroDocumentoTributario", contexto.persona().cuit()); // 20942942495
		integrante.set("rol", "T");
		// Actualizar AUMENTO
		ApiRequestMB requestv2 = ApiMB.request("VentasActualizarPaquete", "ventas_windows", "PUT", "/solicitudes/{idSolicitud}/inclusionModificacion/{inclusionModificacion}", contexto);
		requestv2.headers.put("X-Handle", idSolicitud);
		requestv2.path("idSolicitud", idSolicitud);
		requestv2.path("inclusionModificacion", solicitudAumentoLimite.Id);
		requestv2.body("Cuenta", cuenta);
		requestv2.body("montoOfrecido", solicitudAumentoLimite.MontoOfrecido);
		requestv2.body("montoAceptado", solicitudAumentoLimite.MontoAceptado);
		requestv2.add("Integrantes", integrante);
		requestv2.body("IdProductoFrontEnd", 17);
		requestv2.body("TipoOperacion", "03"); // Se manda 02 para que no haga todas las validaciones
		// TODO probando usar el usuario de MB
		requestv2.headers.put("X-Usuario", ConfigMB.string("configuracion_usuario_mb"));
		return ApiMB.response(requestv2, contexto.idCobis());
	}

	public static ApiResponseMB obtenerConsultaAumento(ContextoMB contexto, String idSolicitud, String idSolicitudAumento) {
		ApiRequestMB request = ApiMB.request("VentasConsultarSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}/inclusionModificacion/{inclusionModificacion}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("idSolicitud", idSolicitud);
		request.path("inclusionModificacion", idSolicitudAumento);
		// TODO probando usar el usuario de MB
		request.headers.put("X-Usuario", ConfigMB.string("configuracion_usuario_mb"));
		return ApiMB.response(request, idSolicitud);
	}

	public static ApiResponseMB ejecutarMotorResponse(ContextoMB contexto, String idSolicitud, Boolean solicitaComprobarIngresos, Boolean esPlanSueldo) {
		ApiRequestMB request = ApiMB.request("VentasEvaluarSolicitud", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/resoluciones", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.body("TipoOperacion", "03");

		Boolean flagSolicitaAprobacionCentralizada = false;

//		if (!Config.esProduccion()) {
//			Set<String> cobisMock = Objeto.setOf("6824609_803_762_3434960_772245_2976577_5594861_24077_315321_13439_339902_11379_112675_320116_1027_6848_423132_197699_474613_841518_946033_2485262_1108832_2531256_3246448_4031353_647173_5277303_5710970_1415107_4688328_" + "2832060_5757757_1772676_3666138_5876697_895579_4031353_2806142_4458806_4474187_3533391_4052868_4693090_3411734_4494305_269289_4184812_4633983_3780656_4616895_4619841_4502759_4083437_4605953_655481_3969117_36481_4678980_1560544_4667279_3934530_4657715_" + "1015123_4517581_151902_4643212_4343440_306983_1408386_2803514_1303691_4340145_4337807_774661_3115468_1340067_4744516_879309_1552843_1127410_2699672_1124858_1516963_215261_920263_4486142_4778332_4344757_1561490_3830624_4383314_775175_1505681_4523393_4768649_"
//					+ "4764558_901495_890174_874189_2392088_4788465_4805041_5013889_1373702_2881738_4351902_808346_4860492_1761355_774717_1838563".split("_"));
//			// centralizada e ingresos AA con doc
//			// !centralizada e ingresos AV con doc
//			if (cobisMock.contains(contexto.idCobis())) {
//				solicitaComprobarIngresos = true;
//				flagSolicitaAprobacionCentralizada = true;
//			}
//		}

		request.body("FlagSolicitaAprobacionCentralizada", flagSolicitaAprobacionCentralizada);
		request.body("FlagSolicitaValidarIdentidad", false);
		request.body("FlagSolicitaComprobarIngresos", solicitaComprobarIngresos);
		request.body("FlagSolicitaAprobacionEstandard", false);
		request.body("FlagSolicitaExcepcion", false);
		request.body("FlagSolicitaEvaluarMercadoAbierto", false);
		request.body("EsPlanSueldo", esPlanSueldo);
		request.body("FlagSolicitaExcepcionCRM", false);
		// TODO probando usar el usuario de MB
		request.headers.put("X-Usuario", ConfigMB.string("configuracion_usuario_mb"));
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB documentacionObligatoriaSolicitud(ContextoMB contexto, String idSolicitud) {
		ApiRequestMB request = ApiMB.request("DocumentacionGET", "ventas_windows", "GET", "/solicitudes/{idSolicitud}/documentacion?solo-obligatorios=true&consultar-contenedor=false", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("idSolicitud", idSolicitud);
		// TODO probando usar el usuario de MB
		request.headers.put("X-Usuario", ConfigMB.string("configuracion_usuario_mb"));
		return ApiMB.response(request, contexto.idCobis(), idSolicitud);
	}

}
