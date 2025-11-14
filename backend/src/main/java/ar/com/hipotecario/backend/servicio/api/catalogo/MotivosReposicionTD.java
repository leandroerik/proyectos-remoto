package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.MotivosReposicionTD.MotivoReposicionTD;;

public class MotivosReposicionTD extends ApiObjetos<MotivoReposicionTD> {

	/* ========== ATRIBUTOS ========== */
	public static class MotivoReposicionTD extends ApiObjeto {
		public String codigo;
		public String descripcion;
	}

	/* ============== SERVICIOS ============= */
	// API-Catalogo_ReposicionTD
	static MotivosReposicionTD get(Contexto contexto) {
		ApiRequest request = new ApiRequest("MotivoReposicionTD", "catalogo", "GET", "/v1/cobis/reposicion", contexto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(MotivosReposicionTD.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		MotivosReposicionTD datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
