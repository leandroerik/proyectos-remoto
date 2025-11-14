package ar.com.hipotecario.backend.servicio.api.personas;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.personas.Actividades.Actividad;

public class Actividades extends ApiObjetos<Actividad> {

	public static String GET_ACTIVIDADES = "Actividades";
	public static String POST_ACTIVIDADES = "CrearActividad";
	public static String PATCH_ACTIVIDADES = "ActualizarActividad";
	public static String FORMATO_FECHA = "yyyy-MM-dd'T'HH:mm:ss";

	/* ========== ATRIBUTOS ========== */
	public static class Actividad extends ApiObjeto {

		public String id;
		public String idSituacionLaboral;
		public String idRamo;
		public String idProfesion;
		public String idCargo;
		public Fecha fechaInicioActividad;
		public Fecha fechaEgresoActividad;
		public String razonSocialEmpleador;
		public String cuitEmpleador;
		public BigDecimal ingresoNeto;
		public Integer idCore;
		public BigDecimal resultadoDDJJGanancias;
		public Fecha fechaUltimaDDJJGanancias;
		public String categoriaMonotributo;
		public BigDecimal ingresoAnualDDJJIIBB;
		public Integer idConvenio;
		public Boolean esPrincipal;
		public String canalModificacion;
		public Fecha fechaCreacion;
		public String usuarioModificacion;
		public Fecha fechaModificacion;
		public String preguntas;
		public String respuestas;
		public String etag;

		public Fecha fecha() {
			return Fecha.maxima(fechaCreacion, fechaModificacion);
		}

		public Actividad actualizar(NuevaActividad relacion) {
			this.esPrincipal = relacion.esPrincipal;
			this.fechaEgresoActividad = relacion.fechaEgresoActividad;
			this.fechaModificacion = Fecha.ahora();

			return this;
		}
	}

	/* ========== CLASES ========== */
	public static class NuevaActividad {
		public String idSituacionLaboral;
		public Boolean esPrincipal;
		public String cuitEmpleador;
		public String razonSocialEmpleador;
		public BigDecimal resultadoDDJJGanancias;
		public String categoriaMonotributo;
		public BigDecimal ingresoAnualDDJJIIBB;
		public BigDecimal ingresoNeto;
		public Fecha fechaInicioActividad;
		public Fecha fechaEgresoActividad = Fecha.nunca();
	}

	/* ========== MÉTODOS ========== */
	public static Boolean esActivo(Actividades actividades) {
		if (actividades == null || actividades.size() == 0)
			return false;

		for (Actividad actividad : actividades) {
			if (empty(actividad.fechaEgresoActividad) && actividad.esPrincipal != null && actividad.esPrincipal) {
				return true;
			}
		}

		return false;
	}

	/* ========== SERVICIO ========== */
	// API-Personas_ConsultarActividadesDePersona
	public static Actividades get(Contexto contexto, String cuit, Boolean cache) {
		ApiRequest request = new ApiRequest(GET_ACTIVIDADES, ApiPersonas.API, "GET", "/personas/{id}/actividades", contexto);
		request.header(ApiPersonas.X_USUARIO, contexto.usuarioCanal());
		request.path("id", cuit);
		request.cache = cache;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200) && !response.contains("no arrojó resultados en BUP"), request, response);
		return response.crear(Actividades.class);
	}

	// API-Personas_AltaActividadAPersona
	public static Actividad post(Contexto contexto, String cuit, NuevaActividad actividad) {
		ApiRequest request = new ApiRequest(POST_ACTIVIDADES, ApiPersonas.API, "POST", "/personas/{id}/actividades", contexto);
		request.header(ApiPersonas.X_USUARIO, contexto.usuarioCanal());
		request.path("id", cuit);
		request.body("idSituacionLaboral", actividad.idSituacionLaboral);
		request.body("esPrincipal", actividad.esPrincipal);
		request.body("cuitEmpleador", actividad.cuitEmpleador);
		request.body("razonSocialEmpleador", actividad.razonSocialEmpleador);
		request.body("categoriaMonotributo", actividad.categoriaMonotributo);
		request.body("ingresoNeto", actividad.ingresoNeto);
		request.body("resultadoDDJJGanancias", actividad.resultadoDDJJGanancias);
		request.body("ingresoAnualDDJJIIBB", actividad.ingresoAnualDDJJIIBB);
		request.body("fechaInicioActividad", actividad.fechaInicioActividad.string(FORMATO_FECHA));

		// request.body("fechaEgresoActividad",
		// actividad.fechaEgresoActividad.string(FORMATO_FECHA, null));

		ApiResponse response = request.ejecutar();
		Api.eliminarCache(contexto, GET_ACTIVIDADES, cuit);
		ApiException.throwIf(!response.http(201), request, response);
		return response.crear(Actividad.class);
	}

	// API-Personas_ModificarParcialmenteRelacion
	public static Actividad patch(Contexto contexto, String cuit, Actividad relacion) {
		ApiRequest request = new ApiRequest(PATCH_ACTIVIDADES, ApiPersonas.API, "PATCH", "/actividades/{id}", contexto);
		request.header(ApiPersonas.X_USUARIO, contexto.usuarioCanal());
		request.path("id", relacion.id);
		request.body("esPrincipal", relacion.esPrincipal);
		request.body("ingresoNeto", relacion.ingresoNeto);
		request.body("resultadoDDJJGanancias", relacion.resultadoDDJJGanancias);
		request.body("ingresoAnualDDJJIIBB", relacion.ingresoAnualDDJJIIBB);
		request.body("fechaInicioActividad", relacion.fechaInicioActividad.string(FORMATO_FECHA, null));
		request.body("fechaEgresoActividad", relacion.fechaEgresoActividad.string(FORMATO_FECHA, null));
		request.body("fechaModificacion", relacion.fechaModificacion.string(FORMATO_FECHA, null));

		ApiResponse response = request.ejecutar();
		Api.eliminarCache(contexto, GET_ACTIVIDADES, cuit);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Actividad.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		String cuit = "20275551083";
		String test = "patch";

		if ("get".equals(test)) {
			Actividades datos = get(contexto, cuit, true);
			System.out.println(datos.nombreServicioMW(contexto));
			System.out.println(datos.get(0));
		}
		if ("post".equals(test)) {
			// TODO: Buscar dato correcto para post
			NuevaActividad actividad = new NuevaActividad();
			actividad.esPrincipal = false;
			actividad.fechaEgresoActividad = Fecha.nunca();
			Actividad datos = post(contexto, cuit, actividad);
			imprimirResultado(contexto, datos);
		}
		if ("patch".equals(test)) {
			NuevaActividad nuevaActividad = new NuevaActividad();

			nuevaActividad.esPrincipal = false;
			nuevaActividad.fechaEgresoActividad = Fecha.ahora();

			Actividades actividades = get(contexto, cuit, true);
			Actividad relacion = actividades.get(0);

			Actividad datos = patch(contexto, cuit, relacion.actualizar(nuevaActividad));
			imprimirResultado(contexto, datos);
		}
	}
}
