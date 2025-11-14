package ar.com.hipotecario.backend.servicio.api.prestamos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class PrestamosNsp extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	// NO ENCONTRE DATOS PARA OBTENER RESPUESTA

	/* ========== SERVICIOS ========== */
	// API-Prestamos_ConsultaPrestamoNSP
	public static PrestamosNsp get(Contexto contexto, String id, String producto) {
		ApiRequest request = new ApiRequest("PrestamosNsp", "prestamos", "GET", "/v1/prestamosnsp/{id}", contexto);
		request.path("id", id);
		request.query("producto", producto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(PrestamosNsp.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		PrestamosNsp datos = get(contexto, "45435435", "342343");
		imprimirResultado(contexto, datos);
	}
}
