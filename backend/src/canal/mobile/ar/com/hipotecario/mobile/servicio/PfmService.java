package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class PfmService {

	public static ApiResponseMB consultarMovimientos(ContextoMB contexto, String fechaDesde, String fechaHasta) {
		ApiRequestMB request = ApiMB.request("PFMGetMovimientos", "pfm", "GET", "/v1/movimientos", contexto);
		request.query("cobisId", contexto.idCobis());
		request.query("fechaDesde", fechaDesde);
		request.query("fechaHasta", fechaHasta);
		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB consultarMovimientosMensuales(ContextoMB contexto, String fechaDesde, String fechaHasta) {
		ApiRequestMB request = ApiMB.request("PFMGetMovimientos", "pfm", "GET", "/v1/movimientosMensuales", contexto);
		request.query("cobisId", contexto.idCobis());
		request.query("fechaDesde", fechaDesde);
		request.query("fechaHasta", fechaHasta);
		return ApiMB.response(request, contexto.idCobis());
	}
}
