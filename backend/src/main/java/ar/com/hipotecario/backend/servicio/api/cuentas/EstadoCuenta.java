package ar.com.hipotecario.backend.servicio.api.cuentas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class EstadoCuenta extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String CodigoBancoBCRA;
	public String CodigoRespuesta;
	public String Denominacion;
	public String Numero;
	public String Moneda;
	public String TipoDeCuenta;

	// NO ENCONTRE DATOS PARA PROBAR ESTA API
	/* ========== SERVICIOS ========== */
	/* ? */
	static EstadoCuenta get(Contexto contexto, String cbu, String idTributario) {
		ApiRequest request = new ApiRequest("CuentaDeposito", "cuentas", "GET", "/v1/cuentas/estado", contexto);
		request.query("cbu", cbu);
		request.query("idTributario", idTributario);
		request.cache = false;
		// maxi
		ApiResponse response = request.ejecutar();

		ApiException.throwIf(response.contains("INFORMACION_NO_DISPONIBLE"), request, response);
		ApiException.throwIf(response.contains("CBU_SIN_VERIFICACION"), request, response);
		ApiException.throwIf(response.contains("CBU_NO_VALIDO"), request, response);
		ApiException.throwIf(response.contains("CBU_BAJA"), request, response);
		ApiException.throwIf(response.contains("BANCO_NO_CARGA_EN_RUC"), request, response);
		ApiException.throwIf(response.contains("CBU_NO_EXISTE"), request, response);
		ApiException.throwIf(response.contains("CBU_INHABILITADO"), request, response);
		ApiException.throwIf(response.contains("CBU_Y_CUIT_NO_CORRESPONDEN"), request, response);
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(EstadoCuenta.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB_BE", "homologacion");
		EstadoCuenta datos = get(contexto, "0440053030000004157093", "30536623104");
		imprimirResultado(contexto, datos);
	}
}
