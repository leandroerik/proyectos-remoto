package ar.com.hipotecario.canal.homebanking.excepcion;

import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

public class ApiException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ApiException(Exception e) {
		super(e);
	}

	public ApiException(ApiResponse e) {
		super();
	}
}
