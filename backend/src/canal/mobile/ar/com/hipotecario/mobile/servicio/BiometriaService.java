package ar.com.hipotecario.mobile.servicio;

import java.net.URLEncoder;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.api.MBAplicacion;
import ar.com.hipotecario.mobile.api.MBBiometria.IsvaDTO;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;

public class BiometriaService {

	public static ApiResponseMB generaTokensAutenticador(ContextoMB contexto) {
		String deviceName = contexto.parametros.string("device_name");
		String deviceType = contexto.parametros.string("device_type");
		Boolean fingerprintSupport = contexto.parametros.bool("fingerprint_support");
		String osVersion = contexto.parametros.string("os_version");
		String pushToken = contexto.parametros.string("push_token");
		String tenantId = contexto.parametros.string("tenant_id", "");

		ApiRequestMB request = ApiMB.request("GeneraTokensAutenticador", "seguridad", "POST", "/v1/tokenautenticador", contexto);
		request.body("device_name", deviceName);
		request.body("device_type", deviceType);
		request.body("fingerprint_support", fingerprintSupport);
		request.body("os_version", osVersion);
		request.body("push_token", pushToken);
		request.body("tenant_id", tenantId);
		return ApiMB.response(request, deviceName);
	}

	public static ApiResponseMB generaRefreshTokens(ContextoMB contexto, String refreshToken) {
		ApiRequestMB request = ApiMB.request("GeneraAccessTokens", "seguridad", "GET", "/v1/refress", contexto);
		request.permitirSinLogin = true;
		request.headers.put("refresh_token", refreshToken);
		request.headers.put("grant_type", "refresh_token");
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB enrolaBiometria(ContextoMB contexto, String metodo, String dispositivo, String token, String publicKey) {
		ApiRequestMB request = ApiMB.request("EnrolaBiometria", "seguridad", "PATCH", "/v1/autenticacion/enrolar", contexto);
		request.query("metodo", metodo);
		Objeto objAutenticador = new Objeto();
		objAutenticador.set("algorithm", "SHA512withRSA");
		objAutenticador.set("enabled", true);
		objAutenticador.set("keyHandle", dispositivo);
		objAutenticador.set("publicKey", publicKey);
		request.body(objAutenticador);
		request.headers.put("Authorization", "Bearer " + token);
		request.headers.put("Content-Type", "application/scim+json");
		return ApiMB.response(request, contexto.idCobis(), dispositivo);
	}

	public static ApiResponseMB revocaBiometria(ContextoMB contexto, String metodo, String token, String dispositivo, String publicKey) {
		ApiRequestMB request = ApiMB.request("RevocaBiometria", "seguridad", "PATCH", "/v1/autenticacion/revocar", contexto);
		if (metodo.equalsIgnoreCase("todo")) {
			request = ApiMB.request("RevocaBiometria", "seguridad", "PATCH", "/v1/tokenautenticador/revocar", contexto);
		}

		request.query("metodo", metodo);
		Objeto objAutenticador = new Objeto();
		objAutenticador.set("algorithm", "SHA512withRSA");
		objAutenticador.set("enabled", true);
		objAutenticador.set("keyHandle", dispositivo == null ? "" : dispositivo);
		objAutenticador.set("publicKey", publicKey);
		request.body(objAutenticador);
		request.headers.put("Authorization", "Bearer " + token);
		request.headers.put("Content-Type", "application/scim+json");
		return ApiMB.response(request, contexto.idCobis(), dispositivo);
	}

	public static ApiResponseMB consultaUsuario(ContextoMB contexto, String token) {
		ApiRequestMB request = ApiMB.request("ConsultarUsuarioIsva", "seguridad", "GET", "/v1/autenticacion", contexto);
		request.headers.put("Authorization", "Bearer " + token);
		request.headers.put("Content-Type", "application/scim+json");
		request.permitirSinLogin = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB inciaTransaccion(ContextoMB contexto, String metodo, String fingerprint) {
		ApiRequestMB request = ApiMB.request("iniciaTransaccionIsva", "seguridad", "GET", "/v1/transaccion", contexto);
		request.header("x-fingerprint", fingerprint);
		request.query("idcliente", contexto.idCobis());
		request.query("metodo", metodo);
		request.permitirSinLogin = true;
		return ApiMB.response(request, contexto.idCobis(), metodo);
	}

	public static ApiResponseMB seteaTransaccion(ContextoMB contexto, String dispositivo, String stateId, IsvaDTO isvaDTO) {
		ApiRequestMB request = ApiMB.request("seteaTransaccionIsva", "seguridad", "GET", "/v1/asociacion/dispositivo", contexto);
		request.query("idDispositivo", isvaDTO.dispositivo);
		request.permitirSinLogin = true;

		if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_otp_state_id")) {
			try {
				request.query("stateId", URLEncoder.encode(isvaDTO.stateId, "UTF-8"));
			} catch (Exception e) {
				request.query("stateId", isvaDTO.stateId);
			}
			request.header("cookie", isvaDTO.cookie);
		}
		return ApiMB.response(request, dispositivo);
	}

	public static ApiResponseMB trasaccionPendiente(ContextoMB contexto, String token) {
		ApiRequestMB request = ApiMB.request("validaTransaccionIsva", "seguridad", "GET", "/v1/transacciones/pendientes", contexto);
		request.headers.put("Authorization", "Bearer " + token);
		request.headers.put("Content-Type", "application/json");
		request.permitirSinLogin = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB seteaTransaccionCompletada(ContextoMB contexto, String dispositivo, String token, String transactionId, String pushToken) {
		ApiRequestMB request = ApiMB.request("transaccionCompletadaIsva", "seguridad", "PUT", "/v1/transacciones/completadas", contexto);
		request.query("idTransaccion", transactionId);
		request.headers.put("Authorization", "Bearer " + token);
		request.headers.put("Content-Type", "application/json");
		request.headers.put("push_token", pushToken);
		request.permitirSinLogin = true;
		return ApiMB.response(request, dispositivo);
	}

	public static ApiResponseMB revocaBiometriaLogin(ContextoMB contexto, String metodo, String publicKey, String token, String dispositivo) {
		ApiRequestMB request = ApiMB.request("RevocaBiometria", "seguridad", "PATCH", "/v1/tokenautenticador/revocar", contexto);

		request.query("metodo", metodo);
		Objeto objAutenticador = new Objeto();
		objAutenticador.set("algorithm", "SHA512withRSA");
		objAutenticador.set("enabled", true);
		objAutenticador.set("keyHandle", dispositivo);
		objAutenticador.set("publicKey", publicKey);
		request.body(objAutenticador);

		request.headers.put("Authorization", "Bearer " + token);
		request.headers.put("Content-Type", "application/scim+json");
		request.permitirSinLogin = true;
		return ApiMB.response(request, contexto.idCobis(), dispositivo);
	}

}