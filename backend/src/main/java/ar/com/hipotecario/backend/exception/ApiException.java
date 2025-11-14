package ar.com.hipotecario.backend.exception;

import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;

public class ApiException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/* ========== ATRIBUTOS ========== */
	public ApiRequest request = null;
	public ApiResponse response = null;
	public String codigoError = "ERROR";

	/* ========== CONSTRUCTORES ========== */
	public ApiException(ApiRequest request, ApiResponse response) {
		super("ERROR");
		this.request = request;
		this.response = response;
	}

	public ApiException(ApiRequest request, ApiResponse response, String codigoError) {
		super(codigoError);
		this.request = request;
		this.response = response;
		this.codigoError = codigoError;
	}

	/* ========== METODOS ========== */
	public static void throwIf(Boolean condicion, ApiRequest request, ApiResponse response) {
		if (condicion) {
			Api.eliminarCache(request.contexto, request.servicio, request.parametrosCache);
			throw new ApiException(request, response);
		}
	}

	public static void throwIf(String codigoError, Boolean condicion, ApiRequest request, ApiResponse response) {
		if (condicion) {
			Api.eliminarCache(request.contexto, request.servicio, request.parametrosCache);
			throw new ApiException(request, response, codigoError);
		}
	}
}
