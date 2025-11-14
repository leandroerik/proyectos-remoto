package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.DocumentosRewards.CodigoDocumentoRewards;

public class DocumentosRewards extends ApiObjetos<CodigoDocumentoRewards> {

	/* ========== ATRIBUTOS ========== */
	public static class CodigoDocumentoRewards extends ApiObjeto {
		public String codigoDocumentoRewards;
		public String descripcionDocumentoRewards;
	}

	/* ========== SERVICIOS ========== */
	// API-Catalogo_ConsultaCodigosDocumentosRewards
	static DocumentosRewards get(Contexto contexto) {
		ApiRequest request = new ApiRequest("DocumentosRewards", "catalogo", "GET", "/v1/codigos/documentosrewards", contexto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(DocumentosRewards.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		DocumentosRewards datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
