package ar.com.hipotecario.backend.servicio.api.recaudaciones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.LotesOB.LotePlanSueldo;

public class LotesOB extends ApiObjetos<LotePlanSueldo> {

	/* ========== ATRIBUTOS ========== */
	public static class LotePlanSueldo extends ApiObjeto {

		public String convenio;
		public String fechaCarga;
		public String nombreArchivo;
		public int numeroLote;
		public String estado;
		public double importe;
		public String observaciones;
		public int cantidad;

	}

	/* ========== SERVICIOS ========== */

	public static LotesOB get(Contexto contexto, String canal, String convenio, String fechadesde, String fechahasta) {
		ApiRequest request = new ApiRequest("LinkGetLotes", "recaudaciones", "GET", "/v1/plansueldo/lotes", contexto);
		request.query("canal", canal);
		request.query("convenio", convenio);
		request.query("fechadesde", fechadesde);
		request.query("fechahasta", fechahasta);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204, 404), request, response);

		return response.crear(LotesOB.class);

	}

}
