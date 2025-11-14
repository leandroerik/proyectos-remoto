package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.Profesiones.Profesion;

public class Profesiones extends ApiObjetos<Profesion> {

	/* ========== ATRIBUTOS ========== */
	public static class Profesion extends ApiObjeto {
		public String id;
		public String descripcion;
		public String estado;
	}

	/* ========== SERVICIOS ========== */
	static Profesiones get(Contexto contexto, String idRamo) {
		return get(contexto, idRamo, true);
	}

	// API-Catalogo_ConsultaProfesiones
	static Profesiones get(Contexto contexto, String idRamo, Boolean vigente) {
		ApiRequest request = new ApiRequest("RamosProfesiones", "catalogo", "GET", "/v1/ramos/{idRamo}/profesiones", contexto);
		request.path("idRamo", idRamo);
		request.query("vigente", vigente);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Profesiones.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Profesiones datos = get(contexto, "51101");
		imprimirResultado(contexto, datos);
	}
}
