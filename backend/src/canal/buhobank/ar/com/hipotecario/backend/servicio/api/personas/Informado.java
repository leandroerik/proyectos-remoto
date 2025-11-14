package ar.com.hipotecario.backend.servicio.api.personas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.buhobank.SesionBB;

public class Informado extends ApiObjeto {

	public static String GET_INFORMADO = "Informado";

	/* ========== ATRIBUTOS ========== */
	public Boolean esTerrorista;

	/* ========== SERVICIOS ========== */
	// API-Personas_Informados
	public static Informado get(Contexto contexto, String documento, String apellido) {
		ApiRequest request = new ApiRequest(GET_INFORMADO, ApiPersonas.API, "GET", "/informados", contexto);
		request.query("documento", documento);
		request.query("apellido", apellido);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Informado.class);
	}

	// API-Personas_Informados
	public static Informado get(Contexto contexto, SesionBB sesion) {
		ApiRequest request = new ApiRequest(GET_INFORMADO, ApiPersonas.API, "GET", "/informados", contexto);
		request.query("documento", sesion.numeroDocumento);
		request.query("apellido", sesion.apellido);
		request.query("nombre", sesion.nombre);
		request.query("cuit", sesion.cuil);
		request.query("sexo", sesion.genero);
		request.query("fechaNacimiento", sesion.fechaNacimiento);
		request.query("lugarNacimiento", sesion.paisNacimiento);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Informado.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Informado datos = get(contexto, "13417354", "perez");
		imprimirResultado(contexto, datos);
	}
}
