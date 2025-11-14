package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.Sexos.Sexo;;

public class Sexos extends ApiObjetos<Sexo> {

	/* ========== ATRIBUTOS ========== */
	public static class Sexo extends ApiObjeto {
		public String codigoSexo;
		public String descripcionSexo;
	}

	/* =========== SERVICIOS =========== */
	// API-Catalogo_ConsultaCodigosSexos
	static Sexos get(Contexto contexto) {
		ApiRequest request = new ApiRequest("Sexos", "catalogo", "GET", "/v1/codigos/sexos", contexto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Sexos.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Sexos datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
