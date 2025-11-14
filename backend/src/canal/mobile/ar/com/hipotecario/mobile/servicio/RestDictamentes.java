package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class RestDictamentes {

	public static ApiResponseMB dictamenRiesgo(ContextoMB contexto, String solicitud) {
		ApiRequestMB request = ApiMB.request("DictamenRiesgo", "dictamenes", "GET", "/v1/solicitudes/{nroSolicitud}/dictamenes/riesgo", contexto);
		request.path("nroSolicitud", solicitud);
		request.header("Authorization", null);
		request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis(), solicitud);
	}

}
