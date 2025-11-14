package ar.com.hipotecario.backend.servicio.api.seguridad;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class LoginHB extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */

	/* ========== SERVICIOS ========== */
	public static Boolean post(Contexto contexto, String usuario, String claveUsuario, String claveNumerica) {
		ApiRequest requestUsuario = new ApiRequest("ValidarUsuarioIDG", "seguridad", "GET", "/v1/clave", contexto);
		requestUsuario.query("grupo", contexto.canal);
		requestUsuario.query("idcliente", usuario);
		requestUsuario.query("clave", claveUsuario);

		ApiResponse responseUsuario = requestUsuario.ejecutar();
		ApiException.throwIf("USUARIO_INVALIDO", responseUsuario.contains("The password authentication failed"), requestUsuario, responseUsuario);
		ApiException.throwIf("USUARIO_BLOQUEADO", responseUsuario.contains("is now locked out"), requestUsuario, responseUsuario);
		ApiException.throwIf("USUARIO_BLOQUEADO", responseUsuario.contains("Maximum authentication attempts exceeded"), requestUsuario, responseUsuario);
		ApiException.throwIf("USUARIO_EXPIRADO", responseUsuario.contains("password has expired"), requestUsuario, responseUsuario);
		ApiException.throwIf(!responseUsuario.http(200) && !responseUsuario.http(202), requestUsuario, responseUsuario);

		ApiRequest requestClave = new ApiRequest("ValidarClaveIDG", "seguridad", "GET", "/v1/clave", contexto);
		requestClave.query("grupo", contexto.canal);
		requestClave.query("idcliente", usuario);
		requestClave.query("clave", claveNumerica);
		requestClave.query("nombreClave", "numerica");

		ApiResponse responseClave = requestClave.ejecutar();
		ApiException.throwIf("USUARIO_INVALIDO", responseClave.contains("The password authentication failed"), requestClave, responseClave);
		ApiException.throwIf("USUARIO_BLOQUEADO", responseClave.contains("is now locked out"), requestClave, responseClave);
		ApiException.throwIf("USUARIO_BLOQUEADO", responseClave.contains("Maximum authentication attempts exceeded"), requestClave, responseClave);
		ApiException.throwIf("CLAVE_EXPIRADA", responseClave.contains("password has expired"), requestClave, responseClave);
		ApiException.throwIf(!responseUsuario.http(200) && !responseUsuario.http(202), requestClave, responseClave);

		return true;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		Contexto contexto = contexto("ClientesBH", "homologacion");
		Boolean paso = post(contexto, "301729", "ElianaEli11", "1390");
		System.out.println(paso);
	}
}
