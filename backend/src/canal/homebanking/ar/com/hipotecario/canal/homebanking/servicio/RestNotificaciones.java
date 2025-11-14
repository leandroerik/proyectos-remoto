package ar.com.hipotecario.canal.homebanking.servicio;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;

public class RestNotificaciones {

	public static SqlResponse consultaConfiguracionAlertas(ContextoHB contexto) {
		SqlRequest sqlRequest = Sql.request("SelectConfiguracionAlertas", "homebanking");
		sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[alertas_por_mail] WHERE [a_id] = ?";
		sqlRequest.parametros.add(contexto.idCobis());
		SqlResponse sqlResponse = Sql.response(sqlRequest);
		return sqlResponse;
	}

	public static boolean consultaConfiguracionAlertasEspecifica(ContextoHB contexto, String alerta) {
		SqlResponse sqlResponse = consultaConfiguracionAlertas(contexto);
		if (sqlResponse.hayError) {
			return false;
		}
		String alertas = "";
		for (Objeto registro : sqlResponse.registros) {
			alertas = registro.string("a_alertas");
		}

		return alertas.contains(alerta);

	}

	public static ApiResponse envioMailOtroDestino(ContextoHB contexto, String plantilla, Objeto parametros, String emailDestino) {
		if (emailDestino == null || "".equals(emailDestino)) {
			return null;
		}
		if (emailDestino != null && !emailDestino.isEmpty()) {
			ApiRequest requestMail = Api.request("LoginCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
			requestMail.body("de", "aviso@mail-hipotecario.com.ar");
			requestMail.body("para", emailDestino);
			requestMail.body("plantilla", plantilla);
			requestMail.body("parametros", parametros);
			requestMail.permitirSinLogin = true;
			return Api.response(requestMail, new Date().getTime());
		}
		return null;
	}

	public static ApiResponse envioMail(ContextoHB contexto, String plantilla, Objeto parametros) {
		String emailDestino = RestPersona.direccionEmail(contexto, contexto.persona().cuit());

		if (StringUtils.isNotBlank(emailDestino)) {
			ApiRequest requestMail = Api.request("LoginCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
			requestMail.body("de", "aviso@mail-hipotecario.com.ar");

			if (ConfigHB.esDesarrollo()) {
				requestMail.body("para", "homologacionit@hipotecario.com.ar");
			} else {
				requestMail.body("para", emailDestino);
			}

			requestMail.body("plantilla", plantilla);
			requestMail.body("parametros", parametros);
			requestMail.permitirSinLogin = true;
			return Api.response(requestMail, new Date().getTime());
		}

		return null;
	}

	public static ApiResponse envioMailSinSesion(ContextoHB contexto, String plantilla, Objeto parametros, String cuil, String idCobis) {
		String emailDestino = RestPersona.direccionEmail(contexto, cuil);
		if (emailDestino == null || "".equals(emailDestino)) {
			return null;
		}
		if (emailDestino != null && !emailDestino.isEmpty()) {
			ApiRequest requestMail = Api.request("LoginCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
			requestMail.body("de", "aviso@mail-hipotecario.com.ar");
			requestMail.body("para", emailDestino);
			requestMail.body("plantilla", plantilla);
			requestMail.body("parametros", parametros);
			requestMail.permitirSinLogin = true;
			return Api.response(requestMail, new Date().getTime());
		}

		return null;
	}

	public static ApiResponse cacheNotificacionesPriorizadas(ContextoHB contexto) {
		ApiRequest request = Api.request("NotificacionesGetPriorizada", "notificaciones", "GET", "/v1/notificaciones/", contexto);
		request.query("idcliente", contexto.idCobis());
		request.query("priorizada", "true");
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse cacheNotificaciones(ContextoHB contexto) {
		// String idCliente = "4373070";
		ApiRequest request = Api.request("NotificacionesGet", "notificaciones", "GET", "/v1/notificaciones/", contexto);
		request.query("idcliente", contexto.idCobis());
		request.query("priorizada", "false");
		request.cacheSesion = true;
		return Api.response(request);
	}

	public static ApiResponse sendSms(ContextoHB contexto, String telefono, String mensaje, String codigo) {
		ApiRequest request = Api.request("NotificacionesEnvioSms", "notificaciones", "POST", "/v1/notificaciones/sms", contexto);

		request.query("tipo", "MB");
		request.body("telefono", telefono);
		request.body("mensaje", mensaje); // utiliza solo el mensaje
		request.body("codigo", codigo); // no lo utiliza para componer el mensaje

		request.permitirSinLogin = true;

		ApiResponse response = Api.response(request);
		return response;
	}

	public static SqlResponse consultaNotificacionesHabilitada(ContextoHB contexto, String parametro) {
		SqlRequest sqlRequest = Sql.request("SelectConfiguracionAlertas", "homebanking");
		sqlRequest.sql = "SELECT * FROM [homebanking].[dbo].[notificacion_por_cobis] WHERE [id_cobis] = ? and [parametro]=?";
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(parametro);
		SqlResponse sqlResponse = Sql.response(sqlRequest);
		return sqlResponse;
	}

	public static ApiResponse enviarCorreo(ContextoHB contexto, String para, String plantilla, Objeto parametros) {
		if (para == null || para.isEmpty() || plantilla == null || plantilla.isEmpty()) {
			return null;
		}

		try {
			ApiRequest request = Api.request("NotificacionesPostCorreoElectronico", "notificaciones", "POST", "/v1/correoelectronico", contexto);
			request.body("de", "aviso@mail-hipotecario.com.ar");
			if (ConfigHB.esProduccion()) {
				request.body("para", para);
			} else {
				request.body("para", "homologacionit@hipotecario.com.ar");
			}
			request.body("plantilla", plantilla);
			request.body("parametros", parametros);
			return Api.response(request, new Date().getTime());
		} catch (Exception e) {}
		return null;
	}

}
