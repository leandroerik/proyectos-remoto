package ar.com.hipotecario.backend.servicio.api.link;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class Verificaciones extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public Boolean adherido;
	public String userId;

	/* =============== SERVICIOS ================ */
	// API-Link_ValidarLink
	public static Verificaciones post(Contexto contexto, String cardId, String pin) {
		ApiRequest request = new ApiRequest("LinkPostVerificacion", "link", "POST", "/v1/verificacion", contexto);

		request.body("cardId", cardId);
		request.body("pin", pin);

		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Verificaciones.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Verificaciones datos = post(contexto, "4998590018999504", "000000");
		imprimirResultado(contexto, datos);
	}

}
