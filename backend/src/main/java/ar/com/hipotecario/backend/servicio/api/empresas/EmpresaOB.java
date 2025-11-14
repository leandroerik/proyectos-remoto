package ar.com.hipotecario.backend.servicio.api.empresas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class EmpresaOB extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String cuit;
	public String razonSocial;
	public String idCobis;
	public String estado;

	/* ========== SERVICIOS ========== */
	// API-Empresas_DatosEmpresa
	public static EmpresaOB get(Contexto contexto, String cuit, String cuil) {
		ApiRequest request = new ApiRequest("EmpresaOB", "empresas", "GET", "/v1/empresas/{emp_cuit}/{usu_cuit}", contexto);
		request.body("ip", contexto.ip());
		request.path("emp_cuit", cuit);
		request.path("usu_cuit", cuil);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(EmpresaOB.class, response.objeto("DatosEmpresa"));
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		Contexto contexto = contexto("OB", "desarrollo");
		EmpresaOB datos = get(contexto, "30527677331", "20309574592");
		imprimirResultado(contexto, datos);
	}
}
