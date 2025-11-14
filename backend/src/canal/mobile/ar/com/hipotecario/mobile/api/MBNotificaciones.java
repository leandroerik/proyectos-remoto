package ar.com.hipotecario.mobile.api;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.RestNotificaciones;
import ar.com.hipotecario.mobile.servicio.RestPostventa;
import ar.com.hipotecario.mobile.servicio.SqlNotificaciones;
import ar.com.hipotecario.mobile.servicio.SqlSucursalVirtual;

public class MBNotificaciones {

	/* ========== PAGOS ========== */
	public static RespuestaMB notificaciones(ContextoMB contexto) {

		ApiResponseMB responsePriorizadas = RestNotificaciones.cacheNotificacionesPriorizadas(contexto);
		if (responsePriorizadas.hayError()) {
			return RespuestaMB.error();
		}

		Objeto datos = new Objeto();
		List<String> listaIdsPriorizados = new ArrayList<String>();
		for (Objeto item : responsePriorizadas.objetos()) {
			Objeto notificacion = new Objeto();
			notificacion.set("id", item.string("id"));
			notificacion.set("texto", item.string("texto"));
			notificacion.set("textoRespuesta", item.string("textoRespuesta"));
			notificacion.set("botonSi", item.string("si"));
			notificacion.set("botonNo", item.string("no"));
			notificacion.set("botonSleep", item.string("sleep"));

			listaIdsPriorizados.add(item.string("id"));

			String jsonDatosAdicionales = item.string("datosAdicionales");
			if (jsonDatosAdicionales != null && !jsonDatosAdicionales.isEmpty()) {
				Objeto arrayDatosAdicionales = Objeto.fromJson(jsonDatosAdicionales);
				for (Objeto datosAdicionales : arrayDatosAdicionales.objetos()) {
					Objeto objeto = new Objeto();
					objeto.set("idPantalla", datosAdicionales.string("idPantalla"));
					objeto.set("diferirRespuesta", datosAdicionales.string("diferirResp"));
					notificacion.set("datosAdicionales", objeto);
				}
			}

			datos.add("listaPriorizadas", notificacion);
		}

		ApiResponseMB responseNotificaciones = RestNotificaciones.cacheNotificaciones(contexto);
		if (responseNotificaciones.hayError()) {
			// return Respuesta.error();
		}
		Boolean existenNotificacionesNoPriorizadas = false;
		for (Objeto item : responseNotificaciones.objetos()) {
			Objeto notificacion = new Objeto();
			notificacion.set("id", item.string("id"));
			notificacion.set("texto", item.string("texto"));
			notificacion.set("textoRespuesta", item.string("textoRespuesta"));
			notificacion.set("botonSi", item.string("si"));
			notificacion.set("botonNo", item.string("no"));
			notificacion.set("botonSleep", item.string("sleep"));
			String jsonDatosAdicionales = item.string("datosAdicionales");
			if (jsonDatosAdicionales != null && !jsonDatosAdicionales.isEmpty()) {
				Objeto arrayDatosAdicionales = Objeto.fromJson(jsonDatosAdicionales);
				for (Objeto datosAdicionales : arrayDatosAdicionales.objetos()) {
					Objeto objeto = new Objeto();
					objeto.set("idPantalla", datosAdicionales.string("idPantalla"));
					objeto.set("diferirRespuesta", datosAdicionales.string("diferirResp"));
					notificacion.set("datosAdicionales", objeto);
				}
			}
			Boolean priorizada = false;
			for (String value : listaIdsPriorizados) {
				if (value.equals(item.string("id"))) {
					priorizada = true;
				}
			}
			notificacion.set("priorizada", priorizada);
			if (!priorizada) {
				existenNotificacionesNoPriorizadas = true;
			}
			datos.add("lista", notificacion);
		}
		datos.add("existenNotificacionesNoPriorizadas", existenNotificacionesNoPriorizadas.toString());
		return RespuestaMB.exito("notificaciones", datos);
	}

