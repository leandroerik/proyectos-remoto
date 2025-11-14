package ar.com.hipotecario.backend.servicio.api.inversiones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.Cotizaciones.Cotizacion;

public class Cotizaciones extends ApiObjetos<Cotizacion> {

	/* ========== ATRIBUTOS ========== */
	public static class Cotizacion extends ApiObjeto {
		public double compra;
		public Boolean cont;
		public String desde;
		public String estado;
		public String expresion;
		public Fecha fecha;
		public double hasta;
		public String hora;
		public String id;
		public String mercado;
		public String idMoneda;
		public String usuario;
		public String var;
		public double venta;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_Cotizaciones
	public static Cotizaciones get(Contexto contexto, String id, String mercado) {
		ApiRequest request = new ApiRequest("Cotizaciones", "inversiones", "GET", "/v2/cotizaciones", contexto);
		request.query("idcliente", id);
		request.query("mercado", mercado);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Cotizaciones.class);
	}

	public static Cotizaciones getWithCanal(Contexto contexto, String id, String mercado, String canal) {
		ApiRequest request = new ApiRequest("Cotizaciones", "inversiones", "GET", "/v2/cotizaciones", contexto);
		request.query("idcliente", id);
		request.query("mercado", mercado);
		request.header("x-canal", canal);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Cotizaciones.class);
	}
	
	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Cotizaciones datos = get(contexto, "0", "6");
		imprimirResultado(contexto, datos);
	}

}
