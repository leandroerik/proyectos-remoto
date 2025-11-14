package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class RestCRM {

	public static ApiResponseMB segmentacionCliente(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("SegmentacionCliente", "CRM_Windows", "GET", "/segmentacioncliente/get", contexto);
		request.query("cuil", contexto.persona().cuit());
		request.cacheSesion = true;
		ApiResponseMB response = ApiMB.response(request);
		return response;
	}
}
