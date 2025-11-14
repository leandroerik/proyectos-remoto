package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.CajasAhorrosV1.CajaAhorroV1;

public class CajasAhorrosV1 extends ApiObjetos<CajaAhorroV1> {

	/* ========== ATRIBUTOS ========== */
	public static class CajaAhorroV1 extends ApiObjeto {
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
	static CajaAhorroV1 get(Contexto contexto, String idCuenta, Fecha fechaDesde) {
		return get(contexto, idCuenta, fechaDesde, false, false);
	}

	// API-Cuentas_ConsultaSaldos
	static CajaAhorroV1 get(Contexto contexto, String idCuenta, Fecha fechaDesde, Boolean historico, Boolean validacuentaempleado) {
		ApiRequest request = new ApiRequest("CajasAhorroByIdCuenta", "cuentas", "GET", "/v1/cajasahorros/{idcuenta}", contexto);
		request.path("idcuenta", idCuenta);
		request.query("fechadesde", fechaDesde.string("yyyy-MM-dd"));
		request.query("historico", historico);
		request.query("validacuentaempleado", validacuentaempleado);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("NO_EXISTE_CUENTA", response.contains("NO EXISTE CUENTA"), request, response);
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CajasAhorrosV1.class).get(0);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fechaDesde = new Fecha("2020-01-13", "yyyy-MM-dd");
		CajaAhorroV1 datos = get(contexto, "201800014699558", fechaDesde);
		imprimirResultado(contexto, datos);
	}
}
