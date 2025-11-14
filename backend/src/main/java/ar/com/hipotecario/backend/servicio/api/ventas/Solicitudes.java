package ar.com.hipotecario.backend.servicio.api.ventas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class Solicitudes extends ApiObjetos<Solicitud> {

	public static String GET_SOLICITUDES = "Solicitudes";

	/* ========== SERVICIOS ========== */
	// solicitudesGET
	public static Solicitudes get(Contexto contexto, String cuit) {
		ApiRequest request = new ApiRequest(GET_SOLICITUDES, ApiVentas.API, "GET", "/solicitudes", contexto);
		request.query("cuil", cuit);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(Solicitudes.class, response.objetos(ApiVentas.DATOS));
	}

	/* ========== METODOS ========== */
	public Solicitud solicitudPaquete() {
		return first(s -> s.paquete() != null, (a, b) -> a.FechaAlta.esAnterior(b.FechaAlta) ? 1 : -1);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		String test = "get";

		if (test.equals("get")) {
			Solicitudes datos = get(contexto, "27322299244");
			imprimirResultadoApiVentas(contexto, datos);
		}
	}
}
