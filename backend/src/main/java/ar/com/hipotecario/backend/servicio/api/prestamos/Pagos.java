package ar.com.hipotecario.backend.servicio.api.prestamos;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.prestamos.Pagos.PagoPrestamo;

public class Pagos extends ApiObjetos<PagoPrestamo> {

	/* ========== ATRIBUTOS ========== */
	public static class PagoPrestamo extends ApiObjeto {
		public String sentido;
		public String formaPago;
		public String origen;
		public String usuario;
		public String hipotecarioNsp;
		public String estado;
		public String convenio;
		public String corresponsal;
		public String nroCuenta;
		public String tipo;
		public String id;
		public String lote;
		public String linea;
		public String tipoPagoNsp;
		public String recibo;
		public Fecha fechaProceso;
		public Fecha fechaPago;
		public BigDecimal sobrante;
		public BigDecimal cotizacion;
		public BigDecimal montoPesos;
		public BigDecimal monto;
	}

	public static class PagosPrestamosRubros extends ApiObjetos<PagoPrestamoRubro> {
	}

	public static class PagoPrestamoRubro extends ApiObjeto {
		public String concepto;
		public String dividendo;
		public String estado;
		public String monto;
	}

	/* ========== SERVICIOS ========== */
	// API-Prestamos_ConsultaRubrosPagosPrestamo
	public static Pagos get(Contexto contexto, String idPrestamo, Fecha fechaDesde, Fecha fechaHasta) {
		ApiRequest request = new ApiRequest("PrestamoConsultaPagos", "prestamos", "GET", "/v1/pagosprestamo", contexto);
		request.query("idprestamo", idPrestamo);
		request.query("fechadesde", fechaDesde.string("yyyy-MM-dd"));
		request.query("fechahasta", fechaHasta.string("yyyy-MM-dd"));
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Pagos.class);
	}

	// API-Prestamos_ConsultaRubrosPagosPrestamo
	public static PagosPrestamosRubros getPrestamosRubros(Contexto contexto, String idPrestamo, String id) {
		ApiRequest request = new ApiRequest("PrestamoRubrosPagos", "prestamos", "GET", "/v1/pagosprestamo/{id}/rubros", contexto);
		request.path("id", id);
		request.query("idprestamo", idPrestamo);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(PagosPrestamosRubros.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		String metodo = "pagosRubros";

		if (metodo.equals("pagos")) {
			Fecha fechaDesde = new Fecha("2020-03-01", "yyyy-MM-dd");
			Fecha fechaHasta = new Fecha("2020-09-30", "yyyy-MM-dd");
			Pagos datos = get(contexto, "0540267228", fechaDesde, fechaHasta);
			imprimirResultado(contexto, datos);
		}

		if (metodo.equals("pagosRubros")) {
			// SIN DATOS A PROBAR
			PagosPrestamosRubros datos = getPrestamosRubros(contexto, "0540267228", "4000306");
			imprimirResultado(contexto, datos);
		}
	}
}
