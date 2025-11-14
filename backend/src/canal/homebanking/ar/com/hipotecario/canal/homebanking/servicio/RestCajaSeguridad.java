package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

public class RestCajaSeguridad {

	/* ========== SERVICIOS ========== */
	public static ApiResponse detalle(ContextoHB contexto, String numero) {
		ApiRequest request = Api.request("CajaSeguridad", "cajasseguridad", "GET", "/v1/cajasseguridad/{idcajaseguridad}", contexto);
		request.path("idcajaseguridad", numero);
		return Api.response(request, contexto.idCobis(), numero);
	}
}
