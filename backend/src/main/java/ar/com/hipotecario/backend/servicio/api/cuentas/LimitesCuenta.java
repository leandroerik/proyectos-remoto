package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.LimitesCuenta.LimiteCuenta;

public class LimitesCuenta extends ApiObjetos<LimiteCuenta> {

	/* ========== ATRIBUTOS ========== */
	public static class LimiteCuenta extends ApiObjeto {
		public String idCliente;
		public String idMoneda;
		public Fecha fecha;
		public BigDecimal importe;
	}

	/* ========== SERVICIOS ========== */
	// API-Cuentas_ConsultaRegistrosTransferenciaEspecial
	static LimitesCuenta get(Contexto contexto, String idcuenta, String idCliente, Fecha fechaDesde, Fecha fechaHasta, String idMoneda) {
		ApiRequest request = new ApiRequest("CuentasGetLimites", "cuentas", "GET", "/v1/cuentas/{idcuenta}/limites", contexto);

		request.path("idcuenta", idcuenta);
		request.query("idcliente", idCliente);
		request.query("fechadesde", fechaDesde.string("yyyy-MM-dd"));
		request.query("fechahasta", fechaHasta.string("yyyy-MM-dd"));
		request.query("idmoneda", idMoneda);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(LimitesCuenta.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fechaDesde = new Fecha("2019-01-01", "yyyy-MM-dd");
		Fecha fechaHasta = new Fecha("2019-12-30", "yyyy-MM-dd");
		LimitesCuenta datos = get(contexto, "404500000745801", "133366", fechaDesde, fechaHasta, "80");
		imprimirResultado(contexto, datos);
	}
}
