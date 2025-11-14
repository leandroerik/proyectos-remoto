package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Objeto;

public class AuditorLogService {

	public static Futuro<ApiResponseMB> biometriaLogVisualizador(ContextoMB contexto, String servicio, Objeto detalle) {
		if (!ConfigMB.bool("activar_log_errores", false)) {
			return new Futuro<>(() -> new ApiResponseMB());
		}
		try {
			Objeto body = new Objeto();
			body.set("canal", "MB");
			body.set("subCanal", "BACUNI");
			body.set("usuario", contexto.idCobis());
			body.set("servicio", servicio);
			body.set("resultado", "200");
//			body.set("inicio", LocalDateTime.now().toString().replace("-", "/"));
//			body.set("fin", LocalDateTime.now().toString().replace("-", "/"));
			body.set("duracion", 1);
			body.set("detalle", detalle);
			body.set("mensajes", new Objeto().set("entrada", "").set("salida", ""));
			body.set("idProceso", Fecha.fechaCompletaString());
			body.set("error", new Objeto());

			ApiRequestMB request = ApiMB.request(servicio, "auditor", "POST", "/v1/reportes", contexto);
			request.body(body);
			request.permitirSinLogin = true;
			return new Futuro<>(() -> ApiMB.response(request));
		} catch (Throwable t) {
			return new Futuro<>(() -> new ApiResponseMB());
		}
	}

	public static Futuro<ApiResponseMB> otpLogVisualizador(ContextoMB contexto, String servicio, Objeto detalle, String salida) {
		if (!ConfigMB.bool("activar_log_errores", false)) {
			return new Futuro<>(() -> new ApiResponseMB());
		}
		try {
			Objeto body = new Objeto();
			body.set("canal", "MB");
			body.set("subCanal", "BACUNI");
			body.set("usuario", contexto.idCobis());
			body.set("servicio", servicio);
			body.set("resultado", "200");
//			body.set("inicio", LocalDateTime.now().toString().replace("-", "/"));
//			body.set("fin", LocalDateTime.now().toString().replace("-", "/"));
			body.set("duracion", 1);
			body.set("detalle", detalle);
			body.set("mensajes", new Objeto().set("entrada", "").set("salida", salida));
			body.set("idProceso", Fecha.fechaCompletaString());
			body.set("error", new Objeto());

			ApiRequestMB request = ApiMB.request(servicio, "auditor", "POST", "/v1/reportes", contexto);
			request.body(body);
			request.permitirSinLogin = true;
			return new Futuro<>(() -> ApiMB.response(request));
		} catch (Throwable t) {
			return new Futuro<>(() -> new ApiResponseMB());
		}
	}

	public static Futuro<ApiResponseMB> prestamosLogVisualizador(ContextoMB contexto, String servicio, Objeto detalle, String salida) {
		if (!ConfigMB.bool("activar_log_errores", false)) {
			return new Futuro<>(() -> new ApiResponseMB());
		}
		try {
			Objeto body = new Objeto();
			body.set("canal", "MB");
			body.set("subCanal", "BACUNI");
			body.set("usuario", contexto.idCobis());
			body.set("servicio", servicio);
			body.set("resultado", "200");
//			body.set("inicio", LocalDateTime.now().toString().replace("-", "/"));
//			body.set("fin", LocalDateTime.now().toString().replace("-", "/"));
			body.set("duracion", 1);
			body.set("detalle", detalle);
			body.set("mensajes", new Objeto().set("entrada", "").set("salida", salida));
			body.set("idProceso", Fecha.fechaCompletaString());
			body.set("error", new Objeto());

			ApiRequestMB request = ApiMB.request(servicio, "auditor", "POST", "/v1/reportes", contexto);
			request.body(body);
			request.permitirSinLogin = true;
			return new Futuro<>(() -> ApiMB.response(request));
		} catch (Throwable t) {
			return new Futuro<>(() -> new ApiResponseMB());
		}
	}

}