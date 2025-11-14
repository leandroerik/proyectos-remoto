package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class RentaFinanciera extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String file;

	/* ========== SERVICIOS ========== */
	// API-Inversiones_RentaFinanciera
	public static RentaFinanciera get(Contexto contexto, String cuil, String idCobis, String periodo) {
		ApiRequest request = new ApiRequest("RentaFinanciera", "inversiones", "GET", "/v1/rentafinanciera", contexto);
		request.query("cuil", cuil);
		request.query("idCobis", idCobis);
		request.query("periodo", periodo);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(RentaFinanciera.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		RentaFinanciera datos = get(contexto, "20081190233", "4373070", "2019");
		imprimirResultado(contexto, datos);
	}

}
