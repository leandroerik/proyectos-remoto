package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class SimulacionLecaps extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public List<Calendarioproximaslicitaciones> calendarioproximaslicitaciones;
	public List<Datosultimalicitacion> datosultimalicitacion;
	public List<Fechaultimalicitacion> fechaultimalicitacion;

	public static class Calendarioproximaslicitaciones {
		public String fechalicitacion;
		public Fecha fechalicitacion2;
		public String horariolicitacionhb;
	}

	public static class Datosultimalicitacion {
		public String plazo;
		public String tm;
		public String coeficientedia360emision;
		public String coeficientedia360liquidacion;
		public String unitrade;
		public String tna;
		public Boolean esultimalicitacion;
		public String dia360emision;
		public String dia360liquidacion;
		public Fecha fechaemision;
		public Fecha fechaliquidacion;
		public Fecha fechavencimiento;
	}

	public static class Fechaultimalicitacion {
		public String mesultimalicitacionnombre;
		public String mesultimalicitacionnombrecorto;
		public String mesultimalicitacionnumero;
		public String anioultimalicitacion;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_SimulacionLecaps
	public static SimulacionLecaps get(Contexto contexto) {
		ApiRequest request = new ApiRequest("SimulacionLecaps", "inversiones", "GET", "/v1/simulacion/lecaps", contexto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(SimulacionLecaps.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		SimulacionLecaps datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
