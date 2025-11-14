package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.Cargos.Cargo;

public class Cargos extends ApiObjetos<Cargo> {

	/* ========== ATRIBUTOS ========== */
	public static class Cargo extends ApiObjeto {
		public String id;
		public String descripcion;
		public String estado;
	}

	/* ========== SERVICIOS ========== */
	static Cargos get(Contexto contexto, String idProfesion) {
		return get(contexto, idProfesion, true);
	}

	// API-Catalogo_ConsultaCargos
	static Cargos get(Contexto contexto, String idProfesion, Boolean vigente) {
		ApiRequest request = new ApiRequest("Cargos", "catalogo", "GET", "/v1/ramos/profesiones/{idProfesion}/cargos", contexto);
		request.path("idProfesion", idProfesion);
		request.query("vigente", vigente);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Cargos.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Cargos datos = get(contexto, "30701");
		imprimirResultado(contexto, datos);
	}
}
