package ar.com.hipotecario.canal.homebanking.servicio;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.excepcion.ApiException;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;


public class RestTransferencia {

	public static ApiResponse cuentasGetLimites(ContextoHB contexto, Cuenta cuenta, Boolean fechaActual, String desde) {
		ApiRequest request = Api.request("CuentasGetLimites", "cuentas", "GET", "/v1/cuentas/{idcuenta}/limites", contexto);
		request.path("idcuenta", cuenta.numero());
		request.query("idcliente", contexto.idCobis());
		request.query("fechadesde", desde);
		request.query("fechahasta", fechaActual ? desde : "2099-01-01");
		request.query("idmoneda", cuenta.idMoneda());
		ApiResponse response = Api.response(request);

		if (response.hayError()) {
			throw new ApiException(response);
		}
		return response;
	}

	public static ApiResponse CuentasPostLimites(ContextoHB contexto, Cuenta cuenta, String fecha, BigDecimal monto) {
		ApiRequest request = Api.request("CuentasPostLimites", "cuentas", "POST", "/v1/cuentas/{idcuenta}/limites", contexto);
		request.path("idcuenta", cuenta.numero());
		request.body("fecha", fecha);
		request.body("idCliente", contexto.idCobis());
		request.body("idMoneda", cuenta.idMoneda());
		request.body("importe", monto);

		ApiResponse response = Api.response(request);
		return response;
	}
	
	public static ApiResponse CuentasTISLimites(ContextoHB contexto, Cuenta cuenta, String fecha, BigDecimal monto,TarjetaDebito tdAsociada) {
		DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        DecimalFormat formato = new DecimalFormat("0.00", symbols);
        
		ApiRequest request = Api.request("CuentasPostLimites", "cuentas", "POST", "/v1/especiales", contexto);
		request.body("cbu", cuenta.cbu());
		request.body("fecha", fecha);
		request.body("importe", formato.format(monto));
		request.body("nroTarjeta", tdAsociada.numero());

		ApiResponse response = Api.response(request);
		return response;
	}
}