package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class BloqueoSaldoCuentaCorriente extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String idProducto;
	public String cuenta;
	public String cbu;
	public String estado;
	public String moneda;
	public BigDecimal acuerdo;
	public BigDecimal bloqueos;
	public BigDecimal saldoAGirar;
	public BigDecimal disponible;

	/* ========== SERVICIOS ========== */
	static BloqueoSaldoCuentaCorriente get(Contexto contexto, String idCuenta) {
		return get(contexto, idCuenta, null);
	}

	// API-Cuentas_ConsultaSaldosBloqueosCuentaCorriente
	static BloqueoSaldoCuentaCorriente get(Contexto contexto, String idCuenta, Fecha fecha) {
		ApiRequest request = new ApiRequest("CuentasCuentasCorrientesSaldosBloqueos", "cuentas", "GET", "/v2/cuentascorrientes/{idcuenta}/saldosbloqueos", contexto);
		request.path("idcuenta", idCuenta);
		if (fecha != null)
			request.query("fecha", fecha.string("yyyy-MM-dd"));
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(BloqueoSaldoCuentaCorriente.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		BloqueoSaldoCuentaCorriente datos = get(contexto, "304500000022494");
		imprimirResultado(contexto, datos);
	}
}
