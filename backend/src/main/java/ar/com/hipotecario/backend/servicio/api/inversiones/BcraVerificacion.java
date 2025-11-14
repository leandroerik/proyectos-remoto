package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class BcraVerificacion extends ApiObjeto {

	/* ========= ATRIBUTOS ========= */
	public String cuil;
	public Boolean valido;

	/* ========== SERVICIOS ========== */
	// API-Inversiones_BCRAVerificador
	public static BcraVerificacion get(Contexto contexto, String cuil) {
		ApiRequest request = new ApiRequest("InversionesIndicesSectorialesDelay", "inversiones", "GET", "/v1/bcra/verificacion/{cuil}", contexto);
		request.path("cuil", cuil);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(BcraVerificacion.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		BcraVerificacion datos = get(contexto, "20309574592");
		imprimirResultado(contexto, datos);
	}
}
