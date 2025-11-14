package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.IntraDiarias.IntraDiaria;

public class IntraDiarias extends ApiObjetos<IntraDiaria> {

	/* ========== ATRIBUTOS ========== */
	public static class IntraDiaria extends ApiObjeto {
		public String idIntradiaria;
		public String symbol;
		public Fecha entryDate;
		public Fecha fechaModificacion;
		public BigDecimal trade;
		public BigDecimal tradeQty;
		public BigDecimal indexValue;
		public BigDecimal openingPrice;
		public BigDecimal closingPrice;
		public BigDecimal staticReferencePrice;
		public BigDecimal tradingSessionHighPrice;
		public BigDecimal tradingSessionLowPrice;
		public BigDecimal tradingSessionVWAPPrice;
		public BigDecimal tradeVolume;
		public BigDecimal tradeVolumeQty;
		public BigDecimal auctionClearingPrice;
		public BigDecimal previousClose;
		public BigDecimal numberOfOrders;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_Intradiarias
	public static IntraDiarias get(Contexto contexto, Integer idPanel, String codigo, String idVencimiento) {
		ApiRequest request = new ApiRequest("InversionesIndicesRealTime", "inversiones", "GET", "/v1/intradiarias", contexto);
		request.query("idPanel", idPanel);
		if (idVencimiento != null)
			request.query("idVencimiento", idVencimiento);
		if (codigo != null)
			request.query("codigo", codigo);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(IntraDiarias.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		IntraDiarias datos = get(contexto, -1, null, null);
		imprimirResultado(contexto, datos);
	}

}
