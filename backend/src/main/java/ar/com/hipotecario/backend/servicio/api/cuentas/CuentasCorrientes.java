package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasCorrientes.CuentaCorriente;

public class CuentasCorrientes extends ApiObjetos<CuentaCorriente> {

	/* ========== ATRIBUTOS ========== */
	public static class CuentaCorriente extends ApiObjeto {
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
		public String direccionCh;
		public String resumen;
		public String descCategoria;
		public String estado;
		public BigDecimal saldoGirar;
		public BigDecimal disponible;
		public BigDecimal disponibleUltimoDiaHabil;
		public BigDecimal acuerdo;
		public BigDecimal acuerdoDisponible;
		public BigDecimal tasaSobregiro;
		public BigDecimal acuerdoGiro;
		public Integer cantTitulares;
		public Fecha fechaUltimoDiaHabil;
		public Fecha fechaVencimientoAcuerdo;
		public Boolean depositoCheque;
		public Boolean esTransaccional;
		public Boolean bloqValor;
		public Boolean bloqMovim;
	}

	/* ========== SERVICIOS ========== */
	// API-Cuentas_ConsultaSaldos
	static CuentaCorriente get(Contexto contexto, String idCuenta, Fecha fechaDesde, Boolean historico, Boolean validaCuentaEmpleado) {
		ApiRequest request = new ApiRequest("CuentasCuentaCorriente", "cuentas", "GET", "/v1/cuentascorrientes/{idcuenta}", contexto);
		request.path("idcuenta", idCuenta);
		request.query("fechadesde", fechaDesde.string("yyyy-MM-dd"));
		request.query("validacuentaempleado", validaCuentaEmpleado);
		request.query("historico", historico);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CuentasCorrientes.class).get(0);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fechaDesde = new Fecha("2020-01-13", "yyyy-MM-dd");
		CuentaCorriente datos = get(contexto, "304500000022494", fechaDesde, false, false);
		imprimirResultado(contexto, datos);
	}
}
