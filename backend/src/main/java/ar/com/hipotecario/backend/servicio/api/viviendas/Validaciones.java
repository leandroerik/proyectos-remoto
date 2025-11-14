package ar.com.hipotecario.backend.servicio.api.viviendas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class Validaciones extends ApiObjetos<ConsultaPersona> {

	public static String GET_VALIDACIONES = "Validaciones";

	/* ========== SERVICIOS ========== */
	// solicitudesGET
	public static ConsultaPersona get(Contexto contexto, String numeroTramite, String numeroDocumento, String sexo) {
		ApiRequest request = new ApiRequest(GET_VALIDACIONES, ApiViviendas.API, "GET", "/v1/validaciones", contexto);
		request.query("idtramite", numeroTramite);
		request.query("dni", numeroDocumento);
		request.query("sexo", sexo);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(ConsultaPersona.class);
	}

}
