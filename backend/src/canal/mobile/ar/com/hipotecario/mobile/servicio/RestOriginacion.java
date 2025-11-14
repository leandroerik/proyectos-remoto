package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class RestOriginacion {

	public static ApiResponseMB consultarReclamosDocumentacion(ContextoMB contexto, String solicitud) {
		ApiRequestMB request = ApiMB.request("ReclamoDocumentacionActivo", "originacion", "GET", "/v1/solicitudes/{solcitud}/reclamos/documentacion/activo", contexto);
		request.path("solcitud", solicitud);
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), solicitud);
	}
}
