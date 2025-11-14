package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class TokenFVNetOB extends ApiObjeto{
	
		public String token;

	
	public static TokenFVNetOB get(Contexto contexto, String idCobis) {
		ApiRequest request = new ApiRequest("SeguridadGetVfNet", "seguridad", "GET", "/v1/tokenvfnet", contexto);
		request.query("idCliente", idCobis);
		request.cache = false;
		
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		TokenFVNetOB r = response.crear(TokenFVNetOB.class);
		return r;
	}
}
