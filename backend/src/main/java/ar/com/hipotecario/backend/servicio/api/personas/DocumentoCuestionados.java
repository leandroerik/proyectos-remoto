package ar.com.hipotecario.backend.servicio.api.personas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.personas.DocumentoCuestionados.DocumentoCuestionado;

public class DocumentoCuestionados extends ApiObjetos<DocumentoCuestionado> {

	public static String GET_DOC_CUESTIONADOS = "docCuestionados";
	public static String TIPO_DOC = "DNI-TARJETA";

	/* ========== ATRIBUTOS ========== */
	public static class DocumentoCuestionado extends ApiObjeto {
		public String cuestionado;
		public String ejemplar;
		public String error;
	}

	/* ========== SERVICIOS ========== */
	public static DocumentoCuestionado get(Contexto contexto, String nroDoc, String sexo) {
		ApiRequest request = new ApiRequest(GET_DOC_CUESTIONADOS, ApiPersonas.API, "GET", "/documentoscuestionados", contexto);
		request.query("documento_tipo", TIPO_DOC);
		request.query("documento_nro", nroDoc);
		request.query("sexo", sexo);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(DocumentoCuestionado.class);
	}

}
