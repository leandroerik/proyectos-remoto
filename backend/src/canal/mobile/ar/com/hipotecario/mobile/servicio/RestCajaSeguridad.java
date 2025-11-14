package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class RestCajaSeguridad {

	/* ========== SERVICIOS ========== */
	public static ApiResponseMB detalle(ContextoMB contexto, String numero) {
		ApiRequestMB request = ApiMB.request("CajaSeguridad", "cajasseguridad", "GET", "/v1/cajasseguridad/{idcajaseguridad}", contexto);
		request.path("idcajaseguridad", numero);
		return ApiMB.response(request, contexto.idCobis(), numero);
	}
}
