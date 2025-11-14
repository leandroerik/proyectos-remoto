package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.CotizacionesHistoricas.CotizacionHistorica;

public class CotizacionesHistoricas extends ApiObjetos<CotizacionHistorica> {

	/* ========== ATRIBUTOS ========== */
	public static class CotizacionHistorica extends ApiObjeto {
		public String descError;
		public String descripcion;
		public String estado;
		public String ex;
		public String moneda;
		public String panel;
		public String simbolo;
		public String tendencia;
		public String vencimiento;
		public String liquidacion;
		public String id;
		public String idCotizacion;
		public String idEstado;
		public String idLiquidacion;
		public String idMoneda;
		public String idPanel;
		public String idVencimiento;
		public Fecha fecha;
		public Integer apertura;
		public Integer cantidadOperaciones;
		public Integer codError;
		public BigDecimal cierreAnterior;
		public BigDecimal maximo;
		public BigDecimal minimo;
		public BigDecimal ultimo;
		public BigDecimal montoOperado;
		public BigDecimal precioPromedioPonderado;
		public BigDecimal promedioDiario;
		public BigDecimal variacion;
		public BigDecimal volumenNominal;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_CotizacionesHistoricas
	public static CotizacionesHistoricas get(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String idEspecie, String idIndice, String indice, String simbolo, String idVencimiento) {
		ApiRequest request = new ApiRequest("InversionesCotizacionesHistoricas", "inversiones", "GET", "/v1/cotizacioneshistoricas", contexto);
		request.query("fechaDesde", fechaDesde.string("yyyy-MM-dd"));
		request.query("fechaHasta", fechaHasta.string("yyyy-MM-dd"));
		request.query("simbolo", simbolo);
		request.query("idVencimiento", idVencimiento);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CotizacionesHistoricas.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fechaDesde = new Fecha("2019-08-18", "yyyy-MM-dd");
		Fecha fechaHasta = new Fecha("2020-08-18", "yyyy-MM-dd");
		CotizacionesHistoricas datos = get(contexto, fechaDesde, fechaHasta, "", "", "", "A2E2", "1");
		imprimirResultado(contexto, datos);
	}
}
