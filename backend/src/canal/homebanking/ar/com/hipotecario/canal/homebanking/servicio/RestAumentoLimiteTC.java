package ar.com.hipotecario.canal.homebanking.servicio;

import java.util.Set;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.ventas.SolicitudAumentoLimite;

public class RestAumentoLimiteTC {

	/* ========== SOLICITUDES ========== */
	public static ApiResponse actualizarCanalSolicitud(ContextoHB contexto, String idSolicitud) {
		ApiRequest requestSolicitud = Api.request("VentasConsultarSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}", contexto);
		requestSolicitud.headers.put("X-Handle", idSolicitud);
		requestSolicitud.path("idSolicitud", idSolicitud);
		ApiResponse responseSolicitud = Api.response(requestSolicitud, contexto.idCobis(), idSolicitud);
		if (responseSolicitud.hayError()) {
			return responseSolicitud;
		}
//      Objeto datos = responseSolicitud.objetos("Datos").get(0);
		ApiRequest request = Api.request("VentasPutCanal", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.body("TipoOperacion", "03");
		request.body("CanalOriginacion1", ConfigHB.integer("api_venta_canalOriginacion1"));
		request.body("CanalOriginacion2", ConfigHB.integer("api_venta_canalOriginacion2"));
		request.body("CanalOriginacion3", ConfigHB.string("api_venta_canalOriginacion3"));
		request.body("CanalVenta1", ConfigHB.string("api_venta_canalVenta1"));
		request.body("CanalVenta2", ConfigHB.string("api_venta_canalVenta2"));
		request.body("CanalVenta3", ConfigHB.string("api_venta_canalVenta3"));
		request.body("CanalVenta4", ConfigHB.string("api_venta_canalVenta4"));
		request.body("Oficina", "0");
		request.body("Simulacion", false);
		request.body("TieneLegajoFisico", false);
		request.body("OrigenDocumentacion", "digital");
		request.body("DocumentacionEnContenedor", true);
		return Api.response(request, contexto.idCobis(), idSolicitud);
	}

	public static ApiResponse generarSolicitud(ContextoHB contexto) {
		ApiRequest request = Api.request("VentasGenerarSolicitud", "ventas_windows", "POST", "/solicitudes", contexto);
		request.headers.put("X-Handle", "0");
		request.body("TipoOperacion", "02");
		request.body("CanalOriginacion1", ConfigHB.integer("api_venta_canalOriginacion1"));
		request.body("CanalOriginacion2", ConfigHB.integer("api_venta_canalOriginacion2"));
		request.body("CanalOriginacion3", ConfigHB.string("api_venta_canalOriginacion3"));
		request.body("CanalVenta1", ConfigHB.string("api_venta_canalVenta1"));
		request.body("CanalVenta2", ConfigHB.string("api_venta_canalVenta2"));
		request.body("CanalVenta3", ConfigHB.string("api_venta_canalVenta3"));
		request.body("CanalVenta4", ConfigHB.string("api_venta_canalVenta4"));
		request.body("Oficina", "0");
		// request.body("Simulacion", false);
		// request.body("OrigenDocumentacion", "digital");
		// request.body("TieneLegajoFisico", false);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse ejecutarMotorResponse(ContextoHB contexto, String idSolicitud, Boolean solicitaComprobarIngresos, Boolean esPlanSueldo) {
		ApiRequest request = Api.request("VentasEvaluarSolicitud", "ventas_windows", "PUT", "/solicitudes/{SolicitudId}/resoluciones", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.body("TipoOperacion", "03");

		Boolean flagSolicitaAprobacionCentralizada = false;

		if (!ConfigHB.esProduccion()) {
			Set<String> cobisMock = Objeto.setOf("6824609", "803", "762", "3434960", "772245", "2976577", "5594861", "24077", "315321", "13439", "339902", "11379", "112675", "320116", "1027", "6848", "423132", "197699", "474613", "841518", "946033", "2485262", "1108832", "2531256", "3246448", "4031353", "647173", "5277303", "5710970", "1415107", "4688328", "" + "2832060", "5757757", "1772676", "3666138", "5876697", "895579", "4031353", "2806142", "4458806", "4474187", "3533391", "4052868", "4693090", "3411734", "4494305", "269289", "4184812", "4633983", "3780656", "4616895", "4619841", "4502759", "4083437", "4605953", "655481", "3969117", "36481", "4678980", "1560544", "4667279", "3934530", "4657715", "" + "1015123", "4517581", "151902", "4643212", "4343440", "306983", "1408386",
					"2803514", "1303691", "4340145", "4337807", "774661", "3115468", "1340067", "4744516", "879309", "1552843", "1127410", "2699672", "1124858", "1516963", "215261", "920263", "4486142", "4778332", "4344757", "1561490", "3830624", "4383314", "775175", "1505681", "4523393", "4768649", "4764558", "901495", "890174", "874189", "2392088", "4788465", "4805041", "5013889", "1373702", "2881738", "4351902", "808346", "4860492", "1761355", "774717", "1838563");
			// centralizada e ingresos AA con doc
			// !centralizada e ingresos AV con doc
			if (cobisMock.contains(contexto.idCobis())) {
				solicitaComprobarIngresos = true;
				flagSolicitaAprobacionCentralizada = true;
			}
		}

		request.body("FlagSolicitaAprobacionCentralizada", flagSolicitaAprobacionCentralizada);
		request.body("FlagSolicitaValidarIdentidad", false);
		request.body("FlagSolicitaComprobarIngresos", solicitaComprobarIngresos);
		request.body("FlagSolicitaAprobacionEstandard", false);
		request.body("FlagSolicitaExcepcion", false);
		request.body("FlagSolicitaEvaluarMercadoAbierto", false);
		request.body("EsPlanSueldo", esPlanSueldo);
		request.body("FlagSolicitaExcepcionCRM", false);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse finalizarSolicitud(ContextoHB contexto, String idSolicitud) {
		ApiRequest request = Api.request("VentasFinalizarSolicitud", "ventas_windows", "GET", "/solicitudes/{SolicitudId}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.query("estado", "finalizar");
		return Api.response(request, contexto.idCobis());
	}

	/* ========== INTEGRANTES ========== */
	public static ApiResponse generarIntegrante(ContextoHB contexto, String idSolicitud) {
		ApiRequest request = Api.request("VentasGenerarIntegrante", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/integrantes", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.body("Secuencia", 0);
		request.body("SolicitudId", idSolicitud);
		request.body("NumeroTributario", contexto.persona().cuit());
		request.body("TipoOperacion", "02");
		ApiResponse response = Api.response(request, contexto.idCobis());

		if (contexto.persona().idEstadoCivil().equals("C")) {
			String cuitConyuge = RestPersona.cuitConyugeSinFechaFinRelacion(contexto);
			if (cuitConyuge != null) {
				ApiRequest requestConyuge = Api.request("VentasGenerarIntegrante", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/integrantes", contexto);
				requestConyuge.headers.put("X-Handle", idSolicitud);
				requestConyuge.path("SolicitudId", idSolicitud);
				requestConyuge.body("NumeroTributario", Long.valueOf(cuitConyuge));
				requestConyuge.body("TipoOperacion", "03");
				Api.response(requestConyuge, contexto.idCobis());
			}
		}
		return response;
	}

	public static ApiResponse generarAumentoLimiteTC(ContextoHB contexto, String idSolicitud, String cuenta) {
		// String idCuenta = contexto.parametros.string("idCuenta");
		Objeto integrante = new Objeto();
		integrante.set("numeroDocumentoTributario", contexto.persona().cuit()); // 20942942495
		integrante.set("rol", "T");
		ApiRequest request = Api.request("VentasGenerarAumentoLimiteTC", "ventas_windows", "POST", "/solicitudes/{SolicitudId}/inclusionModificacion", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("SolicitudId", idSolicitud);
		request.body("Cuenta", cuenta);
		request.add("Integrantes", integrante);
		request.body("IdProductoFrontEnd", 17);
		request.body("TipoOperacion", "02"); // Se manda 02 para que no haga todas las validaciones
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse actualizarAumentoLimiteTC(ContextoHB contexto, String idSolicitud, String cuenta, SolicitudAumentoLimite solicitudAumentoLimite) {
		Objeto integrante = new Objeto();
		integrante.set("numeroDocumentoTributario", contexto.persona().cuit()); // 20942942495
		integrante.set("rol", "T");
		// Actualizar AUMENTO
		ApiRequest requestv2 = Api.request("VentasActualizarPaquete", "ventas_windows", "PUT", "/solicitudes/{idSolicitud}/inclusionModificacion/{inclusionModificacion}", contexto);
		requestv2.headers.put("X-Handle", idSolicitud);
		requestv2.path("idSolicitud", idSolicitud);
		requestv2.path("inclusionModificacion", solicitudAumentoLimite.Id);
		requestv2.body("Cuenta", cuenta);
		requestv2.body("montoOfrecido", solicitudAumentoLimite.MontoOfrecido);
		requestv2.body("montoAceptado", solicitudAumentoLimite.MontoAceptado);
		requestv2.add("Integrantes", integrante);
		requestv2.body("IdProductoFrontEnd", 17);
		requestv2.body("TipoOperacion", "03"); // Se manda 02 para que no haga todas las validaciones
		return Api.response(requestv2, contexto.idCobis());
	}

	public static ApiResponse obtenerConsultaAumento(ContextoHB contexto, String idSolicitud, String idSolicitudAumento) {
		ApiRequest request = Api.request("VentasConsultarSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}/inclusionModificacion/{inclusionModificacion}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("idSolicitud", idSolicitud);
		request.path("inclusionModificacion", idSolicitudAumento);
		return Api.response(request, idSolicitud);
	}

	public static ApiResponse consultarSolicitud(ContextoHB contexto, String idSolicitud) {
		ApiRequest request = Api.request("VentasConsultarSolicitud", "ventas_windows", "GET", "/solicitudes/{idSolicitud}", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("idSolicitud", idSolicitud);
		return Api.response(request, contexto.idCobis(), idSolicitud);
	}

	public static ApiResponse documentacionObligatoriaSolicitud(ContextoHB contexto, String idSolicitud) {
		ApiRequest request = Api.request("DocumentacionGET", "ventas_windows", "GET", "/solicitudes/{idSolicitud}/documentacion?solo-obligatorios=true&consultar-contenedor=false", contexto);
		request.headers.put("X-Handle", idSolicitud);
		request.path("idSolicitud", idSolicitud);
		return Api.response(request, contexto.idCobis(), idSolicitud);
	}

}
