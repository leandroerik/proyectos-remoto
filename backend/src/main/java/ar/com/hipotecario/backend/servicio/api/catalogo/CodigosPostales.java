package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.CodigosPostales.CodigoPostal;

public class CodigosPostales extends ApiObjetos<CodigoPostal> {

	/* ========== ATRIBUTOS ========== */
	public static class CodigoPostal extends ApiObjeto {
		public String codigoPostal;
		public String idCiudad;
		public String idProvincia;
		public String partidoCiudad;
		public String provincia;
		public String statuscode;
	}

	/* ========== SERVICIOS ========== */
	static CodigosPostales get(Contexto contexto, String codigoPostal) {
		return get(contexto, codigoPostal, "", "");
	}

	// API-Catalogo_ConsultaPorCodigoPostalCiudadProvincia
	static CodigosPostales get(Contexto contexto, String codigoPostal, String ciudad, String provincia) {
		ApiRequest request = new ApiRequest("CodigosPostales", "catalogo", "GET", "/v1/cp", contexto);
		request.query("cp", codigoPostal);
		request.query("partidoCiudad", ciudad);
		request.query("provincia", provincia);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200) && !response.contains("no arrojó resultados"), request, response);
		return response.crear(CodigosPostales.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		CodigosPostales datos = get(contexto, "1406");
		imprimirResultado(contexto, datos);
	}
}
