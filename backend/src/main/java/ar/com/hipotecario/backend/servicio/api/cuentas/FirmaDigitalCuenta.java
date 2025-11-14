package ar.com.hipotecario.backend.servicio.api.cuentas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class FirmaDigitalCuenta extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String CUENTA;
	public String TIPO_FIRMA;

	// NO ENCONTRE DATOS PARA PROBAR ESTA API
	/* ========== SERVICIOS ========== */
	/* ? */
	static FirmaDigitalCuenta get(Contexto contexto, String id, String idCuil) {
		ApiRequest request = new ApiRequest("CuentaDeposito", "cuentas", "GET", "/v1/cuentas/{id}/firmadigital", contexto);
		request.path("id", id);
		request.query("idcuil", idCuil);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(FirmaDigitalCuenta.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		FirmaDigitalCuenta datos = get(contexto, "402900000640773", "");
		imprimirResultado(contexto, datos);
	}
}
