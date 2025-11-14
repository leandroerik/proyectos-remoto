package ar.com.hipotecario.mobile.excepcion;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;

public class ApiVentaExceptionMB extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/* ========== ATRIBUTOS ========== */
	public List<String> errores = new ArrayList<>();

	public ApiVentaExceptionMB(ApiResponseMB response) {
		for (Objeto item : response.objetos("Errores")) {
			errores.add(item.string("MensajeCliente"));
		}
	}

}
