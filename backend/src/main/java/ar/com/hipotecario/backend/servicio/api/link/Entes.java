package ar.com.hipotecario.backend.servicio.api.link;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.link.Entes.Ente;

public class Entes extends ApiObjetos<Ente> {

	/* ========== ATRIBUTOS ========== */
	public static class Rubro extends ApiObjeto {
		public String codigo;
		public String descripcion;
		public String descripcionAbreviada;
	}

	public static class Ente extends ApiObjeto {
		public String codigo;
		public String descripcion;
		public Boolean isBaseDeuda;
		public Boolean isMultipleConcepto;
		public Boolean isIngresoReferencia;
		public Boolean isIngresoImporte;
		public Boolean isHabilitado;
		public Rubro rubro;
	}

	/* =============== SERVICIOS ================ */
	// API-Link_ConsultaEntePorRubro-PagosServicios
	public static Entes get(Contexto contexto, String numeroTarjeta, String idRubro) {
		ApiRequest request = new ApiRequest("LinkGetEntes", "link", "GET", "/v1/servicios/{numeroTarjeta}/entes/{idRubro}", contexto);
		request.path("numeroTarjeta", numeroTarjeta);
		request.path("idRubro", idRubro);

		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Entes.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Entes datos = get(contexto, "4998590015391523", "01");
		imprimirResultado(contexto, datos);
	}
}
