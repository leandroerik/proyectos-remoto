package ar.com.hipotecario.backend.servicio.api.prestamos;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.prestamos.PrestamosCuotas.PrestamoCuota;

public class PrestamosCuotas extends ApiObjetos<PrestamoCuota> {

	/* ========== ATRIBUTOS ========== */
	public static class PrestamoCuota extends ApiObjeto {
		public BigDecimal saldoCapital;
		public BigDecimal capital;
		public BigDecimal ajusteCapital;
		public BigDecimal otrosCapitales;
		public BigDecimal intereses;
		public BigDecimal otrosIntereses;
		public BigDecimal monto;
		public BigDecimal otrosRubros;
		public BigDecimal impuestos;
		public BigDecimal total;
		public BigDecimal montoBeneficios;
		public BigDecimal exigible;
		public BigDecimal gracia;
		public BigDecimal capitalizado;
		public BigDecimal extraordinario;
		public BigDecimal ajusteCapitalExtraordinario;
		public BigDecimal ivaPercepcion;
		public BigDecimal quitaCapital;
		public BigDecimal cobrado;
		public String numero;
		public String estado;
		public String cuotaNSP;
		public Fecha fechaVencimiento;
	}

	/* ========== SERVICIOS ========== */
	// API-Prestamos_ConsultaCuotasPrestamo
	public static PrestamosCuotas get(Contexto contexto, String id, Fecha fechaDesde, Fecha fechaHasta) {
		return get(contexto, id, fechaDesde, fechaHasta, null);
	}

	public static PrestamosCuotas get(Contexto contexto, String id, String cuota) {
		return get(contexto, id, null, null, cuota);
	}

	public static PrestamosCuotas get(Contexto contexto, String id, Fecha fechaDesde, Fecha fechaHasta, String cuota) {
		ApiRequest request = new ApiRequest("PrestamosCuotas", "prestamos", "GET", "/v1/prestamos/{id}/cuotas", contexto);
		request.path("id", id);
		if (cuota != null)
			request.query("cuota", cuota);

		if (fechaDesde != null && fechaHasta != null) {
			request.query("fechahasta", fechaHasta.string("yyyy-MM-dd"));
			request.query("fechadesde", fechaDesde.string("yyyy-MM-dd"));
		}

		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(PrestamosCuotas.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fechaDesde = new Fecha("2021-01-01", "yyyy-MM-dd");
		Fecha fechaHasta = new Fecha("2021-07-01", "yyyy-MM-dd");
		PrestamosCuotas datos = get(contexto, "0002075035", fechaDesde, fechaHasta, "10");
//		PrestamosCuotas datos = get(contexto, "0002075035", fechaDesde, fechaHasta);
//		PrestamosCuotas datos = get(contexto, "0002075035", "10");
		imprimirResultado(contexto, datos);
	}
}
