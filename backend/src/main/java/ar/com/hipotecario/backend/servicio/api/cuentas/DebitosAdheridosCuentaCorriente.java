package ar.com.hipotecario.backend.servicio.api.cuentas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.DebitosAdheridosCuentaCorriente.DebitoAdheridoCuentaCorriente;

public class DebitosAdheridosCuentaCorriente extends ApiObjetos<DebitoAdheridoCuentaCorriente> {

	/* ========== ATRIBUTOS ========== */
	public static class DebitoAdheridoCuentaCorriente extends ApiObjeto {
		public String tipoDebito;
		public String idServicio;
		public String descServicio;
		public String estado;
		public Fecha fechaAltaDebito;
	}

	/* ========== SERVICIOS ========== */
	// API-Cuentas_ConsultaDebitosAdheridos
	static DebitosAdheridosCuentaCorriente get(Contexto contexto, String idCuenta) {
		ApiRequest request = new ApiRequest("CuentasCuentaCorrienteDebitosAdheridos", "cuentas", "GET", "/v1/cuentascorrientes/{idcuenta}/debitosadheridos", contexto);
		request.path("idcuenta", idCuenta);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(DebitosAdheridosCuentaCorriente.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		DebitosAdheridosCuentaCorriente datos = get(contexto, "304500000022494");
		imprimirResultado(contexto, datos);
	}
}
