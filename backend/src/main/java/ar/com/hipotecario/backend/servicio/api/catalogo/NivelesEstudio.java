package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.NivelesEstudio.NivelEstudio;

public class NivelesEstudio extends ApiObjetos<NivelEstudio> {

	/* =========== ATRIBUTOS =========== */
	public static class NivelEstudio extends ApiObjeto {
		public String id;
		public String descripcion;
		public String estado;
		public Integer orden;
	}

	/* =========== SERVICIOS =========== */
	static NivelesEstudio get(Contexto contexto) {
		return get(contexto, true);
	}

	// API-Catalogo_NivelEstudios
	static NivelesEstudio get(Contexto contexto, Boolean vigente) {
		ApiRequest request = new ApiRequest("NivelesEstudio", "catalogo", "GET", "/v1/nivelEstudios", contexto);
		request.query("vigente", vigente);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(NivelesEstudio.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		NivelesEstudio datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
