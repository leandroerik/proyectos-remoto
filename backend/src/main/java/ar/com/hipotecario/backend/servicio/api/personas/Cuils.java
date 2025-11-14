package ar.com.hipotecario.backend.servicio.api.personas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.personas.Cuils.Cuil;

public class Cuils extends ApiObjetos<Cuil> {

	public static String GET_CUILS = "Cuils";

	/* ========== ATRIBUTOS ========== */
	public static class Cuil extends ApiObjeto {
		public String apellidoYNombre;
		public String cuil;

		public String getCuil() {
			return cuil;
		}

	}

	/* ========== SERVICIOS ========== */
	public static Cuils get(Contexto contexto, String numeroDocumento) {
		return get(contexto, numeroDocumento, null);
	}

	// API-Personas_Consultacuil
	public static Cuils get(Contexto contexto, String numeroDocumento, String nombreCompleto) {
		ApiRequest request = new ApiRequest(GET_CUILS, ApiPersonas.API, "GET", "/cuils", contexto);
		request.query("dni", numeroDocumento);
		request.query("apYNom", nombreCompleto);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Cuils.class);
	}
	
	// API-Personas_ConsultaNroDocumento
	public static Cuils getNroDoc(Contexto contexto, String numeroDocumento) {
		ApiRequest request = new ApiRequest(GET_CUILS, ApiPersonas.API, "GET", "/nrodoc", contexto);
		request.query("nrodoc", numeroDocumento);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Cuils.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Cuils datos = get(contexto, "95959968");
		imprimirResultado(contexto, datos);
		datos = get(contexto, "9981445", "Navarro Martin");
		imprimirResultado(contexto, datos);
	}
}
