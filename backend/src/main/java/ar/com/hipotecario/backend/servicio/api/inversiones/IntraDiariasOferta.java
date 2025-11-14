package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.IntraDiariasOferta.IntraDiaria;

public class IntraDiariasOferta extends ApiObjetos<IntraDiaria> {

	/* ========== ATRIBUTOS ========== */
	public static class IntraDiaria extends ApiObjeto {
		public String idIntradiaria;
		public String symbol;
		public String tipo;
		public BigDecimal settlType;
		public BigDecimal quality;
		public BigDecimal price;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_IntradiariasOferta
	public static IntraDiariasOferta get(Contexto contexto, Integer idPanel, String codigo, String idVencimiento, String idIntradiaria) {
		ApiRequest request = new ApiRequest("InversionesIntradiariasOferta", "inversiones", "GET", "/v1/intradiariasoferta", contexto);
		request.query("idPanel", idPanel);
		request.query("idIntradiaria", idIntradiaria);
		request.query("idVencimiento", idVencimiento);
		request.query("codigo", codigo);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(IntraDiariasOferta.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		IntraDiariasOferta datos = get(contexto, null, null, null, "602");
		imprimirResultado(contexto, datos);
	}

}
