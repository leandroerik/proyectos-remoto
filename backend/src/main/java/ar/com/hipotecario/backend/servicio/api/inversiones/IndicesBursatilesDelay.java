package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class IndicesBursatilesDelay extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public List<Indice> indices;
	public String mensaje;

	public static class Indice {
		public String nombre;
		public String hora;
		public String mensaje;
		public BigDecimal variacion;
		public BigDecimal apertura;
		public BigDecimal maximoValor;
		public BigDecimal minimoValor;
		public BigDecimal tendencia;
		public BigDecimal ultimo;
		public BigDecimal cierre;
		public BigDecimal promedioDiario;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_IndicesBursatilesDelay
	public static IndicesBursatilesDelay get(Contexto contexto, Boolean cierreAnterior) {
		ApiRequest request = new ApiRequest("InversionesIndicesBursatilesDelay", "inversiones", "GET", "/v1/indicesbursatilesdelay", contexto);
		request.query("cierreAnterior", cierreAnterior);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(IndicesBursatilesDelay.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		IndicesBursatilesDelay datos = get(contexto, false);
		imprimirResultado(contexto, datos);
	}
}
