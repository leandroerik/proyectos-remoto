package ar.com.hipotecario.backend.servicio.api.prestamos;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class Preventas extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String tipo;
	public String id;
	public String cuilCodeudor;
	public String nombreCodeudor;
	public String estado;
	public String moneda;
	public String descripcionMoneda;
	public String anticipos;
	public BigDecimal capitalAcumulado;
	public BigDecimal comisionAcumulada;
	public BigDecimal ivaAcumulado;
	public BigDecimal porcentajeAcumulado;
	public BigDecimal montoObligatorio;

	public static class PreventasAnticipos extends ApiObjetos<PreventaAnticipo> {
	}

	public static class PreventaAnticipo extends ApiObjeto {
		public String numero;
		public String estado;
		public Fecha fechaVencimiento;
		public Fecha fechapago;
		public BigDecimal capital;
		public BigDecimal comisiones;
		public BigDecimal ivaComisiones;
		public BigDecimal total;
	}

	/* ========== SERVICIOS ========== */
	// API-Prestamos_ConsultaPreventaCabecera
	public static Preventas getPreventas(Contexto contexto, String id) {
		ApiRequest request = new ApiRequest("PrestamoPreventas", "prestamos", "GET", "/v1/preventas/{id}", contexto);
		request.path("id", id);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Preventas.class);
	}

	/* ========== SERVICIOS ========== */
	// API-Prestamos_ConsultaAnticiposPreventa
	public static PreventasAnticipos getPreventasAnticipos(Contexto contexto, String id, Fecha fechaDesde, Fecha fechaHasta) {
		ApiRequest request = new ApiRequest("PrestamoPreventasAnticipos", "prestamos", "GET", "/v1/preventas/{id}/anticipos", contexto);
		request.path("id", id);
		request.query("fechadesde", fechaDesde.string("yyyy-MM-dd"));
		request.query("fechahasta", fechaHasta.string("yyyy-MM-dd"));
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(PreventasAnticipos.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		String metodo = "preventasAnticipos";

		if (metodo.equals("preventas")) {
			Preventas datos = getPreventas(contexto, "0280334001");
			imprimirResultado(contexto, datos);
		}

		if (metodo.equals("preventasAnticipos")) {
			Fecha fechaDesde = new Fecha("2019-01-01", "yyyy-MM-dd");
			Fecha fechaHasta = new Fecha("2020-04-01", "yyyy-MM-dd");
			PreventasAnticipos datos = getPreventasAnticipos(contexto, "0160329030", fechaDesde, fechaHasta);
			imprimirResultado(contexto, datos);
		}
	}
}
