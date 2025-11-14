package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class RestProcesos {

	public static ApiResponseMB getEstadoSolicitudes(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("EstadoSolicitudBPM", "procesos", "GET", "/procesos-de-negocio/v1/solicitudes", contexto);
		request.query("pNroDoc", contexto.persona().numeroDocumento());
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), contexto.persona().numeroDocumento());
	}

	public static ApiResponseMB detalleProcesoSolicitud(ContextoMB contexto, String solicitud) {
		ApiRequestMB request = ApiMB.request("DetalleProcesoSolicitud", "procesos", "GET", "/procesos-de-negocio/v1/solicitudes", contexto);
		request.query("pNroSolicitud", solicitud);
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), solicitud);
	}

}
