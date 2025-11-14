package ar.com.hipotecario.backend.servicio.api.ventas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.ventas.Integrantes.Integrante;

public class Integrantes extends ApiObjetos<Integrante> {

	public static String GET_INTEGRANTES = "Integrantes";
	public static String POST_INTEGRANTES = "CrearIntegrantes";

	/* ========== ATRIBUTOS ========== */
	public static class Integrante extends ApiObjeto {
		public String tipoProducto;
		public Integer Secuencia;
		public String NumeroTributario;
		public String IdCobis;
		public String TipoOperacion;
		public Boolean AlertaCSCActividadRiesgo;
		public Boolean AlertaCSCIngresoSuperior;
		public Object Advertencias;
		public String Id;
	}

	/* ========== CLASES ========== */
	public static class NuevoIntegrante {
		public String TipoOperacion;
		public String NumeroTributario;
	}

	/* ========== SERVICIOS ========== */
	// integrantesGET
	public static Integrantes get(Contexto contexto, String numeroSolicitud) {
		ApiRequest request = new ApiRequest(GET_INTEGRANTES, ApiVentas.API, "GET", "/solicitudes/{numeroSolicitud}/integrantes", contexto);
		request.header(ApiVentas.X_HANDLE, numeroSolicitud);
		request.path("numeroSolicitud", numeroSolicitud);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(Integrantes.class, response.objetos(ApiVentas.DATOS));
	}

	// integrantesPOST
	public static Integrante post(Contexto contexto, String numeroSolicitud, NuevoIntegrante nuevoIntegrante) {
		ApiRequest request = new ApiRequest(POST_INTEGRANTES, ApiVentas.API, "POST", "/solicitudes/{numeroSolicitud}/integrantes", contexto);
		request.header(ApiVentas.X_HANDLE, numeroSolicitud);
		request.path("numeroSolicitud", numeroSolicitud);

		request.body("TipoOperacion", nuevoIntegrante.TipoOperacion);
		request.body("NumeroTributario", nuevoIntegrante.NumeroTributario);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(ApiVentas.errorVentas(response), request, response);
		return response.crear(Integrante.class, response.objetos(ApiVentas.DATOS).get(0));
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		String test = "post";

		if (test.equals("get")) {
			Integrantes datos = get(contexto, "30423642");
			imprimirResultadoApiVentas(contexto, datos);
		}

		if (test.equals("post")) {
			String numeroSolicitud = "30423645";
			NuevoIntegrante nuevoIntegrante = new NuevoIntegrante();

			nuevoIntegrante.TipoOperacion = "03";
			nuevoIntegrante.NumeroTributario = "20235684900";

			Integrante datos = post(contexto, numeroSolicitud, nuevoIntegrante);
			imprimirResultadoApiVentas(contexto, datos);
		}

	}
}
