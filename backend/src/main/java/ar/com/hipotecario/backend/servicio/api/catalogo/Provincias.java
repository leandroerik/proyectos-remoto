package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.Provincias.Provincia;

public class Provincias extends ApiObjetos<Provincia> {

	/* ========== ATRIBUTOS ========== */
	public static class Provincia extends ApiObjeto {
		public String id;
		public String descripcion;
		public String idEstado;
	}

	/* ========== SERVICIOS ========== */
	static Provincias get(Contexto contexto) {
		return get(contexto, "80");
	}

	// API-Catalogo_ConsultaPronvincias
	static Provincias get(Contexto contexto, String idPais) {
		ApiRequest request = new ApiRequest("Provincias", "catalogo", "GET", "/v1/paises/{idPais}/provincias", contexto);
		request.path("idPais", idPais);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200) && !response.contains("no arrojó resultados"), request, response);
		return response.crear(Provincias.class);
	}

	private Provincia buscarId(String id) {
		Provincia dato = null;
		for (Provincia provincia : this) {
			if (provincia.descripcion == null) {
				continue;
			}

			if (provincia.id.equals(id)) {
				dato = provincia;
				break;
			}
		}
		return dato;
	}

	public Provincia buscarProvinciaById(String id) {
		if (empty(id))
			return null;
		return buscarId(id);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Provincias datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
