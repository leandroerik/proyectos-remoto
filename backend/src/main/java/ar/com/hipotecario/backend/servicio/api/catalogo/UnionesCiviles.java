package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.UnionesCiviles.UnionCivil;

public class UnionesCiviles extends ApiObjetos<UnionCivil> {

	/* ========== ATRIBUTOS ========== */
	public static class UnionCivil extends ApiObjeto {
		public String id;
		public String descripcion;
		public String estado;
	}

	/* ========== SERVICIOS ========== */
	static UnionesCiviles get(Contexto contexto, String estadoCivil) {
		return get(contexto, estadoCivil, true);
	}

	// API-Catalogo_ConsultaUnionesCiviles
	static UnionesCiviles get(Contexto contexto, String estadoCivil, Boolean vigente) {
		ApiRequest request = new ApiRequest("UnionesCiviles", "catalogo", "GET", "/v1/unionesCiviles", contexto);
		request.query("estadoCivil", estadoCivil);
		request.query("vigente", vigente);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(UnionesCiviles.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		UnionesCiviles datos = get(contexto, "C");
		imprimirResultado(contexto, datos);
	}
}
