package ar.com.hipotecario.backend.servicio.api.MODO;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import java.util.List;

public class Usuarios extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String dni;
	public String email;
	public String verification_id;

	public String access_token;
	public String refresh_token;
	public String expires_in;
	public String token_type;

    public static class CuentaModo {
		public String cbu_number;
		public boolean is_favourite;

		public CuentaModo(String cbu) {
			this.cbu_number = cbu;
			this.is_favourite = false;
		}
	}

	public static class TarjetaModo {
		public FingerprintModo fingerprint;
		public PaymentMethodModo payment_method;
	}

	public static class FingerprintModo {
		public String emulador;
		public String language;
		public String time_stamp;
		public String device_model;
		public String os_version;
		public String os_name;
		public String hardware_id;
		public String ip;
		public GeolocalizationModo geolocalizationModo;
		//public String os_id;
		//public String device_name;
		//public String user_agent;
	}

	public static class GeolocalizationModo {
		public String latitude;
		public String longitude;
	}

	public static class PaymentMethodModo {
		public CreditCardModo credit_card;
	}

	public static class CreditCardModo {
		public String number;
		public String month;
		public String year;
		public String first_name;
		public String last_name;
	}

	/* ========== SERVICIOS ========== */
	public static Boolean checkUser(Contexto contexto, String idCobis, String telefono) {
		ApiRequest request = new ApiRequest("checkUserModo", ApiModo.API, "GET", "/v1/users/{phoneNumber}", contexto);
		request.header("x-subCanal", "BB-MODO");
		request.header("x-usuario", idCobis);
		request.path("phoneNumber", "+549" + telefono);

		ApiResponse response = request.ejecutar();
		if(!response.http(200)
				&& "UNAUTHORIZED".equals(response.string("codigo"))){
			//refrescar el token
			response = request.ejecutar();
			if(!response.http(200)){
				ApiException.throwIf(!response.http(200), request, response);
			}
		}

		return !response.body.contains("NOT_FOUND");
	}

	public static Usuarios post(Contexto contexto, String idCobis, String numeroDocumento, String nombres, String apellidos, String genero, String telefono, String email) {
		ApiRequest request = new ApiRequest("postUserModo", ApiModo.API, "POST", "/v1/users", contexto);
		request.header("x-subCanal", "BB-MODO");
		request.header("x-usuario", idCobis);

		request.body("flow", "NEW_USER");
		request.body("dni", numeroDocumento);
		request.body("first_name", nombres);
		request.body("last_name", apellidos);
		request.body("email", email);
		request.body("phone_number", "+549" + telefono);
		request.body("gender", genero);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Usuarios.class);
	}

	public static Usuarios postSimplificado(Contexto contexto, String idCobis, String numeroDocumento, String nombres, String apellidos, String genero, String telefono, String email) {
		ApiRequest request = new ApiRequest("postUserSimplificadoModo", ApiModo.API, "POST", "/v1/users/simplificado", contexto);
		request.header("x-subCanal", "BB-MODO");
		request.header("x-usuario", idCobis);

		request.body("flow", "NEW_USER");
		request.body("dni", numeroDocumento);
		request.body("first_name", nombres);
		request.body("last_name", apellidos);
		request.body("email", email);
		request.body("phone_number", "+549" + telefono);
		request.body("gender", genero);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Usuarios.class);
	}

	public static Usuarios confirmOtp(Contexto contexto, String idCobis, String numeroDocumento, String telefono, String id, String clave) {
		ApiRequest request = new ApiRequest("confirmOtpModo", ApiModo.API, "POST", "/v1/public/verifications/confirm", contexto);
		request.header("x-subCanal", "BB-MODO");
		request.header("x-usuario", idCobis);

		request.body("dni", numeroDocumento);
		request.body("phone_number", "+549" + telefono);
		request.body("verification_id", id);
		request.body("verification_code", clave);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		if(!response.body.contains("access_token") && !response.body.contains("refresh_token")){
			return null;
		}

		return response.crear(Usuarios.class);
	}

	public static Usuarios insertToken(Contexto contexto, String idCobis, String accessToken, String refreshToken, String expiresIn, String scope, String phoneNumber) {
		ApiRequest request = new ApiRequest("insertTokensModo", ApiModo.API, "POST", "/v1/modo/{id_cobis}", contexto);
		request.header("x-subCanal", "BB-MODO");
		request.header("x-usuario", idCobis);
		request.path("id_cobis", idCobis);

		request.body("id_cobis", idCobis);
		request.body("access_token", accessToken);
		request.body("refresh_token", refreshToken);
		request.body("expires_in", expiresIn);
		request.body("scope", scope);
		request.body("telefono", phoneNumber);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Usuarios.class);
	}

	public static boolean postCuentas(Contexto contexto, String idCobis, String numeroDocumento, String accessToken, List<CuentaModo> cuentas) {
		ApiRequest request = new ApiRequest("postCuentasModo", ApiModo.API, "POST", "/v1/public/accounts", contexto);
		request.header("x-subCanal", "BB-MODO");
		request.header("x-usuario", idCobis);
		request.header("DimoToken", "Bearer " + accessToken);
		request.body("id_cobis", idCobis);
		request.body("dni", numeroDocumento);

		Objeto accounts = new Objeto();
		for (CuentaModo c: cuentas){
			Objeto ca = new Objeto();
			ca.set("cbu_number", c.cbu_number);
			ca.set("is_favourite", c.is_favourite);
			accounts.add(ca);
		}
		request.body("accounts", accounts);

		ApiResponse response = request.ejecutar();
		return response.http(200);
	}

	public static boolean postTarjeta(Contexto contexto, String idCobis, String numeroDocumento, String accessToken, TarjetaModo tarjeta) {
		ApiRequest request = new ApiRequest("postTarjetaModo", ApiModo.API, "POST", "/v1/public/cards/onboarding/tarjetas", contexto);
		request.header("x-subCanal", "BB-MODO");
		request.header("x-usuario", idCobis);
		request.header("DimoToken", "Bearer " + accessToken);

		Objeto fingerprint = new Objeto();
		fingerprint.set("emulador", tarjeta.fingerprint.emulador);
		fingerprint.set("language", tarjeta.fingerprint.language);
		fingerprint.set("time_stamp", tarjeta.fingerprint.time_stamp);
		fingerprint.set("device_model", tarjeta.fingerprint.device_model);
		fingerprint.set("os_version", tarjeta.fingerprint.os_version);
		fingerprint.set("os_name", tarjeta.fingerprint.os_name);
		fingerprint.set("hardware_id", tarjeta.fingerprint.hardware_id);
		fingerprint.set("ip", tarjeta.fingerprint.ip);

		fingerprint.set("geolocalization.latitude", tarjeta.fingerprint.geolocalizationModo.latitude);
		fingerprint.set("geolocalization.longitude", tarjeta.fingerprint.geolocalizationModo.longitude);

		Objeto paymentMethod = new Objeto();
		paymentMethod.set("credit_card.first_name", tarjeta.payment_method.credit_card.first_name);
		paymentMethod.set("credit_card.last_name", tarjeta.payment_method.credit_card.last_name);
		paymentMethod.set("credit_card.month", tarjeta.payment_method.credit_card.month);
		paymentMethod.set("credit_card.year", tarjeta.payment_method.credit_card.year);
		paymentMethod.set("credit_card.number", tarjeta.payment_method.credit_card.number);

		request.body("fingerprint", fingerprint);
		request.body("payment_method", paymentMethod);
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.http(200);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		String test = "post";
		Contexto contexto = contexto("BB", "homologacion");

		if (test.equals("post")) {
			String numeroDocumento = "16000238";
			String nombres = "ESTHER ALICIA";
			String apellidos = "GUERRA";
			String email = "homologacionit@hipotecario.com.ar";
			String telefono = "1133491389";
			String genero = "F";
			String cobis = "8783032";
			String clave = "000000";

			Usuarios resPost = post(contexto, cobis, numeroDocumento, nombres, apellidos, genero, telefono, email);
			String id =resPost.verification_id;
			confirmOtp(contexto, cobis, numeroDocumento, telefono, id, clave);

		} else if (test.equals("post2")) {
			String numeroDocumento = "39012876";
			String nombres = "DANIEL RAMON";
			String apellidos = "QUINTEROS";
			String email = "homologacionit@hipotecario.com.ar";
			String telefono = "1148052398";
			String genero = "M";

			Usuarios resPost = post(contexto, "9041382", numeroDocumento, nombres, apellidos, genero, telefono, email);
		} else if(test.equals("check")) { //codigo ok
			Boolean datos = checkUser(contexto, "9041382", "1148052398");
			System.out.println(datos);
		} else if(test.equals("check2")) { // NOT_FOUND
			Boolean datos = checkUser(contexto, "6033373", "1133491389");
			System.out.println(datos);
		}

	}

}
