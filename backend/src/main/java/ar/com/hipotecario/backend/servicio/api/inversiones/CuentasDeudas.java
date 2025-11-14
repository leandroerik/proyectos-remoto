package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.CuentasDeudas.CuentDeuda;

public class CuentasDeudas extends ApiObjetos<CuentDeuda> {

	/* ========== ATRIBUTOS ========== */
	public static class CuentDeuda extends ApiObjeto {
		public String codigoCliente;
		public String tipoProducto;
		public String categoriaProducto;
		public String numeroProducto;
		public String moneda;
		public String tipo;
		public String garantias;
		public Fecha fechaInicio;
		public Fecha fechaVencimiento;
		public BigDecimal montoIntereses;
		public BigDecimal montoImpuestos;
		public BigDecimal montoComisiones;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_GetDeudas
	public static CuentasDeudas get(Contexto contexto, String idEmpresas, String tipoConsulta, String secuencial, Boolean paginar) {
		ApiRequest request = new ApiRequest("InversionesCuentasDeudas", "inversiones", "GET", "/v1/cuentas/deudas", contexto);
		request.query("idEmpresas", idEmpresas);
		request.query("tipoConsulta", tipoConsulta);
		request.query("paginar", paginar);
		request.query("secuencial", secuencial);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CuentasDeudas.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		CuentasDeudas datos = get(contexto, "354", "D", "0", true);
		imprimirResultado(contexto, datos);
	}
}
