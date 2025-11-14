package ar.com.hipotecario.backend.servicio.api.link;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.link.Rubros.Rubro;

public class Rubros extends ApiObjetos<Rubro> {

	/* ========== ATRIBUTOS ========== */
	public static class Rubro extends ApiObjeto {
		public String codigo;
		public String descripcion;
		public String descripcionAbreviada;
	}

	/* =============== SERVICIOS ================ */
	// API-Link_ConsultaRubros-PagosServicios
	public static Rubros get(Contexto contexto, String numeroTarjeta) {
		ApiRequest request = new ApiRequest("LinkGetRubros", "link", "GET", "/v1/servicios/{numeroTarjeta}/rubros", contexto);
		request.path("numeroTarjeta", numeroTarjeta);

		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Rubros.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Rubros datos = get(contexto, "4998590015391523");
		imprimirResultado(contexto, datos);
	}
}
