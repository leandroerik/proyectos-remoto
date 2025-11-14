package ar.com.hipotecario.backend.servicio.api.seguridad;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;

public class OTP extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String idcliente;
	public String clave;
	public String stateId;
	public String cookie;

	/* ========== SERVICIOS ========== */
	// API-Seguridad_AltaClaveUnicoUsoIDG
	public static OTP generar(Contexto contexto, String idCliente) {
		ApiRequest request = new ApiRequest("GenerarOTP", "seguridad", "GET", "/v1/clave/otp", contexto);
		request.query("grupo", contexto.canal);
		request.query("idcliente", idCliente);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200) || response.objetos("otp").isEmpty(), request, response);
		OTP generarOTP = response.crear(OTP.class, response.objetos("otp").get(0));
		generarOTP.cookie = response.headers.get("set-cookie");
		return generarOTP;
	}

	public static ApiObjeto validar(Contexto contexto, String idCliente, String clave, String stateId, String cookie) {
		ApiRequest request = new ApiRequest("ValidarOTP", "seguridad", "GET", "/v1/clave", contexto);
		request.query("grupo", contexto.canal);
		request.query("idcliente", idCliente);
		request.query("clave", clave);
		request.query("nombreClave", "OTP");
		request.query("stateId", stateId);
		request.header("Cookie", cookie);

		ApiResponse response = request.ejecutar();

		LogOB.evento((ContextoOB) contexto, "OTP_validar", new Objeto().set("idCliente", idCliente).set("reponse", response.toString()));

		ApiException.throwIf("TOKEN_NO_GENERADO", response.contains("invalid state ID"), request, response);
		ApiException.throwIf("USUARIO_BLOQUEADO", response.contains("El usuario se encuentra bloqueado"), request, response);
		ApiException.throwIf("TIEMPO_AGOTADO", response.contains("El tiempo de espera fue agotado"), request, response);
		ApiException.throwIf("TOKEN_INVALIDO", response.contains("La clave ingresada es invalida"), request, response);
		ApiException.throwIf(!response.http(200, 202), request, response);
		return response.crear(ApiObjeto.class, response);
	}

	public static boolean validarOtp(Contexto contexto, String idCliente, String clave, String stateId, String cookie) {
		ApiRequest request = new ApiRequest("ValidarOTP", "seguridad", "GET", "/v1/clave", contexto);
		request.query("grupo", contexto.canal);
		request.query("idcliente", idCliente);
		request.query("clave", clave);
		request.query("nombreClave", "OTP");
		request.query("stateId", stateId);
		request.header("Cookie", cookie);

		ApiResponse response = request.ejecutar();
		return response != null && response.http(200, 202);
	}
	
	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		String test = "validar";
		if (test.equals("generar")) {
			Contexto contexto = contexto("OB", "desarrollo");
			OTP datos = generar(contexto, "133366");
			imprimirResultado(contexto, datos);
		}
		if (test.equals("validar")) {
			Contexto contexto = contexto("OB", "desarrollo");
			OTP otp = generar(contexto, "3313096");
			ApiObjeto datos = validar(contexto, "3313096", "2312", otp.stateId, otp.cookie);
			imprimirResultado(contexto, datos);
		}
	}

}