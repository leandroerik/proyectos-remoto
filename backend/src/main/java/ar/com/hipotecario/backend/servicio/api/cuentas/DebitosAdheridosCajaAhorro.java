package ar.com.hipotecario.backend.servicio.api.cuentas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.DebitosAdheridosCajaAhorro.DebitoAdheridoCajaAhorro;

public class DebitosAdheridosCajaAhorro extends ApiObjetos<DebitoAdheridoCajaAhorro> {

	/* ========== ATRIBUTOS ========== */
	public static class DebitoAdheridoCajaAhorro extends ApiObjeto {
		public String tipoDebito;
		public String idServicio;
		public String descServicio;
		public String estado;
		public Fecha fechaAltaDebito;
	}

	/* ========== SERVICIOS ========== */
	// API-Cuentas_ConsultaDebitosAdheridos
	static DebitosAdheridosCajaAhorro get(Contexto contexto, String idCuenta) {
		ApiRequest request = new ApiRequest("CajasAhorroDebitosAdheridosByIdCuenta", "cuentas", "GET", "/v1/cajasahorros/{idcuenta}/debitosadheridos", contexto);
		request.path("idcuenta", idCuenta);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(DebitosAdheridosCajaAhorro.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		DebitosAdheridosCajaAhorro datos = get(contexto, "404500000745801");
		imprimirResultado(contexto, datos);
	}
}
