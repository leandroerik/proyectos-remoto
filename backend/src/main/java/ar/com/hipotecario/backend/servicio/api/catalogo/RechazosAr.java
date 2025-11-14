package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.RechazosAr.CodigoRechazoAr;

public class RechazosAr extends ApiObjetos<CodigoRechazoAr> {

	/* ========== ATRIBUTOS ========== */
	public static class CodigoRechazoAr extends ApiObjeto {
		public String codigoRechazoArPlus;
		public String descripcionRechazoArPlus;
	}

	/* ========== SERVICIOS ========== */
	// API-Catalogo_ConsultaCodigosRechazosArPlus
	public static RechazosAr get(Contexto contexto) {
		ApiRequest request = new ApiRequest("RechazosAr", "catalogo", "GET", "/v1/codigos/rechazos_ar", contexto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(RechazosAr.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		RechazosAr datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
