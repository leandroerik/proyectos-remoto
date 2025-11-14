package ar.com.hipotecario.backend.servicio.api.personas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.Api;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.personas.Emails.Email;

public class Emails extends ApiObjetos<Email> {

	public static String GET_EMAILS = "Emails";
	public static String POST_EMAIL = "CrearEmail";
	public static String PATCH_EMAIL = "ActualizarEmail";

	/* ========== ATRIBUTOS ========== */
	public static class Email extends ApiObjeto {
		// cobis: SELECT * FROM cobis.dbo.cl_catalogo WHERE tabla IN (SELECT codigo FROM
		// cobis.dbo.cl_tabla WHERE tabla = 'cl_dir_electronica')
		public static String LABORAL = "EML";
		public static String PERSONAL = "EMP";
		public static String WEB = "WEB";

		public String id;
		public String idTipoMail;
		public String direccion;
		public String idCore;
		public String prioridad;
		public Boolean esDeclarado;
		public String canalModificacion;
		public Fecha fechaCreacion;
		public String usuarioModificacion;
		public Fecha fechaModificacion;
		public String etag;

		public Fecha fecha() {
			return Fecha.maxima(fechaCreacion, fechaModificacion);
		}

		public Email actualizar(String email) {
			this.direccion = email;
			return this;
		}

		public String direccion() {
			return direccion;
		}

		public String enmascarado() {
			String direccion = direccion();
			direccion = direccion.substring(0, 2) + "***" + direccion.substring(direccion.indexOf("@"));
			return direccion;
		}
	}

	/* ========== SERVICIOS ========== */
	// API-Personas_ConsultarMailsDePersona
	public static Emails get(Contexto contexto, String cuit, Boolean cache) {
		ApiRequest request = new ApiRequest(GET_EMAILS, ApiPersonas.API, "GET", "/personas/{id}/mails", contexto);
		request.path("id", cuit);
		request.cache = cache;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200) && !response.contains("no arrojó resultados en BUP"), request, response);
		return response.crear(Emails.class);
	}

	// API-Personas_AltaMailAPersona
	public static Email post(Contexto contexto, String cuit, String email, String tipo) {
		ApiRequest request = new ApiRequest(POST_EMAIL, ApiPersonas.API, "POST", "/personas/{id}/mails", contexto);
		request.header(ApiPersonas.X_USUARIO, contexto.usuarioCanal());
		request.path("id", cuit);
		request.body("idTipoMail", tipo);
		request.body("direccion", email);

		ApiResponse response = request.ejecutar();
		Api.eliminarCache(contexto, GET_EMAILS, cuit);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Email.class);
	}

	// API-Personas_ModificarParcialmenteMail
	public static Email patch(Contexto contexto, String cuit, Email email) {
		ApiRequest request = new ApiRequest(PATCH_EMAIL, ApiPersonas.API, "PATCH", "/mails/{id}", contexto);
		request.header(ApiPersonas.X_USUARIO, contexto.usuarioCanal());
		request.path("id", email.id);
		request.body("idTipoMail", email.idTipoMail);
		request.body("direccion", email.direccion);

		ApiResponse response = request.ejecutar();
		Api.eliminarCache(contexto, GET_EMAILS, cuit);
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Email.class);
	}

	/* ========== METODOS ========== */
	public Email crearActualizar(Contexto contexto, String cuit, String nuevoEmail, String tipo) {
		Email email = buscar(tipo);
		if (email == null) {
			email = ApiPersonas.crearEmail(contexto, cuit, nuevoEmail, tipo).get();
		} else {
			email = ApiPersonas.actualizarEmail(contexto, cuit, email.actualizar(nuevoEmail)).get();
		}
		return email;
	}

	public Email crearActualizarTry(Contexto contexto, String cuit, String nuevoEmail, String tipo) {
		Email email = buscar(tipo);
		if (email == null) {
			email = ApiPersonas.crearEmail(contexto, cuit, nuevoEmail, tipo).get();
		} else {
			email = ApiPersonas.actualizarEmail(contexto, cuit, email.actualizar(nuevoEmail)).get();
		}
		return email;
	}

	private Email buscar(String tipo) {
		Email dato = null;
		for (Email email : this) {
			if (email.idTipoMail.equals(tipo) && !email.canalModificacion.equals("BATCH")) {
				dato = (dato == null || email.fecha().esPosterior(dato.fecha())) ? email : dato;
			}
		}
		return dato;
	}

	public Email laboral() {
		return buscar(Email.LABORAL);
	}

	public Email personal() {
		return buscar(Email.PERSONAL);
	}

	public Email web() {
		return buscar(Email.WEB);
	}

	/* ========== TEST ========== */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		String cuit = "20275551083";
		String test = "crearAct";

		if ("get".equals(test)) {
			Emails datos = get(contexto, cuit, true);
			Email personal = datos.personal();
			System.out.println(datos.nombreServicioMW(contexto));
			System.out.println(datos.get(0));
		}
		if ("post".equals(test)) {
			Email datos = post(contexto, cuit, "gsuarez@hipotecario.com.ar", Email.PERSONAL);
			System.out.println(datos.nombreServicioMW(contexto));
			System.out.println(datos);
		}
		if ("patch".equals(test)) {
			Emails emails = get(contexto, cuit, true);
			Email personal = emails.personal();
			Email datos = patch(contexto, cuit, personal.actualizar("patch@hipotecario.com.ar"));
			imprimirResultado(contexto, datos);
		}
		if ("crearAct".equals(test)) {
			String nuevoEmail = "menavarro@hipotecario.com.ar";

			Emails emails = ApiPersonas.emails(contexto, cuit).tryGet();
			if (emails == null) {
				return;
			}
			Email datos = emails.crearActualizarTry(contexto, cuit, nuevoEmail, Email.PERSONAL);
			imprimirResultado(contexto, datos);
		}
	}
}
