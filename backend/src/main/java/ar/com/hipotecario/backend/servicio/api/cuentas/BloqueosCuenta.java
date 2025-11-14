package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.BloqueosCuenta.BloqueoCuenta;

public class BloqueosCuenta extends ApiObjetos<BloqueoCuenta> {

	/* ========== ATRIBUTOS ========== */
	public static class BloqueoCuenta extends ApiObjeto {
		public String numero_causal;
		public String desc_causal;
		public BigDecimal monto_bloqueo;
	}

	/* ========== SERVICIOS ========== */
	// API-Cuentas_ConsultaBloqueoSaldoMicrocredito
	static Boolean validTipoCuenta(String tipoCuenta) {
		return tipoCuenta.equals("AHO") || tipoCuenta.equals("CC") ? true : false;
	}

	static BloqueosCuenta get(Contexto contexto, String id, String tipoCuenta) {
		ApiRequest request = new ApiRequest("CuentasBloqueos", "cuentas", "GET", "/v1/cuentas/{id}/bloqueos", contexto);

		if (!validTipoCuenta(tipoCuenta))
			throw new ApiException(request, null);

		request.path("id", id);
		request.query("tipoCuenta", tipoCuenta);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(BloqueosCuenta.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		BloqueosCuenta datos = get(contexto, "402900000640773", "AHO");
		imprimirResultado(contexto, datos);
	}
}
