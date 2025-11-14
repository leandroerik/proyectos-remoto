package ar.com.hipotecario.backend.servicio.api.seguridad;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Encriptador;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class MigrarUsuarioOB extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String mensaje_ret;
	public String cod_ret;

	/* ========== SERVICIOS ========== */
	// API-Seguridad_MigracionUsuarioOB
	public static MigrarUsuarioOB post(Contexto contexto, String cuitAnterior, String usuarioAnterior, String numeroDocumento, String usuario, String clave, String cuits, String email) {

		ApiRequest request = new ApiRequest("MigrarUsuarioOB", "seguridad", "POST", "/v1/migrausuarioob", contexto);
		String usuarioEncriptado = Config.encriptarAES(usuario);
		String claveEncriptada = Encriptador.sha512(clave);

		request.body("ip", contexto.ip());
		request.body("emp_cuit", cuitAnterior);
		request.body("usu_login_anterior", usuarioAnterior);
		request.body("usu_documento", numeroDocumento);
		request.body("usu_login", usuarioEncriptado);
		request.body("usu_clave", claveEncriptada);
		request.body("cuits", cuits);
		request.body("email", email);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("EMPRESA_INVALIDA", response.contains("No se pudo validar la empresa"), request, response);
		ApiException.throwIf("USUARIO_EXISTENTE", response.contains("El usuario ya fue migrado"), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(MigrarUsuarioOB.class, response);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		Contexto contexto = contexto("OB", "desarrollo");
		MigrarUsuarioOB datos = post(contexto, "30693792335", "operador01", "34670668", "Julian1234", "Banco1234", null, "Jose@hipotecario.com");
		imprimirResultado(contexto, datos);
	}
}