package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class RestSoftTokenService {

	/**
	 * Obtiene el access token para operar con API Seguridad.
	 *
	 * @param contexto Contexto.
	 * @return ApiResponse
	 */
	public ApiResponseMB obtainTokenIsva(ContextoMB contexto, String idCobis) {
		ApiRequestMB request = ApiMB.request("TokenISVA", "seguridad", "POST", "/v1/token", contexto);
		request.body("username", idCobis);
		if (contexto.idCobis() == null)
			request.permitirSinLogin = true;
		return ApiMB.response(request, idCobis);
	}

	/**
	 * Obtiene la información de la semilla para generar el Soft Token (código OTP
	 * de 6 dígitos).
	 *
	 * @param contexto    Contexto.
	 * @param idCliente   Id cobis.
	 * @param accessToken Access Token de ISVA.
	 * @return ApiResponse
	 */
	public ApiResponseMB obtainSeedInformation(ContextoMB contexto, String idCliente, String accessToken) {
		ApiRequestMB request = ApiMB.request("ConsultaSeedSoftToken", "seguridad", "GET", "/v1/softtoken/datos", contexto);
		request.header("x-usuario", idCliente);
		request.header("x-client_token", "Bearer " + accessToken);
		if (contexto.idCobis() == null)
			request.permitirSinLogin = true;
		return ApiMB.response(request, idCliente, accessToken);
	}

	/**
	 * Genera un nuevo access token a partir del refresh token.
	 *
	 * @param contexto     Contexto.
	 * @param refreshToken Refresh Token conseguido en el método obtainTokenIsva y
	 *                     almacenado en la tabla soft_token_alta.
	 * @return ApiResponse
	 */
	public ApiResponseMB refrescarTokenISVA(ContextoMB contexto, String refreshToken, String idCobis) {
		ApiRequestMB request = ApiMB.request("RefrescarTokenISVA", "seguridad", "GET", "/v1/refress", contexto);
		request.headers.put("refresh_token", refreshToken);
		request.headers.put("grant_type", "refresh_token");
		if (contexto.idCobis() == null)
			request.permitirSinLogin = true;
		return ApiMB.response(request, idCobis);
	}

	/**
	 * Inicia el proceso de validación de soft token. Se obtiene el stateId
	 * necesario para la validación del código.
	 *
	 * @param contexto    Contexto.
	 * @param accessToken Access Token de ISVA.
	 * @return ApiResponse
	 */
	public ApiResponseMB iniciarValidacionSoftToken(ContextoMB contexto, String accessToken) {
		ApiRequestMB request = ApiMB.request("IniciarValidacionSoftToken", "seguridad", "GET", "/v1/softtoken", contexto);
		request.query("idcliente", contexto.idCobis());
		request.header("x-client_token", "Bearer " + accessToken);

		return ApiMB.response(request, contexto.idCobis(), accessToken);
	}

	/**
	 * Valida el código soft token (código otp de 6 dígitos).
	 *
	 * @param contexto Contexto.
	 * @param cookie   Cabecera cookie.
	 * @param stateId  State id obtenido en el método iniciarValidacionSoftToken.
	 * @param otp      Código soft token de 6 dígitos.
	 * @return ApiResponse
	 */
	public ApiResponseMB validarSoftToken(ContextoMB contexto, String cookie, String stateId, String otp) {
		ApiRequestMB request = ApiMB.request("ValidarSoftToken", "seguridad", "POST", "/v1/softtoken", contexto);
		request.header("Cookie", cookie);
		request.query("stateId", stateId);
		request.body("idCliente", contexto.idCobis());
		request.body("otp", otp);

		return ApiMB.response(request, contexto.idCobis(), stateId, otp);
	}

}
