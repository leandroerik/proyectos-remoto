package ar.com.hipotecario.backend.servicio.api.cuentas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class DepositoCuenta extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String codigoCliente;
	public String tipoProducto;
	public String cuenta;
	public String titular;
	public String idMoneda;
	public String nroCuentaCobis;
	public String codigoBanca;
	public String descripcionBanca;
	public String idProducto;
	public Boolean depositoCheque;
	public Boolean bloqueoDepositos;
	public Boolean bloqueoRetiros;

	/* ========== SERVICIOS ========== */
	// API-Cuentas_ValidarCuentaDeposito
	static DepositoCuenta get(Contexto contexto, String id) {
		ApiRequest request = new ApiRequest("CuentaDeposito", "cuentas", "GET", "/v1/cuentas/{id}/depositos", contexto);
		request.path("id", id);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(DepositoCuenta.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		DepositoCuenta datos = get(contexto, "402900000640773");
		imprimirResultado(contexto, datos);
	}
}
