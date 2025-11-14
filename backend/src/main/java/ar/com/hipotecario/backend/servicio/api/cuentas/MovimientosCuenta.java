package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.MovimientosCuenta.MovimientoCuenta;

public class MovimientosCuenta extends ApiObjetos<MovimientoCuenta> {

	/* ========== ATRIBUTOS ========== */
	public static class MovimientoCuenta extends ApiObjeto {
		public String moneda;
		public String fecha;
		public BigDecimal monto;
		public String descripcion;
		public Integer secuencial;
		public String cuenta;

		public BigDecimal getMonto() {
			return monto;
		}

		public void setMonto(BigDecimal monto) {
			this.monto = monto;
		}

	}

	/* ========== SERVICIOS ========== */
	static Boolean validaSoN(String valid) {
		return valid.equals("S") || valid.equals("N") ? true : false;
	}

	// API-Cuentas_ConsultaUltimosMovimientos
	static MovimientosCuenta get(Contexto contexto, String cuentas, String file, String from, String validaCuenta) {
		ApiRequest request = new ApiRequest("CuentasGetMovimientosConsolidados", "cuentas", "GET", "/v1/cuentas/movimientos", contexto);

		if (!validaSoN(file) || !validaSoN(from) || !validaSoN(validaCuenta)) {
			throw new ApiException(request, null);
		}

		request.query("file", file);
		request.query("from", from);
		request.query("validaCuenta", validaCuenta);
		request.query("cuentas", cuentas);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(MovimientosCuenta.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("OB", "desarrollo");
		MovimientosCuenta datos = get(contexto, "404500000745801,204500010233259,304500000022494,404500010244503,404500018801414,", "N", "N", "N");
		imprimirResultado(contexto, datos);
	}
}
