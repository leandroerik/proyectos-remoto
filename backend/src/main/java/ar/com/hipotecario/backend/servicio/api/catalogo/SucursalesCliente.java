package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.SucursalesCliente.SucursalCliente;

public class SucursalesCliente extends ApiObjetos<SucursalCliente> {

	/* =========== ATRIBUTOS =========== */
	public static class SucursalCliente extends ApiObjeto {
		public String CodigoPostalSucursal;
		public String IdOficial;
		public String IdSucursal;
	}

	/* =========== SERVICIOS =========== */
	// API-Catalogo_ConsultaSucursalesPorCPCliente
	static SucursalCliente get(Contexto contexto, String idCobis) {
		ApiRequest request = new ApiRequest("SucursalCliente", "catalogo", "GET", "/v1/cpSucursal", contexto);
		request.query("cpcliente", idCobis);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(SucursalCliente.class, response.objetos(0));
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		SucursalCliente datos = get(contexto, "133366");
		imprimirResultado(contexto, datos);
	}
}
