package ar.com.hipotecario.canal.homebanking.ventas;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

public class ApiVentaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/* ========== ATRIBUTOS ========== */
	public List<String> errores = new ArrayList<>();

	public ApiVentaException(ApiResponse response) {
		for (Objeto item : response.objetos("Errores")) {
			errores.add(item.string("MensajeCliente"));
		}
	}
}
