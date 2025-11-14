package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.SubcategoriasCausal.SubcategoriaCausal;

public class SubcategoriasCausal extends ApiObjetos<SubcategoriaCausal> {

	/* ========== ATRIBUTOS ========== */
	public static class SubcategoriaCausal extends ApiObjeto {
		public String Descripcion;
		public String Codigo;
		public String CodigoCategoria;
	}

	/* ========== SERVICIOS ========== */
	// API-Catalogo_ConsultaSubcategoriasCausales
	static SubcategoriasCausal get(Contexto contexto) {
		ApiRequest request = new ApiRequest("SubcategoriasCausal", "catalogo", "GET", "/v1/SubcategoriasCausales", contexto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(SubcategoriasCausal.class);
	}

	// API-Catalogo_ConsultaSubcategoriaCausal
	static SubcategoriaCausal get(Contexto contexto, String causal, String categoria) {
		ApiRequest request = new ApiRequest("SubcategoriaCausal", "catalogo", "GET", "/v1/SubcategoriaCausal/{codigo}/{codigoCategoria}", contexto);
		request.path("codigo", causal);
		request.path("codigoCategoria", categoria);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(SubcategoriaCausal.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		SubcategoriasCausal datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
