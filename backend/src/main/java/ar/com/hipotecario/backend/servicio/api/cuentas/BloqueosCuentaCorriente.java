package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.BloqueosCuentaCorriente.BloqueoCuentaCorriente;

public class BloqueosCuentaCorriente extends ApiObjetos<BloqueoCuentaCorriente> {

	/* ========== ATRIBUTOS ========== */
	public static class BloqueoCuentaCorriente extends ApiObjeto {
		public String causa;
		public String descBloqueo;
		public String tipoBloqueo;
		public String descTipoBloqueo;
		public BigDecimal importe;
		public Fecha fechaBloqueo;
		public Fecha fechaInicioVigencia;
		public Fecha fechaVencimiento;
	}

	/* ========== SERVICIOS ========== */
	// API-Cuentas_ConsultaBloqueosCuentaCorriente
	static BloqueosCuentaCorriente get(Contexto contexto, String idCuenta) {
		ApiRequest request = new ApiRequest("CuentasCuentasCorrientesBloqueos", "cuentas", "GET", "/v2/cuentascorrientes/{idcuenta}/bloqueos", contexto);
		request.path("idcuenta", idCuenta);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(BloqueosCuentaCorriente.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		BloqueosCuentaCorriente datos = get(contexto, "11");
		imprimirResultado(contexto, datos);
	}
}
