package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class IndicesSectorialesDelay extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public List<Indice> indices;

	public static class Indice {
		public String nombre;
		public BigDecimal variacion;
		public BigDecimal apertura;
		public BigDecimal maximoValor;
		public BigDecimal minimoValor;
		public BigDecimal cierreDia;
		public BigDecimal cierreAnterior;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_IndicesSectorialesDelay
	public static IndicesSectorialesDelay get(Contexto contexto) {
		ApiRequest request = new ApiRequest("InversionesIndicesSectorialesDelay", "inversiones", "GET", "/v1/indicessectorialesdelay", contexto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(IndicesSectorialesDelay.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		IndicesSectorialesDelay datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
