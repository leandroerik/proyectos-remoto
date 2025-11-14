package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class BloqueoSaldosCajaAhorro extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String idProducto;
	public String cuenta;
	public BigDecimal acuerdo;

	/* ========== SERVICIOS ========== */
	// API-Cuentas_ConsultaSaldosBloqueosCajaAhorro
	static BloqueoSaldosCajaAhorro get(Contexto contexto, String idCuenta) {
		return get(contexto, idCuenta, null);
	}

	static BloqueoSaldosCajaAhorro get(Contexto contexto, String idCuenta, Fecha fecha) {
		ApiRequest request = new ApiRequest("CuentasCajaAhorroSaldosBloqueos", "cuentas", "GET", "/v2/cajasahorros/{idcuenta}/saldosbloqueos", contexto);
		request.path("idcuenta", idCuenta);
		if (fecha != null)
			request.query("fecha", fecha.string("yyyy-MM-dd"));
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(BloqueoSaldosCajaAhorro.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fecha = new Fecha("2020-01-01", "yyyy-MM-dd");
		BloqueoSaldosCajaAhorro datos = get(contexto, "1174084", fecha);
		imprimirResultado(contexto, datos);
	}
}
