package ar.com.hipotecario.canal.buhobank;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentasBB;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasBB;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasBB.CuentaBB;

public class BBCorresponsalia extends Modulo {
	public static Object esJubilado(ContextoBB contexto) {
		String idCobis = contexto.parametros.string("idCobis");
		CuentasBB cuentas = ApiCuentasBB.get(contexto, idCobis).tryGet();
		for (CuentaBB cuenta : cuentas) {
			if (cuenta.categoria.equals("SS")) {
				ApiRequest requestSeguridadGetUsuario = new ApiRequest("Seguridad", "seguridad", "GET", "/v1/usuario", contexto);
				requestSeguridadGetUsuario.query("grupo", "ClientesBH");
				requestSeguridadGetUsuario.query("idcliente", idCobis);

				ApiResponse responseSeguridadGetUsuario = requestSeguridadGetUsuario.ejecutar();
				if (responseSeguridadGetUsuario.hayError() && responseSeguridadGetUsuario.codigoHttp != 404) {
					return respuesta("ERROR");
				}

				if (!responseSeguridadGetUsuario.bool("tieneClaveNumerica")) {
					return respuesta("esJubilado", true);
				}
			}
		}
		return respuesta("esJubilado", false);
	}

}
