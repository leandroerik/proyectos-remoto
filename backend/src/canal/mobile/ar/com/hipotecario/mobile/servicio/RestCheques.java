package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class RestCheques {

	/* ========== SERVICIOS ========== */
	public static ApiResponseMB cheques(ContextoMB contexto, String idCuenta, String numeroCuenta, String operacion) {
		ApiRequestMB request = ApiMB.request("Cheques", "cheques", "GET", "/v1/cuentascorriente", contexto);
		if (operacion.equals("Rechazados")) {
			request.query("numeroCuenta", numeroCuenta);
		} else {
			request.query("idCuenta", idCuenta);
		}
		request.query("operacion", operacion);
		return ApiMB.response(request, contexto.idCobis());
	}

}
