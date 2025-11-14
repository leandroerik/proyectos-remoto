package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.PanelesEspecies.PanelEspecial;

public class PanelesEspecies extends ApiObjetos<PanelEspecial> {

	/* ========== ATRIBUTOS ========== */
	public static class PanelEspecial extends ApiObjeto {
		public String idPanel;
		public String descripcion;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_PanelesEspecies
	public static PanelesEspecies get(Contexto contexto) {
		ApiRequest request = new ApiRequest("InversionesPanelesEspecies", "inversiones", "GET", "/v1/panelesespecies", contexto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(PanelesEspecies.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		PanelesEspecies datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
