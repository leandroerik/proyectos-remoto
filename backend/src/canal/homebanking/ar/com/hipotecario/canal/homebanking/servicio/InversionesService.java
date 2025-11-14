package ar.com.hipotecario.canal.homebanking.servicio;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

public class InversionesService {

	public static ApiResponse inversionesGetCotizaciones(ContextoHB contexto) {
		ApiRequest request = Api.request("InversionesGetCotizaciones", "inversiones", "GET", "/v1/cotizaciones/{id}", contexto);
		request.path("id", "0");
		request.query("mercado", "6");
		request.query("servicio", "6");
		request.query("cliente", contexto.idCobis());
		request.cacheSesion = false;
        return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse inversionesGetCotizaciones(ContextoHB contexto, String moneda) {
		String idSesion = contexto.idSesion();
		String hoy      = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		String clave    = "HB-" + idSesion + "-cotizacionesmoneda-" + moneda + "-" + hoy;
		String jsonCache = contexto.get(clave);

		if (jsonCache != null) {
			ApiRequest cachedRequest = Api.request(
					"InversionesGetCotizacionesMoneda",
					"inversiones",
					"GET",
					"/v1/cotizacionesmoneda",
					contexto
			);
			return new ApiResponse(cachedRequest, 200, jsonCache);
		}

		ApiRequest request = Api.request(
				"InversionesGetCotizacionesMoneda",
				"inversiones",
				"GET",
				"/v1/cotizacionesmoneda",
				contexto
		);
		request.query("fechadesde", hoy);
		request.query("fechahasta", hoy);
		request.query("moneda", moneda);
		request.cacheSesion = false;

		ApiResponse response = Api.response(request, contexto.idCobis(), moneda);
		if (response.codigo == 200) {
			contexto.set(clave, response.json);
		}
		return response;
	}




	public static ApiResponse inversionesGetLicitaciones(ContextoHB contexto) {
		ApiRequest request = Api.request("InversionesGetLicitaciones", "inversiones", "GET", "/v1/licitaciones", contexto);
		// request.cacheSesion = true;
		return Api.response(request);
	}

	public static ApiResponse inversionesGetCuentasPorComitente(ContextoHB contexto, String numeroCuentaComitente) {
		ApiRequest request = Api.request("InversionesGetCuentasPorComitente", "inversiones", "GET", "/v1/cuentas/{cuentacomitente}/comitente", contexto);
		request.path("cuentacomitente", numeroCuentaComitente);
		// request.cacheSesion = true;
		return Api.response(request, contexto.idCobis());
	}
}
