package ar.com.hipotecario.mobile.servicio;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.excepcion.ApiExceptionMB;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;

public class RestTransferencia {

	public ApiResponseMB CuentasPostLimites(ContextoMB contexto, Cuenta cuenta, String fecha, BigDecimal monto) {
		ApiRequestMB request = ApiMB.request("CuentasPostLimites", "cuentas", "POST", "/v1/cuentas/{idcuenta}/limites", contexto);
		request.path("idcuenta", cuenta.numero());
		request.body("fecha", fecha);
		request.body("idCliente", contexto.idCobis());
		request.body("idMoneda", cuenta.idMoneda());
		request.body("importe", monto);
		return ApiMB.response(request);
	}
	
	public ApiResponseMB CuentasTISLimites(ContextoMB contexto, Cuenta cuenta, String fecha, BigDecimal monto, TarjetaDebito tdAsociada) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat formato = new DecimalFormat("0.00", symbols);
        
		ApiRequestMB request = ApiMB.request("CuentasPostLimites", "cuentas", "POST", "/v1/especiales", contexto);
		request.body("cbu", cuenta.cbu());
		request.body("fecha", fecha);
		request.body("importe", formato.format(monto));
		request.body("nroTarjeta", tdAsociada.numero());
		return ApiMB.response(request);
	}

	public ApiResponseMB cuentasGetLimites(ContextoMB contexto, Cuenta cuenta, Boolean fechaActual, String desde) {
		ApiRequestMB request = ApiMB.request("CuentasGetLimites", "cuentas", "GET", "/v1/cuentas/{idcuenta}/limites", contexto);
		request.path("idcuenta", cuenta.numero());
		request.query("idcliente", contexto.idCobis());
		request.query("fechadesde", desde);
		request.query("fechahasta", fechaActual ? desde : "2099-01-01");
		request.query("idmoneda", cuenta.idMoneda());
		ApiResponseMB response = ApiMB.response(request);

		if (response.hayError()) {
			throw new ApiExceptionMB(response);
		}
		return response;
	}
}