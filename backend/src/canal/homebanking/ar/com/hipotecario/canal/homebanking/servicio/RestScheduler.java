package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;

public class RestScheduler {

	public static ApiResponse crearTarea(ContextoHB contexto, String anio, String dia, String hora, String mes, String minuto, String nombreDia, String sRequest) {
		ApiRequest request = Api.request("SchedulerCrearTarea", "scheduler", "POST", "/v1/tareas", contexto);
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
		request.body("url", ConfigHB.string("api_url_orquestados") + "/api/tarjeta/pagoTarjeta"); // emm: si viene nula la api_url_orquestados, esto explota y está bien que así
																									// sea
		request.body("servicio", "servicios");
		request.body("tipoRequest", "json");
		request.body("tecnologia", "api-rest");
		request.body("enviaEmail", "false");

		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse crearTareaFIC(ContextoHB contexto, String anio, String dia, String hora, String mes, String minuto, String nombreDia, String sRequest, String sUrl) {
		ApiRequest request = Api.request("SchedulerCrearTarea", "scheduler", "POST", "/v1/tareas", contexto);
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
		request.body("url", ConfigHB.string("api_url_inversiones") + sUrl); // "/v1/suscripcionSL"
		request.body("servicio", "servicios");
		request.body("tipoRequest", "json");
		request.body("tecnologia", "api-rest");
		request.body("enviaEmail", "false");

		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse consultarTareas(ContextoHB contexto, String estado) {
		/*
		 * Códigos de estado: "1": Procesado "2": Por Procesar TODO: "Resto de códigos":
		 * Preguntar a Mariano Ghirelli
		 */
		String tipoEstado = "";
		if ("PROCESADO".equals(estado.toUpperCase()))
			tipoEstado = "1";
		if ("POR PROCESAR".equals(estado.toUpperCase()))
			tipoEstado = "2";

		ApiRequest request = Api.request("SchedulerConsultarTarea", "scheduler", "GET", "/v1/tareas", contexto);

		if (!"".equals(tipoEstado)) {
			request.query("estado", tipoEstado);
		}

		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse consultarTareaEspecifica(ContextoHB contexto, String idTarea) {

		ApiRequest request = Api.request("SchedulerConsultarTarea", "scheduler", "GET", "/v1/tareas/{id}", contexto);
		request.path("id", idTarea);

		return Api.response(request, contexto.idCobis());
	}

}
