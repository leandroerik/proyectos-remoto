package ar.com.hipotecario.backend.servicio.api.prestamos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.prestamos.PrestamosResumen.PrestamoResumen;

public class PrestamosResumen extends ApiObjetos<PrestamoResumen> {

	/* ========== ATRIBUTOS ========== */
	public static class PrestamoResumen extends ApiObjeto {
		public String numero;
		public String codigo;
		public String descripcion;
		public String referencia;
		public String usuarioAlta;
		public String usuarioUltimaModificacion;
		public String restricciones;
		public Fecha fechaInicial;
		public Fecha fechaAlta;
		public Fecha fechaUltimaModificacion;
	}

	/* ========== SERVICIOS ========== */
	// API-Prestamos_ConsultaResumenPrestamo
	// SERVICIO NO FUNCIONA
	public static PrestamosResumen get(Contexto contexto, String id, Boolean leyenda) {
		ApiRequest request = new ApiRequest("PrestamosResumen", "prestamos", "GET", "/v1/prestamos/{id}/resumen", contexto);
		request.path("id", id);
		request.query("leyenda", leyenda);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(PrestamosResumen.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		PrestamosResumen datos = get(contexto, "0001669293", false);
		imprimirResultado(contexto, datos);
	}
}
