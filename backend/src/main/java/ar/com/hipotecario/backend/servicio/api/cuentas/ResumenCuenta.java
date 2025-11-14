package ar.com.hipotecario.backend.servicio.api.cuentas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class ResumenCuenta extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String file;

	/* ========== SERVICIOS ========== */
	// API-Cuentas_EResumen
	static ResumenCuenta get(Contexto contexto, String idCuenta, Fecha fechaDesde, Fecha fechaHasta, String producto) {
		ApiRequest request = new ApiRequest("ResumenCuenta", "cuentas", "GET", "/v1/cuentas/{idcuenta}/resumen", contexto);
		request.path("idcuenta", idCuenta);
		request.query("desde", fechaDesde.string("yyyy-MM-dd"));
		request.query("hasta", fechaHasta.string("yyyy-MM-dd"));
		request.query("producto", producto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(ResumenCuenta.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Fecha fechaDesde = new Fecha("2020-01-01", "yyyy-MM-dd");
		Fecha fechaHasta = new Fecha("2020-04-30", "yyyy-MM-dd");
		ResumenCuenta datos = get(contexto, "405100012723564", fechaDesde, fechaHasta, "CA");
		imprimirResultado(contexto, datos);
	}
}
