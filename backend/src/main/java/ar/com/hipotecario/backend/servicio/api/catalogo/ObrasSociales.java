package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.ObrasSociales.ObraSocial;

public class ObrasSociales extends ApiObjetos<ObraSocial> {

	/* =========== ATRIBUTOS =========== */
	public static class ObraSocial extends ApiObjeto {
		public String id;
		public String descripcion;
		public String estado;
	}

	/* =========== SERVICIOS ============ */
	static ObrasSociales get(Contexto contexto) {
		return get(contexto, true);
	}

	// API-Catalogo_ConsultaObrasSociales
	static ObrasSociales get(Contexto contexto, Boolean vigente) {
		ApiRequest request = new ApiRequest("ObrasSociales", "catalogo", "GET", "/v1/obraSociales", contexto);
		request.query("vigente", vigente);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(ObrasSociales.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		ObrasSociales datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
