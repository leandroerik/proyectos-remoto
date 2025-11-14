package ar.com.hipotecario.backend.servicio.api.seguridad;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Encriptador;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class LoginOBAnterior extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String usu_codigo;
	public String mensaje_ret;
	public String usu_login;
	public String emp_cuit;
	public String cod_ret;
	public String rol_id;

	/* ========== SERVICIOS ========== */
	// API-Seguridad_LoginValidacionOB
	public static LoginOBAnterior post(Contexto contexto, Long cuit, String usuario, String clave) {

		String claveEncriptada = Encriptador.sha512(clave);

		ApiRequest request = new ApiRequest("LoginOBAnterior", "seguridad", "POST", "/v1/login", contexto);
		request.body("ip", contexto.ip());
		request.body("emp_cuit", cuit);
		request.body("usu_login", usuario);
		request.body("usu_clave", claveEncriptada);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("ERROR", response.contains("Usuario Inexistente"), request, response);
		ApiException.throwIf("ERROR", response.contains("No existe usuario en Empresa"), request, response);
		ApiException.throwIf("USUARIO_BLOQUEADO", response.contains("Usuario Bloqueado"), request, response);
		ApiException.throwIf("ERROR", response.contains("Password incorrecta"), request, response);
		ApiException.throwIf("PASSWORD_EXPIRADA", response.contains("Password expirada"), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(LoginOBAnterior.class, response);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		Contexto contexto = contexto("OB", "desarrollo");
		LoginOBAnterior datos = post(contexto, Long.valueOf("30527677331"), "Febrero04", "40000004");
		imprimirResultado(contexto, datos);
	}
}
