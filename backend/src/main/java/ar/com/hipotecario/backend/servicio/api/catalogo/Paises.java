package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.Paises.Pais;

public class Paises extends ApiObjetos<Pais> {

	/* ========== ATRIBUTOS ========== */
	public static class Pais extends ApiObjeto {
		public String id;
		public String descripcion;
		public String nacionalidad;
		public String estado;
	}

	/* ========== SERVICIOS ========== */
	public static Paises get(Contexto contexto) {
		return get(contexto, true);
	}

	// API-Catalogo_ConsultaPaises
	public static Paises get(Contexto contexto, Boolean vigente) {
		ApiRequest request = new ApiRequest("Paises", "catalogo", "GET", "/v1/paises", contexto);
		request.query("vigente", vigente);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Paises.class);
	}

	/* ========== METODOS ========== */
	private Pais buscar(String nombre) {
		nombre = nombre.replace("Ñ", "¥");

		Pais dato = null;
		for (Pais pais : this) {
			if (pais.descripcion == null || pais.nacionalidad == null) {
				continue;
			}

			if (pais.descripcion.equals(nombre) || pais.nacionalidad.equals(nombre) || nombre.contains(pais.descripcion) || nombre.contains(pais.nacionalidad.substring(0, pais.nacionalidad.length() - 1))) {
				dato = pais;
				break;
			}
		}
		return dato;
	}

	public Pais buscarPais(String pais) {
		if (empty(pais))
			return null;
		return buscar(pais.toUpperCase());
	}

	private Pais buscarId(String id) {
		Pais dato = null;
		for (Pais pais : this) {
			if (pais.descripcion == null || pais.nacionalidad == null) {
				continue;
			}

			if (pais.id.equals(id)) {
				dato = pais;
				break;
			}
		}
		return dato;
	}

	public Pais buscarPaisById(String id) {
		if (empty(id))
			return null;
		return buscarId(id);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Paises datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
