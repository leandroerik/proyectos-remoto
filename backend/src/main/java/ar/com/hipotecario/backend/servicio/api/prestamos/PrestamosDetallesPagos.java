package ar.com.hipotecario.backend.servicio.api.prestamos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class PrestamosDetallesPagos extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	// SIN DATOS PARA PROBAR

	/* ========== SERVICIOS ========== */
	// API-Prestamos_ConsultaDetallePagos
	public static PrestamosDetallesPagos get(Contexto contexto, String numOperacion, String secPago) {
		ApiRequest request = new ApiRequest("PrestamosDetallesPagos", "prestamos", "GET", "/v1/prestamos/{numOperacion}/detallepagos", contexto);
		request.path("numOperacion", numOperacion);
		request.query("secPago", secPago);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(PrestamosDetallesPagos.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		PrestamosDetallesPagos datos = get(contexto, "0002104874", "571937719");
		imprimirResultado(contexto, datos);
	}
}
