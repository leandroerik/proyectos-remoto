package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.IndicesBursatiles.IndiceBursatil;

public class IndicesBursatiles extends ApiObjetos<IndiceBursatil> {

	/* ========== ATRIBUTOS ========== */
	public static class IndiceBursatil extends ApiObjeto {
		public String idIndice;
		public String indice;
		public String codigo;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_IndicesBursatiles
	public static IndicesBursatiles get(Contexto contexto, Boolean sectorial) {
		ApiRequest request = new ApiRequest("InversionesIndicesBursatiles", "inversiones", "GET", "/v1/indicesbursatiles", contexto);
		request.query("sectorial", sectorial);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(IndicesBursatiles.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		IndicesBursatiles datos = get(contexto, true);
		imprimirResultado(contexto, datos);
	}
}
