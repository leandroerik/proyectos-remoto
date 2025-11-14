package ar.com.hipotecario.backend.servicio.api.seguridad;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;

public class LoginGire extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String mensaje;
	/* ========== SERVICIOS ========== */	
	public static LoginGire post(Contexto contexto, String cuit, String nombre, String officeBankingId) {
		ApiRequest request = new ApiRequest("GireLoginOfbk", "seguridad", "POST", "/v1/logingire", contexto);
		request.body("cuit", cuit);
		request.body("nombre", nombre);
		Objeto objeto = new Objeto();
		objeto.set("officeBankingId",officeBankingId);
		request.body("usuario", objeto);
		ApiResponse response = request.ejecutar();
		return response.crear(LoginGire.class, response);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		String test = "post";
		if (test.equals("post")) {
			String cuit = "33715900209"; 
			String nombre = "FIDEICOMISO PREDIO LOMAS DE ZAMORA TORRE";
			String officeBankingId = "Echeq3";
			Contexto contexto = contexto("OB", "desarrollo");
			LoginGire result = LoginGire.post(contexto, cuit, nombre, officeBankingId);
			System.out.print("Result: ");
			imprimirResultado(contexto,result);
		}
	}
}