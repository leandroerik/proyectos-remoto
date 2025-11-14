package ar.com.hipotecario.backend.servicio.api.seguridad;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Encriptador;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class CambioClaveOB extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String mensaje_ret;
	public String cod_ret;

	/* ========== SERVICIOS ========== */
	// API-Seguridad_CambiarClaveOB
	public static CambioClaveOB post(Contexto contexto, String numeroDocumento, String usuario, String clave, String claveAnterior) {

		String claveAnteriorEncriptada = Encriptador.sha512(claveAnterior);
		String claveEncriptada = Encriptador.sha512(clave);

		ApiRequest request = new ApiRequest("CambioClaveOB", "seguridad", "POST", "/v1/cambiarclaveob", contexto);
		request.body("ip", contexto.ip());
		request.body("usu_nro_documento", numeroDocumento);
		request.body("usu_login", usuario);
		request.body("usu_clave", claveEncriptada);
		request.body("usu_clave_ant", claveAnteriorEncriptada);
		request.body("clu_version_enc", "SHA-512");

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("USUARIO_INVALIDO", response.contains("Usuario Inexistente"), request, response);
		ApiException.throwIf("USUARIO_INVALIDO", response.contains("No existe usuario en Empresa"), request, response);
		ApiException.throwIf("CLAVE_INVALIDA", response.contains("Password ingresada no es correcta"), request, response);
		ApiException.throwIf("UTILIZADA_ANTERIORMENTE", response.contains("Password nueva utilizada anteriormente"), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(CambioClaveOB.class, response);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		Contexto contexto = contexto("OB", "desarrollo");
		CambioClaveOB datos = post(contexto, "95904456", "Julieta3", "Julieta3434", "Julieta5050");
		imprimirResultado(contexto, datos);
	}
}