	public static RespuestaMB notificacionesRespuesta(ContextoMB contexto) {
		String idAlerta = contexto.parametros.string("idAlerta");
		String tipoRespuesta = contexto.parametros.string("tipoRespuesta");
		String textoRespuesta = contexto.parametros.string("textoRespuesta");
		ApiRequestMB request = ApiMB.request("NotificacionesPostRespuesta", "notificaciones", "POST", "/v1/notificaciones/{id}/alertas", contexto);
		request.path("id", idAlerta);
		request.body("id", idAlerta);
		request.body("idCliente", contexto.idCobis());
		request.body("tipoRespuesta", tipoRespuesta);
		request.body("textoRespuesta", textoRespuesta);
		ApiResponseMB response = ApiMB.response(request);
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		return RespuestaMB.exito();
	}

	public static RespuestaMB configuracionAlertas(ContextoMB contexto) {
		SqlResponseMB sqlResponse = RestNotificaciones.consultaConfiguracionAlertas(contexto);
		if (sqlResponse.hayError) {
			return RespuestaMB.error();
		}
		String alertas = "";
		for (Objeto registro : sqlResponse.registros) {
			alertas = registro.string("a_alertas");
		}

		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("ingresos", alertas.contains("A_ACC"));
		respuesta.set("cambioClave", alertas.contains("A_CCL"));
		respuesta.set("desbloqueoClave", alertas.contains("A_DBC"));
		respuesta.set("agendarBeneficiario", alertas.contains("A_BNF"));

		return respuesta;
	}

	public static RespuestaMB modificarConfiguracionAlertas(ContextoMB contexto) {
		Boolean ingresos = contexto.parametros.bool("ingresos", false);
		Boolean cambioClave = contexto.parametros.bool("cambioClave", false);
		Boolean desbloqueoClave = contexto.parametros.bool("desbloqueoClave", false);
		Boolean agendarBeneficiario = contexto.parametros.bool("agendarBeneficiario", false);

		SqlResponseMB sqlResponseConsulta = RestNotificaciones.consultaConfiguracionAlertas(contexto);
		if (sqlResponseConsulta.hayError) {
			return RespuestaMB.error();
		}
		String alertas = "";
		for (Objeto registro : sqlResponseConsulta.registros) {
			alertas = registro.string("a_alertas");
		}
		// Esta lógica la hago para que no se borren las configuraciones viejas por las
		// dudas que en algún momento las volvamos a utilizar
		alertas = alertas.replace("|A_ACC|", "|");
		alertas = alertas.replace("A_ACC|", "");
		alertas = alertas.replace("|A_ACC", "");
		alertas = alertas.replace("|A_CCL|", "|");
		alertas = alertas.replace("A_CCL|", "");
		alertas = alertas.replace("|A_CCL", "");
		alertas = alertas.replace("|A_DBC|", "|");
		alertas = alertas.replace("A_DBC|", "");
		alertas = alertas.replace("|A_DBC", "");
		alertas = alertas.replace("|A_BNF|", "|");
		alertas = alertas.replace("A_BNF|", "");
		alertas = alertas.replace("|A_BNF", "");
		alertas = alertas.trim();

		if (ingresos)
			alertas = alertas + "A_ACC|";
		if (cambioClave)
			alertas = alertas + "A_CCL|";
		if (desbloqueoClave)
			alertas = alertas + "A_DBC|";
		if (agendarBeneficiario)
			alertas = alertas + "A_BNF|";

		SqlRequestMB sqlRequest = SqlMB.request("InsertOrUpdateConfiguracionAlertas", "homebanking");
		sqlRequest.sql = "UPDATE [homebanking].[dbo].[alertas_por_mail] ";
		sqlRequest.sql += "SET [a_alertas] = ? ";
		sqlRequest.sql += ",[a_email] = ? ";
		sqlRequest.sql += "WHERE [a_id] = ? ";
		sqlRequest.add(alertas);
		sqlRequest.add(contexto.persona().email());
		sqlRequest.add(contexto.idCobis());

		sqlRequest.sql += "IF @@ROWCOUNT = 0 ";
		sqlRequest.sql += "INSERT INTO [homebanking].[dbo].[alertas_por_mail] ([a_id] ,[a_alertas] ,[a_email]) ";
		sqlRequest.sql += "VALUES (?, ?, ?) ";
		sqlRequest.add(contexto.idCobis());
		sqlRequest.add(alertas);
		sqlRequest.add(contexto.persona().email());

		SqlResponseMB sqlResponseUpdate = SqlMB.response(sqlRequest);
		if (sqlResponseUpdate.hayError) {
			return RespuestaMB.error();
		}

		return RespuestaMB.exito();
	}

