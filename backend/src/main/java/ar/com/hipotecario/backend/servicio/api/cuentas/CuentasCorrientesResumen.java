package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasCorrientesResumen.CuentaCorrienteResumen;

public class CuentasCorrientesResumen extends ApiObjetos<CuentaCorrienteResumen> {

	/* ========== ATRIBUTOS ========== */
	public static class CuentaCorrienteResumen extends ApiObjeto {
		public String cuenta;
		public String cbu;
		public String descEstado;
		public String moneda;
		public String descMoneda;
		public String tipoTitularidad;
		public String descTipoTitularidad;
		public String usoFirma;
		public String descUsoFirma;
		public String direccionEc;
		public String categoria;
		public String resumen;
		public String descCategoria;
		public String estado;
		public Fecha fechaUltimoDiaHabil;
		public Fecha fechaCierre;
		public Boolean depositoCheque;
		public Boolean esTransaccional;
		public Boolean bloqValor;
		public Boolean bloqMovim;
		public BigDecimal saldoGirar;
		public BigDecimal disponible;
		public BigDecimal disponibleUltimoDiaHabil;
		public BigDecimal valSuspenso;
		public Integer cantTitulares;
	}

	/* ========== SERVICIOS ========== */
	// API-Cuentas_ConsultaSaldos
	static CuentasCorrientesResumen get(Contexto contexto, String idCuenta, Fecha fechaDesde, Fecha fechaHasta) {
		ApiRequest request = new ApiRequest("CuentasCuentasCorrientesResumen", "cuentas", "GET", "/v1/cuentacorriente/resumen/{idcuenta}", contexto);
		request.path("idcuenta", idCuenta);
		request.query("fechaDesde", fechaDesde.string("yyyy-dd-MM"));
		request.query("fechaHasta", fechaHasta.string("yyyy-dd-MM"));
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CuentasCorrientesResumen.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fechaDesde = new Fecha("2020-13-01", "yyyy-dd-MM");
		Fecha fechaHasta = new Fecha("2020-14-04", "yyyy-dd-MM");
		CuentasCorrientesResumen datos = get(contexto, "404500000745801", fechaDesde, fechaHasta);
		imprimirResultado(contexto, datos);
	}
}
