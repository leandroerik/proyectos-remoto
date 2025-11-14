package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

public class RestSoftTokenService {

	public ApiResponse obtenerTokenIsva(ContextoHB contexto) {
		ApiRequest request = Api.request("TokenISVA", "seguridad", "POST", "/v1/token", contexto);
		request.body("username", contexto.idCobis());
		request.permitirSinLogin = true;

		return Api.response(request, contexto.idCobis());
	}

	public ApiResponse refrescarTokenISVA(ContextoHB contexto, String refreshToken) {
		ApiRequest request = Api.request("RefrescarTokenISVA", "seguridad", "GET", "/v1/refress", contexto);
		request.headers.put("refresh_token", refreshToken);
		request.headers.put("grant_type", "refresh_token");
		request.permitirSinLogin = true;

		return Api.response(request, contexto.idCobis());
	}

	public ApiResponse iniciarValidacionSoftToken(ContextoHB contexto, String accessToken) {
		ApiRequest request = Api.request("IniciarValidacionSoftToken", "seguridad", "GET", "/v1/softtoken", contexto);
		request.query("idcliente", contexto.idCobis());
		request.header("x-client_token", "Bearer " + accessToken);
		request.permitirSinLogin = true;

		return Api.response(request, contexto.idCobis(), accessToken);
	}

	public ApiResponse validarSoftToken(ContextoHB contexto, String cookie, String stateId, String otp) {
		ApiRequest request = Api.request("ValidarSoftToken", "seguridad", "POST", "/v1/softtoken", contexto);
		request.header("Cookie", cookie);
		request.query("stateId", stateId);
		request.body("idCliente", contexto.idCobis());
		request.body("otp", otp);
		request.permitirSinLogin = true;

		return Api.response(request, contexto.idCobis(), stateId, otp);
	}
}
