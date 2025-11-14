package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.DeudasCuenta.DeudaCuenta;

public class DeudasCuenta extends ApiObjetos<DeudaCuenta> {

	/* ========== ATRIBUTOS ========== */
	public static class DeudaCuenta extends ApiObjeto {
		public String numeroProducto;
		public String tipoProducto;
		public String categoriaProducto;
		public String moneda;
		public Fecha fechaInicio;
		public Fecha fechaVencimiento;
		public String tipo;
		public BigDecimal monto;
		public BigDecimal montoIntereses;
		public BigDecimal montoImpuestos;
		public BigDecimal montoComisiones;
		public String garantias;
		public String descripcionGarantias;
	}

	/* ========== SERVICIOS ========== */
	// API-Cuentas_GetDeudas
	static DeudasCuenta get(Contexto contexto, String idEmpresas, Boolean paginar, String secuencial) {
		ApiRequest request = new ApiRequest("CuentasGetDeudas", "cuentas", "GET", "/v1/cuentas/deudas", contexto);
		request.query("idEmpresas", idEmpresas);
		request.query("paginar", paginar);
		request.query("tipoConsulta", "P");
		request.query("secuencial", secuencial);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(DeudasCuenta.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "desarrollo");
		DeudasCuenta datos = get(contexto, "4373070", false, "0");
		imprimirResultado(contexto, datos);
	}
}
