package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.TenenciasCuotaPartista.TenenciaCuotaPartista;

public class TenenciasCuotaPartista extends ApiObjetos<TenenciaCuotaPartista> {

	/* ========== ATRIBUTOS ========== */
	public static class TenenciaCuotaPartista extends ApiObjeto {
		public String NombreFondo;
		public String PersFisica;
		public String TenenciaCantCp;
		public String TenenciaValuada;
		public String CuotapartistaNum;
		public String CUIL_CUIT_CuotaPartista;
		public String ValorCuotaparte;
		public Fecha FechaIngreso;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_TenenciasFCICuotaPartista
	public static TenenciasCuotaPartista get(Contexto contexto, String cuilCuitCuotaPartista) {
		ApiRequest request = new ApiRequest("TenenciasCuotaPartista", "inversiones", "GET", "/v1/tenencias/cuotapartista", contexto);
		request.query("cuilCuitCuotaPartista", cuilCuitCuotaPartista);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(TenenciasCuotaPartista.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		TenenciasCuotaPartista datos = get(contexto, "20081190233");
		imprimirResultado(contexto, datos);
	}
}
