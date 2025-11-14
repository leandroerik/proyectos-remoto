package ar.com.hipotecario.backend.servicio.api.cuentas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.Chequeras.Chequera;

public class Chequeras extends ApiObjetos<Chequera> {

	/* ========== ATRIBUTOS ========== */
	public static class Chequera extends ApiObjeto {
		public String estado;
		public String sucursal;
		public Integer nroPrimerCheque;
		public Integer nroUltimoCheque;
	}

	/* ========== SERVICIOS ========== */
	// API-Cuentas_ConsultaChequeras
	static Chequeras get(Contexto contexto, String idCuenta) {
		ApiRequest request = new ApiRequest("CuentasCuentaCorrienteChequeras", "cuentas", "GET", "/v1/cuentascorrientes/{idcuenta}/chequeras", contexto);
		request.path("idcuenta", idCuenta);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Chequeras.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Chequeras datos = get(contexto, "260");
		imprimirResultado(contexto, datos);
	}
}
