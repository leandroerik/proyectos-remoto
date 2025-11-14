package ar.com.hipotecario.backend.servicio.api.link;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class TokenAFIP extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String token;
	public String firma;
	public String urlafip;
	public String accion;

	/* =============== SERVICIOS ================ */
	// API-Link_ObtenerTokenAfip-VEP
	public static TokenAFIP post(Contexto contexto, String cliente, String empresa, String numeroTarjetaDebito) {
		ApiRequest request = new ApiRequest("LinkPostTokenAfip", "link", "POST", "/v1/tokenAFIP", contexto);

		request.body("cliente", new Objeto().set("idTributario", cliente));
		request.body("empresa", new Objeto().set("idTributario", empresa));
		request.body("tarjetaDebito", new Objeto().set("numero", numeroTarjetaDebito));

		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(TokenAFIP.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		TokenAFIP datos = post(contexto, "20105176512", "20105176512", "4998590015392216");
		imprimirResultado(contexto, datos);
	}

}
