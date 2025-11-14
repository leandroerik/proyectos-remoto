package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.TenenciasRendi.TenenciaRendi;

public class TenenciasRendi extends ApiObjetos<TenenciaRendi> {

	/* ========== ATRIBUTOS ========== */
	public static class TenenciaRendi extends ApiObjeto {
		public String NombreFondo;
		public Fecha FechaIngreso;
		public String Plazo;
		public BigDecimal Capital;
		public BigDecimal Rendimiento;
		public BigDecimal Porcentaje;
		public BigDecimal ValorCuotaParte;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_TenenciasFCIRendi
	public static TenenciasRendi get(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String nombreFondo, String capital) {
		ApiRequest request = new ApiRequest("TenenciasRendi", "inversiones", "GET", "/v1/tenencias/rendi", contexto);
		request.query("fechaDesde", fechaDesde.string("dd-MM-yyyy"));
		request.query("fechaHasta", fechaHasta.string("dd-MM-yyyy"));
		request.query("nombreFondo", nombreFondo);
		request.query("capital", capital);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(TenenciasRendi.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		String nombreFondo = "TORONTO TRUST MULTIMERCADO";
		String capital = "1";
		Fecha fechaDesde = new Fecha("01-11-2019", "dd-MM-yyyy");
		Fecha fechaHasta = new Fecha("01-12-2019", "dd-MM-yyyy");
		TenenciasRendi datos = get(contexto, fechaDesde, fechaHasta, nombreFondo, capital);
		imprimirResultado(contexto, datos);
	}
}
