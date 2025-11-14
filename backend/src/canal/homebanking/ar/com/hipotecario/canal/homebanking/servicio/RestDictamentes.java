package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

public class RestDictamentes {

	public static ApiResponse dictamenRiesgo(ContextoHB contexto, String solicitud) {
		ApiRequest request = Api.request("DictamenRiesgo", "dictamenes", "GET", "/v1/solicitudes/{nroSolicitud}/dictamenes/riesgo", contexto);
		request.path("nroSolicitud", solicitud);
		request.header("Authorization", null);
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), solicitud);
	}

}
