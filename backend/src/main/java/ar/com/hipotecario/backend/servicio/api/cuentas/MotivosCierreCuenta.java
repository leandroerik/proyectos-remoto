package ar.com.hipotecario.backend.servicio.api.cuentas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.MotivosCierreCuenta.MotivoCierreCuenta;

public class MotivosCierreCuenta extends ApiObjetos<MotivoCierreCuenta> {

	/* ========== ATRIBUTOS ========== */
	public static class MotivoCierreCuenta extends ApiObjeto {
		public String descripcion;
		public String codigo;
	}

	/* ========== SERVICIOS ========== */
	// API-Cuentas_CatalogoDeMotivosDeCierre
	static MotivosCierreCuenta get(Contexto contexto) {
		ApiRequest request = new ApiRequest("CuentasMotivosCierre", "cuentas", "GET", "/v1/cuentas/motivoscierre", contexto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(MotivosCierreCuenta.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		MotivosCierreCuenta datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
