package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.EstadosCiviles.EstadoCivil;

public class EstadosCiviles extends ApiObjetos<EstadoCivil> {

	/* =========== ATRIBUTOS =========== */
	public static class EstadoCivil extends ApiObjeto {
		public String id;
		public String descripcion;
		public String estado;
	}

	/* =========== SERVICIOS ============ */
	static EstadosCiviles get(Contexto contexto) {
		return get(contexto, true);
	}

	// API-Catalogo_ConsultaEstadoCiviles
	static EstadosCiviles get(Contexto contexto, Boolean vigente) {
		ApiRequest request = new ApiRequest("EstadosCiviles", "catalogo", "GET", "/v1/estadoCiviles", contexto);
		request.query("vigente", vigente);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(EstadosCiviles.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		EstadosCiviles datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
