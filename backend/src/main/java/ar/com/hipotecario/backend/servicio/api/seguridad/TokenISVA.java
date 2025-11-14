package ar.com.hipotecario.backend.servicio.api.seguridad;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class TokenISVA extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String access_token;
	public String refresh_token;
	public String token_type;
	public String expires_in;

	/* ========== SERVICIOS ========== */
	// API-Seguridad_ConsultaTokenISVA
	public static TokenISVA post(Contexto contexto) {
		ApiRequest request = new ApiRequest("TokenISVA", "seguridad", "POST", "/v1/token", contexto);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("USUARIO_INVALIDO", response.contains("Usuario Invalido"), request, response);
		ApiException.throwIf(!response.http(200), request, response);

		return response.crear(TokenISVA.class, response);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		Contexto contexto = contexto("OB", "desarrollo");
		TokenISVA datos = post(contexto);
		imprimirResultado(contexto, datos);
	}

}