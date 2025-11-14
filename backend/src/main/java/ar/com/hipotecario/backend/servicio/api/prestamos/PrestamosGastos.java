package ar.com.hipotecario.backend.servicio.api.prestamos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.prestamos.PrestamosGastos.PrestamoGasto;

public class PrestamosGastos extends ApiObjetos<PrestamoGasto> {

	/* ========== ATRIBUTOS ========== */
	public static class PrestamoGasto extends ApiObjeto {
		// SIN DATOS A PROBAR
	}

	/* ========== SERVICIOS ========== */
	// API-Prestamos_ConsultaGastoPrestamos
	public static PrestamosGastos get(Contexto contexto, String numOperacion, Fecha fecha) {
		ApiRequest request = new ApiRequest("PrestamosGastos", "prestamos", "GET", "/v1/prestamos/gasto", contexto);
		request.query("num_operacion", numOperacion);
		request.query("fecha", fecha.string("MM/dd/yyyy"));

		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(PrestamosGastos.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fecha = new Fecha("01/21/2021", "MM/dd/yyyy");
		PrestamosGastos datos = get(contexto, "0460099005", fecha);
		imprimirResultado(contexto, datos);
	}
}
