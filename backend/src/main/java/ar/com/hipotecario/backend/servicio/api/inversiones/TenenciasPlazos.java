package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.TenenciasPlazos.TenenciaPlazo;

public class TenenciasPlazos extends ApiObjetos<TenenciaPlazo> {

	/* ========== ATRIBUTOS ========== */
	public static class TenenciaPlazo extends ApiObjeto {
		public String NombreFondo;
		public Fecha FechaIngreso;
		public Integer ValorCuotaParte;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_TenenciasFCIPlazos
	public static TenenciasPlazos get(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String nombreFondo) {
		ApiRequest request = new ApiRequest("TenenciasPlazos", "inversiones", "GET", "/v1/tenencias/plazos", contexto);
		request.query("fechaDesde", fechaDesde.string("dd-MM-yyyy"));
		request.query("fechaHasta", fechaHasta.string("dd-MM-yyyy"));
		request.query("nombreFondo", nombreFondo);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(TenenciasPlazos.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		String nombreFondo = "TORONTO TRUST MULTIMERCADO";
		Fecha fechaDesde = new Fecha("01-11-2019", "dd-MM-yyyy");
		Fecha fechaHasta = new Fecha("01-12-2019", "dd-MM-yyyy");
		TenenciasPlazos datos = get(contexto, fechaDesde, fechaHasta, nombreFondo);
		imprimirResultado(contexto, datos);
	}
}
