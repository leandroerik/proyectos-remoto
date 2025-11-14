package ar.com.hipotecario.backend.servicio.api.plazosfijos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class SolicitudPrecancelar extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */

	/* ========== SERVICIOS ========== */
	public static SolicitudPrecancelar getSolicitud(Contexto contexto, String idCobis) {
		ApiRequest request = new ApiRequest("SolicitudPrecancelar", "plazosfijos", "GET", "/v1/solicitudPrecancelar", contexto);
		request.query("idCobis", idCobis);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(SolicitudPrecancelar.class);
	}

	public static SolicitudPrecancelar patch(Contexto contexto, String idCobis) {
		ApiRequest request = new ApiRequest("SolicitudPrecancelarPatch", "plazosfijos", "PATCH", "/v1/solicitudPrecancelar/{idCobis}", contexto);
		request.path("idCobis", idCobis);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(SolicitudPrecancelar.class);
	}

	public static SolicitudPrecancelar patchReversa(Contexto contexto, String idCobis) {
		ApiRequest request = new ApiRequest("SolicitudPrecancelar", "plazosfijos", "PATCH", "/v1/solicitudPrecancelar/reversa/{idCobis}", contexto);
		request.path("idCobis", idCobis);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(SolicitudPrecancelar.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		String metodo = "patch";

		if (metodo.equals("getSolicitud")) {
			SolicitudPrecancelar datos = getSolicitud(contexto, "6155045");
			imprimirResultado(contexto, datos);
		}
	}
}
