package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.CotizacionesMoneda.CotizacionMoneda;

public class CotizacionesMoneda extends ApiObjetos<CotizacionMoneda> {

	/* ========== ATRIBUTOS ========== */
	public static class CotizacionMoneda extends ApiObjeto {
		public Fecha fechaCotizacion;
		public BigDecimal valorCotizacion;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_CotizacionesMoneda
	public static CotizacionesMoneda get(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String idMoneda) {
		ApiRequest request = new ApiRequest("CotizacionesMoneda", "inversiones", "GET", "/v2/cotizaciones/{idmoneda}", contexto);
		request.path("idmoneda", idMoneda);
		request.query("fechadesde", fechaDesde.string("yyyy-MM-dd"));
		request.query("fechahasta", fechaHasta.string("yyyy-MM-dd"));
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CotizacionesMoneda.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		CotizacionesMoneda datos = get(contexto, Fecha.ayer(), Fecha.hoy(), "88");
		imprimirResultado(contexto, datos);
	}
}
