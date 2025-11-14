package ar.com.hipotecario.backend.servicio.api.seguridad;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class NuevoOperadorOB extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String mensaje_ret;
	public String cod_ret;

	/* ========== SERVICIOS ========== */
	// API-Seguridad_AltaOperadorOB
	public static NuevoOperadorOB post(Contexto contexto, String numeroDocumentoInicial, String numeroDocumento, Long cuit, String cuentas, String menues) {

		ApiRequest request = new ApiRequest("AltaOperadorOB", "seguridad", "POST", "/v1/altaoperadorob", contexto);

		request.body("emp_cuit", cuit);
		request.body("usu_documento", numeroDocumento);
		request.body("usu_documento_ope_inicial", numeroDocumentoInicial);
		request.body("cuentas", cuentas);
		request.body("menues", menues);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("DOCUMENTO_INVALIDO", response.contains("El documento del operador inicial no es valido"), request, response);
		ApiException.throwIf("DOCUMENTO_OPERADOR_INVALIDO", response.contains("El documento del operador no es valido"), request, response);
		ApiException.throwIf("OPERADOR_EXISTENTE", response.contains("El usuario ya es operador de la empresa"), request, response);
		ApiException.throwIf("OPERADOR_INICIAL_INEXISTENTE", response.contains("El nro. de documento no corresponde a un operador inicial de la empresa"), request, response);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(NuevoOperadorOB.class, response);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		Contexto contexto = contexto("OB", "desarrollo");
		NuevoOperadorOB datos = post(contexto, "29698692", "64547134", Long.valueOf("30612929455"), "300-111-12312", "1,2,3,4");
		imprimirResultado(contexto, datos);
	}
}