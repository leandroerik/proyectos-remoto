package ar.com.hipotecario.backend.servicio.api.tarjetaDebito;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.tarjetaDebito.Cuentas.Cuenta;

public class Cuentas extends ApiObjetos<Cuenta> {

	public static class Cuenta extends ApiObjeto {
		public String numero;
		public String codigo;
		public String tipo;
		public Boolean principal;
		public Boolean principalExt;
	}

	// API-TarjetasDebito_ConsultaCuentasTarjetaDebito
	public static Cuentas getCuentas(Contexto contexto, String numeroTarjeta) {
		ApiRequest request = new ApiRequest("CuentasAsociadasTarjetaDebito", "tarjetasdebito", "GET", "/v1/tarjetasdebito/{numeroTarjeta}/cuentas", contexto);
		request.path("numeroTarjeta", numeroTarjeta);

		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Cuentas.class);
	}

}
