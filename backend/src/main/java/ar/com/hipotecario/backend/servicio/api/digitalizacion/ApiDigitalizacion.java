package ar.com.hipotecario.backend.servicio.api.digitalizacion;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.servicio.api.digitalizacion.EnvioDocumentos.NuevoEnvioDocumento;

// http://api-digitalizacion-microservicios-desa.appd.bh.com.ar/
public class ApiDigitalizacion extends Api {

	/* ========== CONSTANTES ========== */
	public static String X_HANDLE = "X-Handle";

	/* ========== Api Digitalización ========== */

	// POST /v1/documentos/{idTributario}
	public static Futuro<EnvioDocumentos> guardarDocumentacion(Contexto contexto, NuevoEnvioDocumento nuevoDocumento) {
		return futuro(() -> EnvioDocumentos.post(contexto, nuevoDocumento));
	}

	// GET /v1/documentos/{idDocumento}
	public static Futuro<EnvioDocumentos> consultarDocumentacion(Contexto contexto, String idDocumento) {
		return futuro(() -> EnvioDocumentos.get(contexto, idDocumento));
	}
}
