package ar.com.hipotecario.backend.servicio.api.prestamos;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.prestamos.PrestamosPagos.PrestamoPago;

public class PrestamosPagos extends ApiObjetos<PrestamoPago> {

	/* ========== ATRIBUTOS ========== */
	public static class PrestamoPago extends ApiObjeto {
		public String operacion;
		public String secuencia;
		public String tipo;
		public String recibo;
		public String estado;
		public String usuario;
		public String sentido;
		public String corresponsal;
		public String formaCobro;
		public String modOrigen;
		public String hipotecarioNSP;
		public String tipoPagoNSP;
		public String fechaPago;
		public String fechaProceso;
		public Integer convenio;
		public Integer lote;
		public Integer linea;
		public BigDecimal valor;
		public BigDecimal sobrante;
		public BigDecimal cotizacion;
		public BigDecimal montoMN;
	}

	/* ========== SERVICIOS ========== */
	// API-Prestamos_PagoPrestamoConsultaPagos
	public static PrestamosPagos get(Contexto contexto, String numOperacion, Fecha fechaDesde, Fecha fechaHasta) {
		ApiRequest request = new ApiRequest("PrestamosPagos", "prestamos", "GET", "/v1/prestamos/{numOperacion}/pagos", contexto);
		request.path("numOperacion", numOperacion);
		request.query("fechadesde", fechaDesde.string("MM/dd/yyyy"));
		request.query("fechahasta", fechaHasta.string("MM/dd/yyyy"));
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(PrestamosPagos.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fechaDesde = new Fecha("01/21/2020", "MM/dd/yyyy");
		Fecha fechaHasta = new Fecha("04/21/2020", "MM/dd/yyyy");
		PrestamosPagos datos = get(contexto, "0370081148", fechaDesde, fechaHasta);
		imprimirResultado(contexto, datos);
	}
}
