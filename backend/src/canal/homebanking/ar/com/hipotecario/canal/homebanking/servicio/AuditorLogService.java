package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;

public class AuditorLogService {

	public static ApiResponse biometriaLogVisualizador(ContextoHB contexto, String servicio, Objeto detalle) {
		if (!ConfigHB.bool("activar_log_errores", false)) {
			return new ApiResponse();
		}
		Objeto body = new Objeto();
		body.set("canal", "MB");
		body.set("subCanal", "BACUNI");
		body.set("usuario", contexto.idCobis());
		body.set("servicio", servicio);
		body.set("resultado", "200");
		body.set("duracion", 1);
		body.set("detalle", detalle);
		body.set("mensajes", new Objeto().set("entrada", "").set("salida", ""));
		body.set("idProceso", Fecha.fechaCompletaString());
		body.set("error", new Objeto());

		ApiRequest request = Api.request(servicio, "auditor", "POST", "/v1/reportes", contexto);
		request.body(body);
		request.permitirSinLogin = true;
		return Api.response(request);
	}
}