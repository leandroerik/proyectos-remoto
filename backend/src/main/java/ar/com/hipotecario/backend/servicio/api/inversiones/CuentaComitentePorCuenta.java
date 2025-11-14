package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.CuentaComitentePorCuenta.CuentaComitenteCuenta;;

public class CuentaComitentePorCuenta extends ApiObjetos<CuentaComitenteCuenta> {

	/* ========== ATRIBUTOS ========== */
	public static class CuentaComitenteCuenta extends ApiObjeto {
		public String NUMERO;
		public String TIPO;
		public String SUCURSAL;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_CuentasPorComitente
	public static CuentaComitentePorCuenta get(Contexto contexto, String cuentacomitente) {
		ApiRequest request = new ApiRequest("CuentasPorComitente", "inversiones", "GET", "/v1/cuentas/{cuentacomitente}/comitente", contexto);
		request.path("cuentacomitente", cuentacomitente);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CuentaComitentePorCuenta.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		CuentaComitentePorCuenta datos = get(contexto, "2-000108703");
		imprimirResultado(contexto, datos);
	}
}
