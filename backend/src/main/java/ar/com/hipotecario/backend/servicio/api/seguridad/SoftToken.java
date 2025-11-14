package ar.com.hipotecario.backend.servicio.api.seguridad;

import java.util.Scanner;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;

public class SoftToken extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String stateId;
	public String cookie;
	public String state;

	/* ========== SERVICIOS ========== */
	// API-Seguridad_AltaSoftTokenISVA
	public static SoftToken get(Contexto contexto, String idCliente) {
		ApiRequest request = new ApiRequest("GenerarSoftToken", "seguridad", "GET", "/v1/softtoken", contexto);
		request.query("idcliente", idCliente);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		SoftToken generarToken = response.crear(SoftToken.class, response);
		generarToken.cookie = response.headers.get("set-cookie");
		return generarToken;
	}

	public static SoftToken post(Contexto contexto, String idCliente, String stateId, String cookie, String otp) {
		ApiRequest request = new ApiRequest("ValidarSoftToken", "seguridad", "POST", "/v1/softtoken", contexto);
		request.query("stateId", stateId);
		request.body("otp", otp);
		request.body("idCliente", idCliente);
		request.header("Cookie", cookie);

		ApiResponse response = request.ejecutar();

		LogOB.evento((ContextoOB) contexto, "SoftToken_post", new Objeto().set("idCliente", idCliente).set("reponse", response.toString()));

		ApiException.throwIf(!response.http(202, 403), request, response);
		SoftToken token = response.crear(SoftToken.class, response);

		return token;

	}

	public static ApiObjeto delete(Contexto contexto, String idCliente, String accesToken) {
		ApiRequest request = new ApiRequest("BajaSoftToken", "seguridad", "DELETE", "/v1/softtoken", contexto);
		request.query("idcliente", idCliente);
		request.header("x-client_token", "Bearer " + accesToken);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(ApiObjeto.class, response);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		String test = "post";
		if (test.equals("get")) {
			Contexto contexto = contexto("OB", "desarrollo");
			SoftToken datos = get(contexto, "133366");
			imprimirResultado(contexto, datos);
		}
		if (test.equals("post")) {
			String idCliente = "133366";
			Contexto contexto = contexto("OB", "desarrollo");
			SoftToken softToken = SoftToken.get(contexto, idCliente);
			System.out.print("TOKEN: ");
			try (Scanner scanner = new Scanner(System.in)) {
				String otp = scanner.nextLine().trim();
				ApiObjeto datos = post(contexto, idCliente, softToken.stateId, softToken.cookie, otp);
				imprimirResultado(contexto, datos);
			}
		}
		if (test.equals("delete")) {
			Contexto contexto = contexto("OB", "desarrollo");
			ApiObjeto datos = delete(contexto, "4594725", "LO0oik11hymKzBQ7f7Dy");
			imprimirResultado(contexto, datos);
		}
	}
}