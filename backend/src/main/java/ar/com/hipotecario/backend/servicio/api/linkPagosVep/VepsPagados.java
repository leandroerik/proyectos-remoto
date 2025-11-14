package ar.com.hipotecario.backend.servicio.api.linkPagosVep;

import java.math.BigDecimal;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class VepsPagados extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public Integer paginaSiguiente;
	public Integer cantidad;
	public List<Vep> veps;

	public static class Vep extends ApiObjeto {
		public Long fechaHoraCreacion;
		public String pagoDesc;
		public Usuario usuario;
		public Autorizante autorizante;
		public Contribuyente contribuyente;
		public String establecimiento;
		public String conceptoDesc;
		public String subconceptoDesc;
		public String periodoFiscal;
		public String anticipoCuota;
		public BigDecimal importe;
		public String pagoBancoEmisor;
		public String pagoNroTerminal;
		public String pagoNroSec;
		public Fecha pagoFecha;
		public Fecha pagoFechaPost;
		public String pagoHora;
		public String pagoTipoCuenta;
		public String pagoNroCuenta;
		public String pagoCodAbre;
		public String nroVepOriginal;
		public String pagoEstado;
		public String vepEstado;

		public Fecha fechaHoraCreacion() {
			return new Fecha(fechaHoraCreacion);
		}
	}

	public static class Usuario {
		public String idTributario;
	}

	public static class Autorizante {
		public String idTributario;
	}

	public static class Contribuyente {
		public String idTributario;
	}

	/* ========== SERVICIOS ========== */
	public static VepsPagados get(Contexto contexto, Fecha fechaDesde, String idTributarioCliente, String idTributarioContribuyente, String idTributarioOriginante, String idTributarioEmpresa, String maxCantidad, String numeroTarjeta, String numeroVep, String tipoConsultaLink) {
		return get(contexto, fechaDesde, null, idTributarioCliente, idTributarioContribuyente, idTributarioEmpresa, idTributarioOriginante, maxCantidad, numeroTarjeta, numeroVep, null, tipoConsultaLink);
	}

	public static VepsPagados get(Contexto contexto, Fecha fechaDesde, Fecha fechaHasta, String idTributarioCliente, String idTributarioContribuyente, String idTributarioEmpresa, String idTributarioOriginante, String maxCantidad, String numeroTarjeta, String numeroVep, String pagina, String tipoConsultaLink) {
		ApiRequest request = new ApiRequest("LinkGetVepsPagados", "veps", "GET", "/v1/veps/{idTributarioCliente}/pagados", contexto);
		request.path("idTributarioCliente", idTributarioCliente);
		request.query("fechaDesde", fechaDesde == null ? null : fechaDesde.string("yyyy-MM-dd"));
		request.query("fechaHasta", fechaHasta == null ? null : fechaHasta.string("yyyy-MM-dd"));
		request.query("idTributarioContribuyente", idTributarioContribuyente);
		request.query("idTributarioEmpresa", idTributarioEmpresa);
		request.query("idTributarioOriginante", idTributarioOriginante);
		request.query("maxCantidad", maxCantidad);
		request.query("numeroTarjeta", numeroTarjeta);
		request.query("numeroVep", numeroVep);
		request.query("pagina", pagina);
		request.query("tipoConsultaLink", tipoConsultaLink);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204, 404), request, response);
		VepsPagados vepsPagados = response.crear(VepsPagados.class);
		return vepsPagados;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		VepsPagados datos = get(contexto, new Fecha("2019-01-01", "dd/MM/yyyy"), new Fecha("2019-12-31", "dd/MM/yyyy"), "20105176512", null, null, null, null, "4998590266707708", null, null, "1");
		imprimirResultado(contexto, datos);
	}
}
