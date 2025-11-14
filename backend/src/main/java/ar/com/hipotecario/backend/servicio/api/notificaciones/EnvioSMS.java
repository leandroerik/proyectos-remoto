package ar.com.hipotecario.backend.servicio.api.notificaciones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class EnvioSMS extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String texto;
	public Transaccion transaccion;

	public static class Transaccion extends ApiObjeto {
		public String estado;
		public Fecha fecha;
		public String idTran;
	}

	/* ========== SERVICIOS ========== */
	// API-Notificaciones_SendSMS
	public static EnvioSMS post(Contexto contexto, String telefono, String mensaje) {
		ApiRequest request = new ApiRequest("EnvioSMS", "notificaciones", "POST", "/v1/notificaciones/sms", contexto);
		request.query("tipo", "SMS");
		request.body("telefono", telefono);
		request.body("mensaje", mensaje);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(EnvioSMS.class, response);
	}

	// API-Notificaciones_SendSMS
	public static EnvioSMS postOTP(Contexto contexto, String telefono, String codigo) {
		ApiRequest request = new ApiRequest("EnvioSMS", "notificaciones", "POST", "/v1/notificaciones/sms", contexto);
		request.query("tipo", "TPO");
		request.body("telefono", telefono);
		request.body("codigo", codigo);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(EnvioSMS.class, response);
	}
	
	public static EnvioSMS postOTPv1(Contexto contexto, String telefono, String codigo, String mensaje) {
		ApiRequest request = new ApiRequest("EnvioSMS", "notificaciones", "POST", "/v1/notificaciones/sms", contexto);
		request.query("tipo", mensaje != null ? "MB" : "TPO");
		request.body("telefono", telefono);
		request.body("codigo", codigo);
		if(mensaje != null) {
			request.body("mensaje", mensaje);
		}

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(EnvioSMS.class, response);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "desarrollo");
		EnvioSMS datos = postOTP(contexto, "1169235102", "1234");
		imprimirResultado(contexto, datos);
	}
}
