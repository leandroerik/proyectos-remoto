package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.TenenciasResumenDetalle.TenenciaResumenDetalle;

public class TenenciasResumenDetalle extends ApiObjetos<TenenciaResumenDetalle> {

	/* ========== ATRIBUTOS ========== */
	public static class TenenciaResumenDetalle extends ApiObjeto {
		public String anulado;
		public String cuitCuil;
		public String descripcionEstado;
		public String fechaConcertacion;
		public BigDecimal importe;
		public String moneda;
		public String nombreFondo;
		public String nroCuotapartista;
		public String numeroSolicitud;
		public String tipo;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_TenenciasFCIResumenDetalle
	public static TenenciasResumenDetalle get(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String numCuotaPartista) {
		ApiRequest request = new ApiRequest("TenenciasResumenDetalle", "inversiones", "GET", "/v1/tenencias/resumendetalle", contexto);
		request.query("fechadesde", fechaDesde.string("yyyy-MM-dd"));
		request.query("fechahasta", fechaHasta.string("yyyy-MM-dd"));
		request.query("numCuotaPartista", numCuotaPartista);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(TenenciasResumenDetalle.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fechaDesde = new Fecha("2018-11-01", "yyyy-MM-dd");
		Fecha fechaHasta = new Fecha("2021-01-01", "yyyy-MM-dd");
		TenenciasResumenDetalle datos = get(contexto, fechaDesde, fechaHasta, "8473");
		imprimirResultado(contexto, datos);
	}
}