	public static String eliminarTagsInnecesarias(String texto) {

		if (texto != null) {
			texto = texto.replace("<![CDATA[", "");
			texto = texto.replace("]]>", "");
			texto = texto.replace("<p>", "");
			texto = texto.replace("</p>", "");
		}

		return texto;
	}

	public static RespuestaMB sendSmsOTP(ContextoMB contexto) {
		try {
			String telefono = contexto.parametros.string("telefono");
			String mensaje = contexto.parametros.string("mensaje");
			String codigo = contexto.parametros.string("codigo");
			String motivo = contexto.parametros.string("motivo");
			String tipoMotivo = contexto.parametros.string("tipoMotivo");

			if (telefono == null || telefono.isEmpty()) {
				return RespuestaMB.parametrosIncorrectos();
			}

			if (codigo == null || codigo.isEmpty()) {
				return RespuestaMB.parametrosIncorrectos();
			}

			if (motivo != null && !motivo.isEmpty()) {
				switch (tipoMotivo) {
				case "transferencia":
					mensaje = "Nunca compartas esta clave. Tu codigo de seguridad Banco Hipotecario es " + codigo + " " + motivo;
					break;
				default:
					break;
				}
			}

			if (mensaje == null || mensaje.isEmpty()) {
				return RespuestaMB.estado("ERROR_TEXTO_MENSAJE");
			}

			ApiResponseMB response = RestNotificaciones.sendSms(contexto, telefono, mensaje, codigo);
			if (response.hayError()) {
				return RespuestaMB.error();
			}

			RespuestaMB respuesta = new RespuestaMB();
			return respuesta.set("respuesta", response.objetos());
		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}

	// TODO dejarlo async
	public static RespuestaMB notificationesDescarga(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		ApiResponseMB response = RestPostventa.obtenerCasos(contexto);
		Objeto datos = (Objeto) response.get("Datos");

		for (Objeto caso : datos.objetos()) {
			Map<String, Object> notificacion = new HashMap<String, Object>();

			String notasResolucion = caso.get("NotasResolucion") == null ? "" : (String) caso.get("NotasResolucion");
			String numeroCaso = caso.get("NumeroCaso") == null ? "" : (String) caso.get("NumeroCaso");

			String fechaUltimaModificacion = "";
			try {
				if (caso.get("FechaUltimaModificacion") != null) {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
					OffsetDateTime dateTime = OffsetDateTime.parse((String) caso.get("FechaUltimaModificacion"));
					fechaUltimaModificacion = dateTime.format(formatter);
				}
			} catch (Exception e) {
				fechaUltimaModificacion = "-";
			}

			notificacion.put("title", caso.get("Titulo"));
			notificacion.put("id", caso.get("IdCaso"));
			notificacion.put("dateDesc", fechaUltimaModificacion);
			notificacion.put("read", caso.get("NotificacionLeida"));
			notificacion.put("numeroCaso", numeroCaso);
			notificacion.put("descripcion", "<p><font color= #FC7B41>" + caso.string("Nombres", contexto.persona().nombre()) + ",</font></p> Ya nos ocupamos de tu solicitud número: <font color= #FC7B41>" + numeroCaso + "</font> Te informamos que la misma se encuentra finalizada habiendo llegado a la siguiente resolución: <br><font color= #FC7B41>" + notasResolucion + "</font><br> En caso de haber recibido un archivo adjunto vas a poder descargarlo desde tu mail personal.");

			if (caso.get("Adjuntos") == null || "{}".equals(caso.get("Adjuntos"))) {
				notificacion.put("attach", null);
			} else {
				notificacion.put("attach", caso.get("Adjuntos"));
			}
			respuesta.add("notificaciones", notificacion);
		}

		// TODO: Habilitar cuando se implemente por los FRONT
		try {
			if (!ConfigMB.esProduccion()) {
				if (!contexto.persona().esEmpleado() || (contexto.persona().esEmpleado() && MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoAumentoLimiteEmpleados"))) {
					// AUMENTO LIMITE TC
					RespuestaMB respuestaNotifAL = MBAumentoLimiteTC.notificacionesAumetoLimite(contexto);
					if (!respuestaNotifAL.hayError()) {
						for (Objeto notif : (ArrayList<Objeto>) respuestaNotifAL.objetos("notificacionesAL")) {
							respuesta.add("notificaciones", notif);
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		return respuesta;
	}

	public static RespuestaMB actualizarNotificacion(ContextoMB contexto) {
		String numeroCaso = contexto.parametros.string("numeroCaso");
		String type = contexto.parametros.string("type", "");

		if (Objeto.anyEmpty(numeroCaso)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		try {
			RespuestaMB respuesta = new RespuestaMB();

			if (!type.isEmpty()) {
				Boolean leida = contexto.parametros.bool("notificacionLeida", false);
				Boolean borrar = contexto.parametros.bool("notificacionBorrada", false);
				SqlNotificaciones.updateNotificacionSolicitudLeidoBorrado(contexto, numeroCaso, leida, borrar);
				switch (type) {
					case "aumento-limite" -> SqlNotificaciones.updateNotificacionSolicitudLeidoBorrado(contexto, numeroCaso, leida, borrar);
					case "INV" -> SqlNotificaciones.updateNotificacionesPorIdCobisLeidoBorrado(contexto, leida, borrar);
					case "CRM-SV" -> {
						String nroSolicitud = numeroCaso.split("-")[1];
//						SqlSucursalVirtual.notificacionLeida(contexto.idCobis() ,nroSolicitud);
						SqlSucursalVirtual.SPGeneric("leer_notificacion", nroSolicitud, contexto.idCobis(), null);
					}
				};
			} else {
				ApiResponseMB response = RestPostventa.actualizarNotificaciones(contexto);
				if (response.hayError()) {
					respuesta.setEstado("ERROR");
					respuesta.set("mensaje", "Hubo un error al procesar la notificación, proba nuevamente mas tarde.");
					return respuesta;
				}
			}

			respuesta.set("mensaje", "Notificación actualizada correctamente.");
			return respuesta;

		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}

	// Envio Email Adelanto de Sueldo

	public static RespuestaMB envioEmailPP(ContextoMB contexto) {

		try {
			Objeto parametros = new Objeto();
			parametros.set("Subject", "Solicitaste un préstamo");
			parametros.set("NOMBRE", contexto.persona().nombre());
			parametros.set("APELLIDO", contexto.persona().apellido());
			parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
			Date hoy = new Date();
			parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
			parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
            parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
			parametros.set("TITULAR_CANAL", contexto.persona().apellido());
			parametros.set("NOMBRE_PRESTAMO", "personal");
			parametros.set("MOSTRAR_ARREPENTIMIENTO", "true");

			if ("true".equals(ConfigMB.string("salesforce_prendido_alta_prestamo")) && MBSalesforce.prendidoSalesforceAmbienteBajoConFF(contexto)) {
				var salesforcePf = contexto.parametros.objeto(ConfigMB.string("salesforce_alta_prestamo"));
				String salesforce_alta_prestamo = ConfigMB.string("salesforce_alta_prestamo");
				parametros.set("IDCOBIS", contexto.idCobis());
				parametros.set("MONTO_PRESTAMO", salesforcePf.string("MONTO_PRESTAMO"));
				if(salesforcePf.string("FECHA_VENCIMIENTO_CUOTA") != null) {
			        LocalDate today = LocalDate.now();
			        LocalDate fechaVencimiento = LocalDate.of(today.getYear(), today.getMonth().plus(1), Integer.parseInt(salesforcePf.string("FECHA_VENCIMIENTO_CUOTA")));
			        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
					parametros.set("FECHA_VENCIMIENTO_CUOTA", fechaVencimiento.format(formatter).toString());
				}
				parametros.set("MONTO_CUOTA", salesforcePf.string("MONTO_CUOTA"));
				parametros.set("NUMERO_CUOTA", "1");
				new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_alta_prestamo, parametros));
			}
			else
				RestNotificaciones.envioMail(contexto, ConfigMB.string("doppler_alta_prestamo"), parametros);


			String celular = contexto.persona().celular();
			if (celular != null && !"".equals(celular)) {
				RestNotificaciones.sendSms(contexto, celular, "Solicitaste un préstamo personal desde el canal Banca Movíl si no fuiste vos llamanos al 0810 222 7777", "");
			}

		} catch (Exception e) {
		}
		return RespuestaMB.exito();
	}

	public static RespuestaMB envioEmailSolicitaTuAdelanto(ContextoMB contexto) {

		try {
			Objeto parametros = new Objeto();
			parametros.set("Subject", contexto.persona().nombre() + ", ¡solicitá tu adelanto de sueldo!");
			parametros.set("NOMBRE", contexto.persona().nombre());

			RestNotificaciones.envioMail(contexto, ConfigMB.string("doppler_solicita_ya_tu_adelanto"), parametros);
		} catch (Exception e) {
		}

		return RespuestaMB.exito();
	}

	public static RespuestaMB envioEmailDesembolsoAdelanto(ContextoMB contexto) {

		try {
			Objeto parametros = new Objeto();
			parametros.set("Subject", contexto.persona().nombre() + ", ¡Ya tenés tu adelanto de sueldo!");
			parametros.set("NOMBRE", contexto.persona().nombre());
			parametros.set("APELLIDO", contexto.persona().apellido());
		parametros.set("MONTO", contexto.parametros.bigDecimal("monto"));
			parametros.set("CUENTA", "******" + contexto.cuenta(contexto.parametros.string("cuenta")).ultimos4digitos());
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.DATE, +45);
			Date fechaLimite = cal.getTime();
			parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(fechaLimite));

			if (!MBSalesforce.prendidoSalesforce(contexto.idCobis())) {
				RestNotificaciones.envioMail(contexto, ConfigMB.string("doppler_desembolso_adelanto"), parametros);
			} else {
				String salesforce_desembolso_adelanto = ConfigMB.string("salesforce_desembolso_adelanto");
				parametros.set("IDCOBIS", contexto.idCobis());
				parametros.set("ISMOBILE", true);
                parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
				new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, salesforce_desembolso_adelanto, parametros));
			}
		} catch (Exception e) {
		}

		String celular = contexto.persona().celular();
		if (celular != null && !"".equals(celular)) {
			RestNotificaciones.sendSms(contexto, celular, "Solicitaste un Adelanto de sueldo desde el canal Banca Movíl si no fuiste vos llamanos al 0810 222 7777", "");
		}

		return RespuestaMB.exito();
	}

	public static RespuestaMB envioEmailVencimientoAdelanto(ContextoMB contexto) {

		try {
			Objeto parametros = new Objeto();
			parametros.set("Subject", contexto.persona().nombre() + ", ¡tenés un vencimiento en los próximos días!");
			parametros.set("NOMBRE", contexto.persona().nombre());
			parametros.set("MONTO", contexto.parametros.bigDecimal("monto"));
			Date fechaLimite = new Date(Calendar.DATE + 45);
			parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(fechaLimite));
			parametros.set("CUENTA", contexto.cuenta(contexto.parametros.string("idCuenta")));

			RestNotificaciones.envioMail(contexto, ConfigMB.string("doppler_vencimiento_adelanto"), parametros);
		} catch (Exception e) {
		}

		return RespuestaMB.exito();
	}

	public static RespuestaMB envioEmailSeDebitoTuAdelanto(ContextoMB contexto) {

		try {
			Objeto parametros = new Objeto();
			parametros.set("Subject", contexto.persona().nombre() + ", ¡Pagaste tu préstamo Adelanto de Sueldo!");
			parametros.set("NOMBRE", contexto.persona().nombre());

			RestNotificaciones.envioMail(contexto, ConfigMB.string("doppler_debito_adelanto"), parametros);
		} catch (Exception e) {
		}

		return RespuestaMB.exito();
	}
}
