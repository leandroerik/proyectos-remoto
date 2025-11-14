package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.Ciudades.Ciudad;

public class Ciudades extends ApiObjetos<Ciudad> {

	/* ========== ATRIBUTOS ========== */
	public static class Ciudad extends ApiObjeto {
		public String id;
		public String descripcion;
		public String estado;
		public String idProvincia;
		public String idPais;
		public String distrito;
	}

	/* ========== SERVICIOS ========== */
	static Ciudades get(Contexto contexto, String idProvincia) {
		return get(contexto, idProvincia, "80");
	}

	// API-Catalogo_ConsultaCiudades
	static Ciudades get(Contexto contexto, String idProvincia, String idPais) {
		ApiRequest request = new ApiRequest("Ciudades", "catalogo", "GET", "/v1/paises/{idPais}/provincias/{idProvincia}/ciudades", contexto);
		request.path("idPais", idPais);
		request.path("idProvincia", idProvincia);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200) && !response.contains("no arrojó resultados"), request, response);
		return response.crear(Ciudades.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Ciudades datos = get(contexto, "2");
		imprimirResultado(contexto, datos);
	}
}
