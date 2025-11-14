package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.Ofertas.Oferta;

public class Ofertas extends ApiObjetos<Oferta> {

	/* ========== ATRIBUTOS ========== */
	public static class Oferta extends ApiObjeto {
		public Integer id;
		public String applID;
		public String execID;
		public String clOrdID;
		public String orderID;
		public String multiLegReportingType;
		public String execType;
		public String ordStatus;
		public Integer ordRejReason;
		public String text;
		public Integer leavesQty;
		public Integer cumQty;
		public String symbol;
		public String securityType;
		public String currency;
		public Integer orderBook;
		public Integer accountType;
		public String ordType;
		public String timeInForce;
		public String side;
		public String orderCapacity;
		public String mDEntryID;
		public BigDecimal orderQty;
		public BigDecimal displayQty;
		public BigDecimal price;
		public Boolean preTradeAnonymity;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_ExecutionReport
	public static Ofertas get(Contexto contexto, String id) {
		return get(contexto, id, null);
	}

	public static Ofertas get(Contexto contexto, String id, String cantidad) {
		ApiRequest request = new ApiRequest("Ofertas", "inversiones", "GET", "/v1/oferta", contexto);
		request.query("cantidad", cantidad != null ? cantidad : 0);
		request.query("id", id);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Ofertas.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
//		Ofertas datos = get(contexto, "11483");
		Ofertas datos = get(contexto, "11483", "2");
		imprimirResultado(contexto, datos);
	}

}
