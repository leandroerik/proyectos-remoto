package ar.com.hipotecario.mobile.excepcion;

import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class ApiExceptionMB extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ApiExceptionMB(Exception e) {
		super(e);
	}

	public ApiExceptionMB(ApiResponseMB e) {
		super();
	}
}
