package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.Ramos.Ramo;

public class Ramos extends ApiObjetos<Ramo> {

	/* ========== ATRIBUTOS ========== */
	public static class Ramo extends ApiObjeto {
		public String id;
		public String descripcion;
		public String estado;
	}

	/* ========== SERVICIOS ========== */
	static Ramos get(Contexto contexto) {
		return get(contexto, true);
	}

	// API-Catalogo_ConsultaRamos
	static Ramos get(Contexto contexto, Boolean vigente) {
		ApiRequest request = new ApiRequest("Ramos", "catalogo", "GET", "/v1/ramos", contexto);
		request.query("vigente", vigente);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Ramos.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Ramos datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
