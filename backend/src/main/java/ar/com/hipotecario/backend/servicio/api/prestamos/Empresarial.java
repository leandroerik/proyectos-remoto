package ar.com.hipotecario.backend.servicio.api.prestamos;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class Empresarial extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String numeroPrestamo;
	public String idMoneda;
	public String periodicidadPagoIntereses;
	public String tipoAmortizacion;
	public String tipoPrestamo;
	public String descripcionTipoPrestamo;
	public String tipoTasa;
	public String cantidadDiasAtraso;
	public String descripcionSucursal;
	public String idSucursal;
	public Integer cantidadCuotas;
	public Integer cantidadCuotasRestantes;
	public Fecha fechaAlta;
	public Fecha fechaUltimaCuota;
	public BigDecimal montoInicial;
	public BigDecimal montoDeuda;
	public BigDecimal tasa;
	public BigDecimal deudaTotal;
	public Boolean marcaProrrogado;
	public Boolean marcaGarantiasVinculadas;
	public List<Garantias> garantias;

	public static class Garantias {
		public String tipoGarantia;
		public String clasificacion;
		public String idMoneda;
		public String clase;
		public String estado;
		public Fecha fechaVencimiento;
		public Fecha fechaPrescripcion;
		public BigDecimal valorInstrumento;
	}

	/* ========== SERVICIOS ========== */
	// API-Prestamos_ConsultaPrestamoEmpresarial
	public static Empresarial get(Contexto contexto, String id) {
		ApiRequest request = new ApiRequest("PrestamoEmpresarial", "prestamos", "GET", "/v1/prestamosempresarial/{id}", contexto);
		request.path("id", id);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Empresarial.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Empresarial datos = get(contexto, "0280295712");
		imprimirResultado(contexto, datos);
	}
}
