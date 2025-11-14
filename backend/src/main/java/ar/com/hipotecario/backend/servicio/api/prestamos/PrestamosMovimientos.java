package ar.com.hipotecario.backend.servicio.api.prestamos;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class PrestamosMovimientos extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String tipo;
	public Desembolo desembolo;
	public List<Pagos> pagos;

	public static class Desembolo {
		public String nroPrestamo;
		public String tipoOperacion;
		public String formaPago;
		public String cuenta;
		public Integer cuotas;
		public BigDecimal montoAprobado;
		public Fecha fechaLiquidacion;
	}

	public static class Pagos {
		public String nroPrestamo;
		public String tasa;
		public Integer nroCuotaPago;
		public Integer cuotasRestantes;
		public BigDecimal montoCuotaPago;
	}

	/* ========== SERVICIOS ========== */
	// API-Prestamos_CuentasMovimientos
	public static PrestamosMovimientos get(Contexto contexto, String numCuenta, Fecha fechaMovimiento, String productoCobis, String secuencial) {
		ApiRequest request = new ApiRequest("PrestamosCuentaMovimiento", "prestamos", "GET", "/v1/prestamos/{numCuenta}/movimientos", contexto);
		request.path("numCuenta", numCuenta);
		request.query("fechaMovimiento", fechaMovimiento.string("yyyy-MM-dd"));
		request.query("productoCobis", productoCobis);
		request.query("secuencial", secuencial);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(PrestamosMovimientos.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fechaMovimiento = new Fecha("2020-07-31", "yyyy-MM-dd");
		PrestamosMovimientos datos = get(contexto, "401500014851450", fechaMovimiento, "4", "571937719");
		imprimirResultado(contexto, datos);
	}
}
