package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.CuentaComitentesLicitaciones.CuentaComitenteLicitacion;

public class CuentaComitentesLicitaciones extends ApiObjetos<CuentaComitenteLicitacion> {

	/* ========== ATRIBUTOS ========== */
	public static class CuentaComitenteLicitacion extends ApiObjeto {

	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_ConsultaPosicionNegociable
	public static CuentaComitentesLicitaciones get(Contexto contexto, String id, Fecha fecha, String cantregistros, String secuencial) {
		ApiRequest request = new ApiRequest("CuentasComitenteLicitaciones", "inversiones", "GET", "/v1/cuentascomitentes/{id}/licitaciones", contexto);
		request.path("id", id);
		request.query("cantregistros", cantregistros);
		request.query("fecha", fecha.string("yyyy-MM-dd"));
		request.query("secuencial", secuencial);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(CuentaComitentesLicitaciones.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Fecha fecha = new Fecha("2020-01-01", "yyyy-MM-dd");
		Contexto contexto = contexto("HB", "homologacion");
		CuentaComitentesLicitaciones datos = get(contexto, "2-000108703", fecha, "100", "1");
		imprimirResultado(contexto, datos);
	}
}
