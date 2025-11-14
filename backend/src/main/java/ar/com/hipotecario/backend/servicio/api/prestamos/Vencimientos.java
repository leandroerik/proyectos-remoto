package ar.com.hipotecario.backend.servicio.api.prestamos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class Vencimientos extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String pdf;

	/* ========== SERVICIOS ========== */
	// API-Prestamos_AvisoVencimientoPdfGet
	public static Vencimientos get(Contexto contexto, String plantilla, String cuota, String numeroOperacion) {
		ApiRequest request = new ApiRequest("PrestamoAvisoVencimiento", "prestamos", "GET", "/v1/vencimientos/{plantilla}/prestamos", contexto);
		request.path("plantilla", plantilla);

		request.query("numerooperacion", numeroOperacion);
		request.query("cuota", cuota);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Vencimientos.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Vencimientos datos = get(contexto, "AvisoVencimiento", "2", "0820194122");
		imprimirResultado(contexto, datos);
	}
}
