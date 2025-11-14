package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class SimulacionLetes extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public List<Calendarioproximaslicitaciones> calendarioproximaslicitaciones;
	public List<Datosultimalicitacion> datosultimalicitacion;
	public List<Parametros> parametros;
	public List<Fechaultimalicitacion> fechaultimalicitacion;

	public static class Parametros {
		public String parametro;
		public String valor;
	}

	public static class Calendarioproximaslicitaciones {
		public String fechalicitacion;
		public Fecha fechalicitacion2;
		public String horariolicitacionhb;
	}

	public static class Datosultimalicitacion {
		public String plazo;
		public String tasacorte;
		public Boolean esultimalicitacion;
		public String preciocorte;
	}

	public static class Fechaultimalicitacion {
		public String mesultimalicitacionnombre;
		public String mesultimalicitacionnombrecorto;
		public String mesultimalicitacionnumero;
		public String anioultimalicitacion;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_SimulacionLetes
	public static SimulacionLetes get(Contexto contexto) {
		ApiRequest request = new ApiRequest("SimulacionLetes", "inversiones", "GET", "/v1/simulacion/letes", contexto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(SimulacionLetes.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		SimulacionLetes datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
