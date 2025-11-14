package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

public class RestOriginacion {

	public static ApiResponse consultarReclamosDocumentacion(ContextoHB contexto, String solicitud) {
		ApiRequest request = Api.request("ReclamoDocumentacionActivo", "originacion", "GET", "/v1/solicitudes/{solcitud}/reclamos/documentacion/activo", contexto);
		request.path("solcitud", solicitud);
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), solicitud);
	}
}
