package ar.com.hipotecario.backend.servicio.api.personas;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.personas.PreguntasRiesgoNet.Pregunta;

public class PreguntasRiesgoNet extends ApiObjetos<Pregunta> {

	public static String GET_PREGUNTAS_RIESGO = "PreguntasRiesgoNet";

	public Boolean valido;

	/* ========== REQUEST ========== */
	public static class Request {
		public String dni;
		public String genero;
		public String cuit;
		public String apellido;
		public String nombre;
		public Fecha fechaNacimiento;
		public String provincia;
		public String localidad;
		public String calle;
		public String altura;
	}

	/* ========== ATRIBUTOS ========== */
	public static class Pregunta extends ApiObjeto {
		public String id;
		public String enunciado;
		public List<Opcion> opciones = new ArrayList<>();

		public Boolean esCorrecta(String idOpcion) {
			for (Opcion opcion : opciones) {
				if (opcion.id.equals(idOpcion)) {
					return opcion.correcta;
				}
			}
			return false;
		}
	}

	public static class Opcion extends ApiObjeto {
		public String id;
		public String texto;
		public Boolean correcta;
	}

	/* ========== METODOS ========== */
	public Boolean esCorrecta(String idPregunta, String idOpcion) {
		for (Pregunta pregunta : this) {
			if (pregunta.id.equals(idPregunta)) {
				return pregunta.esCorrecta(idOpcion);
			}
		}
		return false;
	}

	/* ========== SERVICIOS ========== */
	// API-Personas_ConsultaRNV
	public static PreguntasRiesgoNet get(Contexto contexto, Request datos) {
		ApiRequest request = new ApiRequest(GET_PREGUNTAS_RIESGO, ApiPersonas.API, "GET", "/rnvconsulta", contexto);
		request.query("dni", datos.dni);
		request.query("genero", datos.genero);
		request.query("cuit", datos.cuit);
		request.query("apellido", datos.apellido);
		request.query("nombre", datos.nombre);
		request.query("fechaNacimiento", datos.fechaNacimiento.string("dd/MM/yyyy"));
		request.query("provinciaP", datos.provincia);
		request.query("localidadP", datos.localidad);
		request.query("calleP", datos.calle);
		request.query("alturaP", datos.altura);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		ApiException.throwIf("BLOQUEADO_RIESGONET", response.contains("BLOQUEADO"), request, response);
		PreguntasRiesgoNet preguntasRiesgoNet = response.crear(PreguntasRiesgoNet.class, new Objeto());

		Integer idPregunta = 0;
		for (Objeto item : response.objetos("0.oPreguntas.0.preguntas.0.Pregunta")) {
			String correcta = item.string("ok.0.opcion1");
			Pregunta pregunta = new Pregunta();
			pregunta.id = (++idPregunta).toString();
			pregunta.enunciado = item.string("enunciado");
			Integer idOpcion = 0;
			for (Objeto subitem : item.objetos("opciones.0.opcion")) {
				Opcion opcion = new Opcion();
				opcion.id = (++idOpcion).toString();
				opcion.texto = subitem.string("opcion1");
				opcion.correcta = subitem.string("opcion1").equals(correcta);
				pregunta.opciones.add(opcion);
			}
			preguntasRiesgoNet.add(pregunta);
		}

		return preguntasRiesgoNet;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");

		PreguntasRiesgoNet.Request request = new PreguntasRiesgoNet.Request();
		request.dni = "31923880";
		request.genero = "M";
		request.cuit = "20319238809";
		request.apellido = "Sarabia";
		request.nombre = "Cesar Joaquin";
		request.fechaNacimiento = new Fecha("28/02/1986", "dd/MM/yyyy");
		request.provincia = "JUJUY";
		request.localidad = "MAIMARA";
		request.calle = "PARAJE SAN PEDRITO";
		request.altura = "0";
		PreguntasRiesgoNet datos = get(contexto, request);
		imprimirResultado(contexto, datos);
	}
}
