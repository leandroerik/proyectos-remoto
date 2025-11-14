package ar.com.hipotecario.mobile.servicio;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class InversionesService {

	public static ApiResponseMB inversionesGetCotizaciones(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("InversionesGetCotizaciones", "inversiones", "GET", "/v1/cotizaciones/{id}", contexto);
		request.path("id", "0");
		request.query("mercado", "6");
		request.query("servicio", "6");
		request.query("cliente", contexto.idCobis());
		request.cacheSesion = false;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB inversionesGetCotizaciones(ContextoMB contexto, String moneda) {
		ApiRequestMB request = ApiMB.request("InversionesGetCotizacionesMoneda", "inversiones", "GET", "/v1/cotizacionesmoneda", contexto);
		request.query("fechadesde", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		request.query("fechahasta", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		request.query("moneda", moneda);
		request.cacheSesion = true;
		return ApiMB.response(request);
	}

	public static ApiResponseMB inversionesGetLicitaciones(ContextoMB contexto) {
		ApiRequestMB request = ApiMB.request("InversionesGetLicitaciones", "inversiones", "GET", "/v1/licitaciones", contexto);
		// request.cacheSesion = true;
		request.headers.put("x-canal", "HB");
		return ApiMB.response(request);
	}

	public static ApiResponseMB inversionesGetCuentasPorComitente(ContextoMB contexto, String numeroCuentaComitente) {
		ApiRequestMB request = ApiMB.request("InversionesGetCuentasPorComitente", "inversiones", "GET", "/v1/cuentas/{cuentacomitente}/comitente", contexto);
		request.path("cuentacomitente", numeroCuentaComitente);
		// request.cacheSesion = true;
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB inversionesGetSeguimientoLicitaciones(ContextoMB contexto, String numeroCuentaComitente) {
		ApiRequestMB request = ApiMB.request("InversionesGetPosturas", "inversiones", "GET", "/v1/cuentascomitentes/{cuentacomitente}/posturas", contexto);
		request.path("cuentacomitente", numeroCuentaComitente);
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.add(Calendar.MONTH, -20);
		request.query("fechadesde", new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime()));
		request.query("fechahasta", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		// request.cacheSesion = true;
		return ApiMB.response(request);
	}

}
