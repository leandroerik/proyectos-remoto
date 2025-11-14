package ar.com.hipotecario.backend.servicio.api.catalogo;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.ParametriasPlazosFijos.ParametriaPlazosFijos;

public class ParametriasPlazosFijos extends ApiObjetos<ParametriaPlazosFijos> {

	/* ========== ATRIBUTOS ========== */
	public static class ParametriaPlazosFijos extends ApiObjeto {
		public String monedaDesc;
		public String tipoPlazoFijo;
		public String descripcion;
		public String codPlan;
		public String descPlan;
		public String referencial;
		public String moneda;
		public String estado;
		public String ajuste;
		public String indice;
		public String plazo;
		public Fecha vigenciaDesde;
		public Fecha vigenciaHasta;
		public BigDecimal secuencial;
		public BigDecimal spread;
		public BigDecimal reintentos;
		public BigDecimal tasa;
	}

	/* ========== SERVICIOS ========== */
	// API-Catalogo_ConsultaParametriaPlanAhorroPlazoFijo
	static ParametriasPlazosFijos get(Contexto contexto, String idCobis) {
		ApiRequest request = new ApiRequest("ParametriasPlazosFijos", "catalogo", "GET", "/v1/plazoFijos/parametrias", contexto);
		request.query("operacion", "Q");
		request.query("opcion", "1");
		request.query("codCliente", idCobis);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(ParametriasPlazosFijos.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		ParametriasPlazosFijos datos = get(contexto, "5093472");
		imprimirResultado(contexto, datos);
	}
}
