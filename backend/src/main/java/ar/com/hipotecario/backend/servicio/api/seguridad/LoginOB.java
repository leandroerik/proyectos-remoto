package ar.com.hipotecario.backend.servicio.api.seguridad;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Encriptador;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class LoginOB extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String usu_codigo;
	public String mensaje_ret;
	public String usu_login;
	public String cod_ret;
	public String rol_id;

	/* ========== SERVICIOS ========== */
	// API-Seguridad_LoginOB
	public static LoginOB post(Contexto contexto, String numeroDocumento, String usuario, String clave) {
		String usuarioEncriptado = Config.encriptarAES(usuario);
		String claveEncriptada = Encriptador.sha512(clave);

		ApiRequest request = new ApiRequest("LoginOB", "seguridad", "POST", "/v1/loginob", contexto);
		request.body("ip", contexto.ip());
		request.body("usu_nro_documento", numeroDocumento);
		request.body("usu_login", usuarioEncriptado);
		request.body("usu_clave", claveEncriptada);
		
		ApiResponse response = request.ejecutar();
		ApiException.throwIf("USUARIO_INVALIDO", response.contains("Usuario Inexistente"), request, response);
		ApiException.throwIf("USUARIO_INVALIDO", response.contains("No existe usuario en Empresa"), request, response);
		ApiException.throwIf("USUARIO_BLOQUEADO", response.contains("Usuario Bloqueado"), request, response);
		ApiException.throwIf("CLAVE_INVALIDA", response.contains("Password incorrecta"), request, response);
		ApiException.throwIf("CLAVE_EXPIRADA", response.contains("Password expirada"), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(LoginOB.class, response);
	}

	/* ========== METODOS ========== */
	public String rol() {
		String rol = "";
		rol = "1".equals(rol_id) ? "OPERADOR" : rol;
		rol = "2".equals(rol_id) ? "OPERADOR_INICIAL" : rol;
		return rol;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		Contexto contexto = contexto("OB", "desarrollo");
		LoginOB datos = post(contexto, "40059876", "Emiliano11", "Banco1234");
		imprimirResultado(contexto, datos);
	}
}
