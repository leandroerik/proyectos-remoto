package ar.com.hipotecario.backend.servicio.api.firmas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class FirmanteOB extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String esquemaHabilitado;
	public String tipoFirma;
	public String descripcion;

	/* ========== SERVICIOS ========== */
	// API firmas core
	public static FirmanteOB get(Contexto contexto, String cedruc, String cuenta, String firmante, String monto, String funcOB) {
		ApiRequest request = new ApiRequest("FirmanteOB", "firmas", "GET", "/esquemas/{cedruc}/firmantes", contexto);
		request.path("cedruc", cedruc);
		request.query("cuenta", cuenta);
		request.query("firmante", firmante);
		request.query("monto", monto);
		request.query("funcOB", funcOB);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(FirmanteOB.class, response.objetos(0));
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		Contexto contexto = contexto("OB", "desarrollo");
		FirmanteOB datos = get(contexto, "30509300700", "300000000367653", "20103574219", "600000", "7");
		// FirmanteOB datos = get(contexto, "30710870299",
		// "302700000374889","20346929562","600000","16");
		imprimirResultado(contexto, datos);
	}
}