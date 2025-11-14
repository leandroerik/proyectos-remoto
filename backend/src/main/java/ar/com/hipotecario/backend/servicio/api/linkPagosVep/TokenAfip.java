package ar.com.hipotecario.backend.servicio.api.linkPagosVep;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class TokenAfip extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String token;
	public String firma;
	public String urlAfip;
	public String accion;

	/* ========== SERVICIOS ========== */
	public static TokenAfip post(Contexto contexto, String idTributarioCliente, String idTributarioEmpresa, String numeroTarjeta) {
		ApiRequest request = new ApiRequest("LinkPostTokenAfip", "veps", "POST", "/v1/tokenAFIP", contexto);
		request.body("cliente", new Objeto().set("idTributario", idTributarioCliente));
		request.body("empresa", new Objeto().set("idTributario", idTributarioEmpresa));
		request.body("tarjetaDebito", new Objeto().set("numero", numeroTarjeta));

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);

		TokenAfip tokenAfip = new TokenAfip();
		tokenAfip.token = response.string("token");
		tokenAfip.firma = response.string("firma");
		tokenAfip.urlAfip = response.string("urlAfip");
		tokenAfip.accion = response.string("accion");

		return tokenAfip;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String prueba = "post";
		if ("post".equals(prueba)) {
			Contexto contexto = contexto("HB", "homologacion");
			TokenAfip datos = post(contexto, "20000000087", "30000000015", "5046200441112559");
			imprimirResultado(contexto, datos);
		}
	}
}
