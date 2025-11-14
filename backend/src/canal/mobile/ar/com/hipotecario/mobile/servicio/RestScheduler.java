package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;

public class RestScheduler {

	public static ApiResponseMB crearTarea(ContextoMB contexto, String anio, String dia, String hora, String mes, String minuto, String nombreDia, String sRequest) {
		ApiRequestMB request = ApiMB.request("SchedulerCrearTarea", "scheduler", "POST", "/v1/tareas", contexto);
		Objeto fechaEjecucions = new Objeto();
		fechaEjecucions.set("id", "0");
		fechaEjecucions.set("ano", anio);
		fechaEjecucions.set("dia", dia);
		fechaEjecucions.set("hora", hora);
		fechaEjecucions.set("mes", mes);
		fechaEjecucions.set("minuto", minuto);
		fechaEjecucions.set("nombreDia", nombreDia);
		fechaEjecucions.set("segundo", "0");

		request.body("fechaEjecucions", new Objeto().add(fechaEjecucions));
		request.body("identificadorPeriodicidad", "UNICA");
		request.body("identificadorHorarioEjecucion", "GENERAL");
		request.body("metodo", "/");
		request.body("numeroMaxIntento", "1");
		request.body("protocolo", "POST");
		request.body("request", sRequest);
		request.body("servicio", "servicios");
		request.body("url", ConfigMB.string("api_url_orquestados") + "/api/tarjeta/pagoTarjeta"); // emm: si viene nula la api_url_orquestados, esto explota y está bien que así
																									// sea
		request.body("servicio", "servicios");
		request.body("tipoRequest", "json");
		request.body("tecnologia", "api-rest");
		request.body("enviaEmail", "false");

		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB crearTareaFIC(ContextoMB contexto, String anio, String dia, String hora, String mes, String minuto, String nombreDia, String sRequest, String sUrl) {
		ApiRequestMB request = ApiMB.request("SchedulerCrearTarea", "scheduler", "POST", "/v1/tareas", contexto);
		Objeto fechaEjecucions = new Objeto();
		fechaEjecucions.set("id", "0");
		fechaEjecucions.set("ano", anio);
		fechaEjecucions.set("dia", dia);
		fechaEjecucions.set("hora", hora);
		fechaEjecucions.set("mes", mes);
		fechaEjecucions.set("minuto", minuto);
		fechaEjecucions.set("nombreDia", nombreDia);
		fechaEjecucions.set("segundo", "0");

		request.body("fechaEjecucions", new Objeto().add(fechaEjecucions));
		request.body("identificadorPeriodicidad", "UNICA");
		request.body("identificadorHorarioEjecucion", "GENERAL");
		request.body("metodo", "/");
		request.body("numeroMaxIntento", "0");
		request.body("protocolo", "POST");
		request.body("request", sRequest);
		request.body("servicio", "servicios");
		request.body("url", ConfigMB.string("api_url_inversiones") + sUrl); // "/v1/suscripcionSL"
		request.body("servicio", "servicios");
		request.body("tipoRequest", "json");
		request.body("tecnologia", "api-rest");
		request.body("enviaEmail", "false");

		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB consultarTareas(ContextoMB contexto, String estado) {
		/*
		 * Códigos de estado: "1": Procesado "2": Por Procesar TODO: "Resto de códigos":
		 * Preguntar a Mariano Ghirelli
		 */
		String tipoEstado = "";
		if ("PROCESADO".equals(estado.toUpperCase()))
			tipoEstado = "1";
		if ("POR PROCESAR".equals(estado.toUpperCase()))
			tipoEstado = "2";

		ApiRequestMB request = ApiMB.request("SchedulerConsultarTarea", "scheduler", "GET", "/v1/tareas", contexto);

		if (!"".equals(tipoEstado)) {
			request.query("estado", tipoEstado);
		}

		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB consultarTareaEspecifica(ContextoMB contexto, String idTarea) {

		ApiRequestMB request = ApiMB.request("SchedulerConsultarTarea", "scheduler", "GET", "/v1/tareas/{id}", contexto);
		request.path("id", idTarea);

		return ApiMB.response(request, contexto.idCobis());
	}

	public static ApiResponseMB consultarHorarioBancario(ContextoMB contexto, String identificador) {
		ApiRequestMB request = ApiMB.request("SchedulerConsultarHorario", "scheduler", "GET", "/v1/horariobancario/identificador/{id}", contexto);
		request.path("id", identificador);

		return ApiMB.response(request, contexto.idCobis());
	}

}
