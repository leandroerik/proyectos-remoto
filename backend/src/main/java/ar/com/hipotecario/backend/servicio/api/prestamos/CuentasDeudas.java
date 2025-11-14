package ar.com.hipotecario.backend.servicio.api.prestamos;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.prestamos.CuentasDeudas.CuentaDeuda;

public class CuentasDeudas extends ApiObjetos<CuentaDeuda> {

	/* ========== ATRIBUTOS ========== */
	public static class CuentaDeuda extends ApiObjeto {
		public String categoriaProducto;
		public String codigoCliente;
		public String descripcionCategoriaProducto;
		public String descripcionGarantias;
		public Fecha fechaInicio;
		public Fecha fechaVencimiento;
		public String garantias;
		public String moneda;
		public BigDecimal monto;
		public BigDecimal montoComisiones;
		public BigDecimal montoImpuestos;
		public BigDecimal montoIntereses;
		public String numeroProducto;
		public String tipo;
		public String tipoProducto;
	}

	/* ========== SERVICIOS ========== */
	/* ? SIN DATOS A PROBAR */
	public static CuentasDeudas get(Contexto contexto, String idEmpresas, Boolean paginar, String secuencial, String tipoConsulta) {
		ApiRequest request = new ApiRequest("PrestamoCuentaDeudas", "prestamos", "GET", "/v1/cuentas/deudas", contexto);
		request.query("idEmpresas", idEmpresas);
		request.query("paginar", paginar);
		request.query("secuencial", secuencial);
		request.query("tipoConsulta", tipoConsulta);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CuentasDeudas.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "desarrollo");
		CuentasDeudas datos = get(contexto, "4373070", false, "0", "P");
		imprimirResultado(contexto, datos);
	}
}
