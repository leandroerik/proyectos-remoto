package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.Especies.Especie;

public class Especies extends ApiObjetos<Especie> {

	/* ========== ATRIBUTOS ========== */
	public static class Especie extends ApiObjeto {
		public String idPanel;
		public String idEspecie;
		public String panel;
		public String simbolo;
		public String moneda;
		public String idMoneda;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_Especies
	public static Especies get(Contexto contexto, String idPanel) {
		ApiRequest request = new ApiRequest("InversionesEspeciesPorPanel", "inversiones", "GET", "/v1/especies", contexto);
		request.query("idPanel", idPanel);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Especies.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Especies datos = get(contexto, "7");
		imprimirResultado(contexto, datos);
	}
}
