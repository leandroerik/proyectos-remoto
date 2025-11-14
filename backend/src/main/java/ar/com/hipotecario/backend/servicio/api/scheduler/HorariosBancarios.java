package ar.com.hipotecario.backend.servicio.api.scheduler;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.scheduler.HorariosBancarios.HorarioBancario;

public class HorariosBancarios extends ApiObjetos<HorarioBancario> {

	public static String GET_HORARIOS = "HorariosBancarios";
	public static String GET_HORARIO_IDENTIFICADOR = "HorarioBancarioIdentificador";

	/* ========== CLASES ========== */
	public static class HorarioBancario extends ApiObjeto {
		public String id;
		public String descripcion;
		public Integer horaInicio;
		public Integer minutoInicio;
		public Integer horaFin;
		public Integer minutoFin;
		public String identificador;
	}

	/* ========== SERVICIO ========== */
	// API-Scheduler_ConsultarHorariosBancarios
	public static HorariosBancarios get(Contexto contexto) {
		ApiRequest request = new ApiRequest(GET_HORARIOS, ApiScheduler.API, "GET", "/v1/horariobancario", contexto);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(HorariosBancarios.class);
	}

	// horarioBancarioGET
	public static HorarioBancario get(Contexto contexto, String identificador) {
		ApiRequest request = new ApiRequest(GET_HORARIO_IDENTIFICADOR, ApiScheduler.API, "GET", "/v1/horariobancario/identificador/{identificador}", contexto);
		request.path("identificador", identificador);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200) || response.isEmpty(), request, response);
		return response.crear(HorariosBancarios.class).get(0);
	}

	/* ========== METODOS ========== */
	private HorarioBancario buscar(String tipo) {
		HorarioBancario dato = null;
		for (HorarioBancario horario : this) {
			if (tipo.equals(horario.identificador) || tipo.equals(horario.id)) {
				dato = (dato == null) ? horario : dato;
			}
		}
		return dato;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		String test = "identif";

		if ("buscar".equals(test)) {
			HorariosBancarios datos = get(contexto);
			HorarioBancario dato = datos.buscar("GENERAL");
			imprimirResultado(contexto, dato);
		}

		if ("get".equals(test)) {
			HorariosBancarios datos = get(contexto);
			imprimirResultado(contexto, datos);
		}

		if ("identif".equals(test)) {
			HorarioBancario dato = get(contexto, "BATCH_CORE");
			imprimirResultado(contexto, dato);
		}

	}
}
