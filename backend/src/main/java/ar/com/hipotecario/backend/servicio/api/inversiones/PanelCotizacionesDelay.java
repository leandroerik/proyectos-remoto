package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class PanelCotizacionesDelay extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public List<Cotizacion> cotizaciones;
	public String horaUltimaCotizacion;
	public String mensajeNro;

	public static class Cotizacion {
		public Integer apertura;
		public Integer cantidadNominalCompra;
		public Integer cantidadNominalVenta;
		public Integer cantidadOperaciones;
		public Integer cantidadOperada;
		public BigDecimal cierreAnterior;
		public BigDecimal maximo;
		public BigDecimal minimo;
		public BigDecimal montoOperadoPesos;
		public BigDecimal precioCompra;
		public BigDecimal precioVenta;
		public BigDecimal precioPromedio;
		public BigDecimal precioPromedioPonderado;
		public BigDecimal ultimo;
		public BigDecimal variacion;
		public BigDecimal volumenNominal;
		public String denominacion;
		public String estado;
		public String ex;
		public String horaCorizacion;
		public String simbolo;
		public String tendencia;
		public String tipoLiquidacion;
		public String vencimiento;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_PanelCotizacionesDelay
	public static PanelCotizacionesDelay get(Contexto contexto, String idPanel) {
		ApiRequest request = new ApiRequest("InversionesPanelesCotizacionesDelay", "inversiones", "GET", "/v1/panelcotizacionesdelay", contexto);
		request.query("idPanel", idPanel);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(PanelCotizacionesDelay.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		PanelCotizacionesDelay datos = get(contexto, "7");
		imprimirResultado(contexto, datos);
	}
}
