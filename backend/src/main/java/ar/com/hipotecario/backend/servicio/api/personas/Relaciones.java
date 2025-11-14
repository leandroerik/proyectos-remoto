package ar.com.hipotecario.backend.servicio.api.personas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.personas.Relaciones.Relacion;

public class Relaciones extends ApiObjetos<Relacion> {

	public static String GET_RELACIONES = "Relaciones";
	public static String POST_RELACIONES = "CrearRelacion";
	public static String PATCH_RELACIONES = "ActualizarRelacion";
	public static String FORMATO_FECHA = "yyyy-MM-dd'T'HH:mm:ss";

	/* ========== ATRIBUTOS ========== */
	public static class Relacion extends ApiObjeto {
		public static String CONYUGUE = "2";

		public String id;
		public String idTipoRelacion;
		public String idPersonaRelacionada;
		public Fecha fechaInicioRelacion;
		public Fecha fechaFinRelacion;
		public String canalModificacion;
		public Fecha fechaCreacion;
		public String usuarioModificacion;
		public Fecha fechaModificacion;
		public String cuitPersonaRelacionada;
		public String etag;

		public Fecha fecha() {
			return Fecha.maxima(fechaCreacion, fechaModificacion);
		}

		public Relacion actualizar(NuevaRelacion relacion) {
			this.cuitPersonaRelacionada = relacion.cuitPersonaRelacionada;
			this.fechaFinRelacion = relacion.fechaFinRelacion;
			this.fechaModificacion = Fecha.ahora();

			return this;
		}
	}

	/* ========== CLASES ========== */
	public static class NuevaRelacion {
		public String cuitPersonaRelacionada;
		public Fecha fechaInicioRelacion = Fecha.nunca();
		public Fecha fechaFinRelacion = Fecha.nunca();
	}

	/* ========== SERVICIO ========== */
	// API-Personas_ConsultarRelacionesDePersona
	public static Relaciones get(Contexto contexto, String cuit, Boolean cache) {
		ApiRequest request = new ApiRequest(GET_RELACIONES, ApiPersonas.API, "GET", "/personas/{id}/relaciones", contexto);
		request.header(ApiPersonas.X_USUARIO, contexto.usuarioCanal());
		request.path("id", cuit);
		request.cache = cache;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200) && !response.contains("no arrojó resultados en BUP"), request, response);
		return response.crear(Relaciones.class);
	}

	public static Relacion post(Contexto contexto, String cuit, String cuitRelacion, String tipo) {
		NuevaRelacion relacion = new NuevaRelacion();
		relacion.cuitPersonaRelacionada = cuitRelacion;
		relacion.fechaInicioRelacion = Fecha.ahora();
		return post(contexto, cuit, relacion, tipo);
	}

	// API-Personas_AltaRelacionesAPersona
	public static Relacion post(Contexto contexto, String cuit, NuevaRelacion relacion, String tipo) {
		ApiRequest request = new ApiRequest(POST_RELACIONES, ApiPersonas.API, "POST", "/personas/{id}/relaciones", contexto);
		request.header(ApiPersonas.X_USUARIO, contexto.usuarioCanal());
		request.path("id", cuit);
		request.body("idTipoRelacion", tipo);
		request.body("cuitPersonaRelacionada", relacion.cuitPersonaRelacionada);
		request.body("fechaInicioRelacion", relacion.fechaInicioRelacion.string(FORMATO_FECHA, null));
		request.body("fechaFinRelacion", relacion.fechaFinRelacion.string(FORMATO_FECHA, null));

		ApiResponse response = request.ejecutar();
		Api.eliminarCache(contexto, GET_RELACIONES, cuit);
		ApiException.throwIf(!response.http(201), request, response);
		return response.crear(Relacion.class);
	}

	// API-Personas_ModificarParcialmenteRelacion
	public static Relacion patch(Contexto contexto, String cuit, Relacion relacion) {
		ApiRequest request = new ApiRequest(PATCH_RELACIONES, ApiPersonas.API, "PATCH", "/relaciones/{id}", contexto);
		request.header(ApiPersonas.X_USUARIO, contexto.usuarioCanal());
		request.path("id", relacion.id);
		request.body("idTipoRelacion", relacion.idTipoRelacion);
		request.body("cuitPersonaRelacionada", relacion.cuitPersonaRelacionada);
		request.body("fechaInicioRelacion", relacion.fechaInicioRelacion.string(FORMATO_FECHA, null));
		request.body("fechaFinRelacion", relacion.fechaFinRelacion.string(FORMATO_FECHA, null));
		request.body("fechaModificacion", relacion.fechaModificacion.string(FORMATO_FECHA, null));

		ApiResponse response = request.ejecutar();
		Api.eliminarCache(contexto, GET_RELACIONES, cuit);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Relacion.class);
	}

	/* ========== METODOS ========== */
	public Relacion crearActualizar(Contexto contexto, String cuit, NuevaRelacion nuevoTelefono, String tipo) {
		Relacion relacion = buscar(tipo);
		if (relacion == null) {
			relacion = ApiPersonas.crearRelacion(contexto, cuit, nuevoTelefono, tipo).get();
		} else {
			relacion = ApiPersonas.actualizarRelacion(contexto, cuit, relacion.actualizar(nuevoTelefono)).get();
		}
		return relacion;
	}

	public Relacion crearActualizarTry(Contexto contexto, String cuit, NuevaRelacion nuevoTelefono, String tipo) {
		Relacion relacion = buscar(tipo);
		if (relacion == null) {
			relacion = ApiPersonas.crearRelacion(contexto, cuit, nuevoTelefono, tipo).tryGet();
		} else {
			relacion = ApiPersonas.actualizarRelacion(contexto, cuit, relacion.actualizar(nuevoTelefono)).tryGet();
		}
		return relacion;
	}

	private Relacion buscar(String tipo) {
		Relacion dato = null;
		for (Relacion relacion : this) {
			if (relacion.idTipoRelacion.equals(tipo)) {
				dato = (dato == null || relacion.fecha().esPosterior(dato.fecha())) ? relacion : dato;
			}
		}
		return dato;
	}

	public Relacion conyugue() {
		return buscar(Relacion.CONYUGUE);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = new Contexto("HB", "homologacion", "133366");
		String cuit = "20275551083";
		String test = "patch";

		if ("get".equals(test)) {
			Relaciones datos = get(contexto, cuit, true);
			System.out.println(datos.nombreServicioMW(contexto));
			System.out.println(datos.get(0));
		}
		if ("post".equals(test)) {
			NuevaRelacion relacion = new NuevaRelacion();
			relacion.cuitPersonaRelacionada = "27326881568";
			relacion.fechaInicioRelacion = new Fecha("2019-03-28", "yyyy-MM-dd");
			relacion.fechaFinRelacion = Fecha.nunca();
			Relacion datos = post(contexto, cuit, relacion, Relacion.CONYUGUE);
			imprimirResultado(contexto, datos);
		}
		if ("patch".equals(test)) {
			NuevaRelacion nuevaRelacion = new NuevaRelacion();

			nuevaRelacion.cuitPersonaRelacionada = "20940729239";
			nuevaRelacion.fechaFinRelacion = Fecha.nunca();

			Relaciones relaciones = get(contexto, cuit, true);
			Relacion relacion = relaciones.get(0);

			Relacion datos = patch(contexto, cuit, relacion.actualizar(nuevaRelacion));
			imprimirResultado(contexto, datos);
		}
	}
}
