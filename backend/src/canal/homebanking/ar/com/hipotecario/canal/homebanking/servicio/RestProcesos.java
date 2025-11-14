package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

public class RestProcesos {

	public static ApiResponse getEstadoSolicitudes(ContextoHB contexto) {
		ApiRequest request = Api.request("EstadoSolicitudBPM", "procesos", "GET", "/procesos-de-negocio/v1/solicitudes", contexto);
		request.query("pNroDoc", contexto.persona().numeroDocumento());
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), contexto.persona().numeroDocumento());
	}

	public static ApiResponse recepcionReclamoDocumentacion(ContextoHB contexto, String solicitud, String claseDocumental) {
		ApiRequest request = Api.request("postRecepcionReclamoDocumentacion", "procesos", "POST", "/procesos-de-negocio/v1/solicitudes/{idSolicitud}/reclamos/documentacion/notificacion", contexto);
		request.path("idSolicitud", solicitud);
		request.body("idSolicitud", solicitud);
		request.body("dni", contexto.persona().numeroDocumento());
		request.body("claseDocumental", claseDocumental);
		try {
			Api.eliminarCache(contexto, "EstadoSolicitudBPM", contexto.idCobis(), contexto.persona().numeroDocumento());
		} catch (Exception e) {
			// TODO: handle exception
		}

		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse detalleProcesoSolicitud(ContextoHB contexto, String solicitud) {
		ApiRequest request = Api.request("DetalleProcesoSolicitud", "procesos", "GET", "/procesos-de-negocio/v1/solicitudes", contexto);
		request.query("pNroSolicitud", solicitud);
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), solicitud);
	}

}
