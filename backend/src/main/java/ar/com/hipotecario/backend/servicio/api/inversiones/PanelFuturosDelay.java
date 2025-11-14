package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class PanelFuturosDelay extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public List<Cotizacion> cotizaciones;
	public String horaUltimaCotizacion;
	public String mensajeNro;

	public static class Cotizacion {
		public BigDecimal ajuste;
		public BigDecimal ajusteAnterior;
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
		public Integer cantidadNominalCompra;
		public Integer cantidadNominalVenta;
		public Integer cantidadOperaciones;
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
	// API-Inversiones_PanelFuturosDelay
	public static PanelFuturosDelay get(Contexto contexto, Integer nroMsg) {
		ApiRequest request = new ApiRequest("InversionesPanelFuturosDelay", "inversiones", "GET", "/v1/panelfuturosdelay", contexto);
		if (nroMsg != null)
			request.query("nroMsg", nroMsg);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(PanelFuturosDelay.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		PanelFuturosDelay datos = get(contexto, null);
		imprimirResultado(contexto, datos);
	}
}
