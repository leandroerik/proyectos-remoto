package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.VencimientosEspecies.VencimientoEspecie;

public class VencimientosEspecies extends ApiObjetos<VencimientoEspecie> {

	/* ========== ATRIBUTOS ========== */
	public static class VencimientoEspecie extends ApiObjeto {
		public String descripcion;
		public String idVencimiento;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_VencimientosEspecies
	public static VencimientosEspecies get(Contexto contexto) {
		ApiRequest request = new ApiRequest("InversionesVencimientosEspecies", "inversiones", "GET", "/v1/vencimientosespecies", contexto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(VencimientosEspecies.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		VencimientosEspecies datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
