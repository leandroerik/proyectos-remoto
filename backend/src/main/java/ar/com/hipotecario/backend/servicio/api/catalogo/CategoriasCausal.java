package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.CategoriasCausal.CategoriaCausal;

public class CategoriasCausal extends ApiObjetos<CategoriaCausal> {

	/* ========== ATRIBUTOS ========== */
	public static class CategoriaCausal extends ApiObjeto {
		public String descripcion;
		public String codigo;
	}

	/* ========== SERVICIOS ========== */
	// API-Catalogo_ConsultaCategoriasCausales
	static CategoriasCausal get(Contexto contexto) {
		ApiRequest request = new ApiRequest("CategoriasCausales", "catalogo", "GET", "/v1/CategoriasCausales", contexto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(CategoriasCausal.class);
	}

	// API-Catalogo_ConsultaCategoriaCausal
	static CategoriaCausal get(Contexto contexto, String codigo) {
		ApiRequest request = new ApiRequest("CategoriaCausal", "catalogo", "GET", "/v1/CategoriaCausal/{codigo}", contexto);
		request.path("codigo", codigo);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(CategoriaCausal.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		CategoriasCausal datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
