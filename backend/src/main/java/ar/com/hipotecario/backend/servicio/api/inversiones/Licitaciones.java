package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.Licitaciones.Licitacion;

public class Licitaciones extends ApiObjetos<Licitacion> {

	/* ========== ATRIBUTOS ========== */
	public static class Especies {
		public List<Especie> especie;
	}

	public static class Especie {
		public String monedaReferencia;
		public Tramos tramos;
		public String codigo;
		public String precioBloqueo;
		public Criterio criterio;
		public String descripcion;
		public String monedaLiquidacion;
	}

	public static class Criterio {
		public String tipo;
		public String maximo;
		public String minimo;
	}

	public static class Tramos {
		public List<Tramo> tramo;
	}

	public static class Tramo {
		public String tipo;
		public Cantidad Cantidad;
	}

	public static class Cantidad {
		public String maximaPorPostura;
		public String licitada;
		public String minimaPorPostura;
	}

	public static class Licitacion extends ApiObjeto {
		public String codigo;
		public String descripcion;
		public Fecha fechaFinColocadoresHB;
		public String horaFinColocadoresHB;
		public String perfilInversor;
		public Especies especies;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_Licitaciones
	public static Licitaciones get(Contexto contexto) {
		ApiRequest request = new ApiRequest("InversionesGetLicitaciones", "inversiones", "GET", "/v2/licitaciones", contexto);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Licitaciones.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Licitaciones datos = get(contexto);
		imprimirResultado(contexto, datos);
	}
}
