package ar.com.hipotecario.backend.servicio.api.seguridad;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class ConsultaQRSoftToken extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String qr;

	/* ========== SERVICIOS ========== */
	// API-Seguridad_ConsultaQRSoftToken
	public static ConsultaQRSoftToken qr(Contexto contexto, String idCliente, String accesToken) {
		ApiRequest request = new ApiRequest("ConsultaQRSoftToken", "seguridad", "GET", "/v1/softtoken/qr", contexto);
		request.header("x-usuario", idCliente);
		request.header("x-client_token", "Bearer " + accesToken);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(ConsultaQRSoftToken.class, response);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		Contexto contexto = contexto("OB", "desarrollo");
		TokenISVA token = TokenISVA.post(contexto);
		ConsultaQRSoftToken consulta = qr(contexto, "133366", token.access_token);
		imprimirResultado(contexto, consulta);
	}
}