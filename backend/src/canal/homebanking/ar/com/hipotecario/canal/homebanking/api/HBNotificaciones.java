package ar.com.hipotecario.canal.homebanking.api;

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
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.lib.Encriptador;
import ar.com.hipotecario.canal.homebanking.servicio.RestNotificaciones;
import ar.com.hipotecario.canal.homebanking.servicio.RestPostventa;
import ar.com.hipotecario.canal.homebanking.servicio.SqlNotificaciones;
import ar.com.hipotecario.canal.homebanking.servicio.SqlSucursalVirtual;

public class HBNotificaciones {

	private static ExecutorService pool = Executors.newCachedThreadPool();

	/* ========== PAGOS ========== */
	public static Respuesta notificaciones(ContextoHB contexto) {
		try {
			ApiResponse responsePriorizadas = RestNotificaciones.cacheNotificacionesPriorizadas(contexto);
			if (responsePriorizadas.hayError()) {
				return Respuesta.error();
			}

			Objeto datos = new Objeto();
			List<String> listaIdsPriorizados = new ArrayList<String>();
			for (Objeto item : responsePriorizadas.objetos()) {

				// TODO: para buscar y avanzar solo con el de mora
				Boolean soloDefaults = false;
				if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "hb_prendido_solodefault_dic")) {
					String jsonDatosAdicionales = item.string("datosAdicionales");
					if(jsonDatosAdicionales == null || jsonDatosAdicionales.isEmpty()) {
						soloDefaults = true;
						continue;
					}

					if (!jsonDatosAdicionales.isEmpty()) {
						Objeto arrayDatosAdicionales = Objeto.fromJson(jsonDatosAdicionales);
						for (Objeto datosAdicionales : arrayDatosAdicionales.objetos()) {
							String idPantalla = datosAdicionales.string("idPantalla");
							if (!"MORAT".equalsIgnoreCase(idPantalla) && !"MORAA".equalsIgnoreCase(idPantalla)) {
								soloDefaults = true;
								continue;
							}
						}
					}
				}

				if (soloDefaults) {
					continue;
				}
				// TODO: para buscar y avanzar solo con el de mora

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
						objeto.set("urlDestino", datosAdicionales.string("urlDestino"));

						String idPantalla = datosAdicionales.string("idPantalla");
						if ("MORAT".equalsIgnoreCase(idPantalla) || "MORAA".equalsIgnoreCase(idPantalla)) {
//							objeto.set("urlDestino", urlCobranzas(contexto.persona().numeroDocumento()));
							notificacion.set("texto", ConfigHB.string("hb_texto_mora"));
							notificacion.set("botonSi", ConfigHB.string("hb_texto_boton_mora"));
							objeto.set("urlDestino", ConfigHB.string("hb_url_whatsapp"));

						}

						notificacion.set("datosAdicionales", objeto);
					}
				}

				datos.add("listaPriorizadas", notificacion);
			}

			ApiResponse responseNotificaciones = RestNotificaciones.cacheNotificaciones(contexto);
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
			return Respuesta.exito("notificaciones", datos);
		} catch (Exception e) {
			return Respuesta.error();
		}
	}

	public static Respuesta notificacionesRespuesta(ContextoHB contexto) {
		String idAlerta = contexto.parametros.string("idAlerta");
		String tipoRespuesta = contexto.parametros.string("tipoRespuesta");
		String textoRespuesta = contexto.parametros.string("textoRespuesta");
		ApiRequest request = Api.request("NotificacionesPostRespuesta", "notificaciones", "POST", "/v1/notificaciones/{id}/alertas", contexto);
		request.path("id", idAlerta);
		request.body("id", idAlerta);
		request.body("idCliente", contexto.idCobis());
		request.body("tipoRespuesta", tipoRespuesta);
		request.body("textoRespuesta", textoRespuesta);
		ApiResponse response = Api.response(request);
		if (response.hayError()) {
			return Respuesta.error();
		}

		return Respuesta.exito();
	}

	public static Respuesta configuracionAlertas(ContextoHB contexto) {
		SqlResponse sqlResponse = RestNotificaciones.consultaConfiguracionAlertas(contexto);
		if (sqlResponse.hayError) {
			return Respuesta.error();
		}
		String alertas = "";
		for (Objeto registro : sqlResponse.registros) {
			alertas = registro.string("a_alertas");
		}

		Respuesta respuesta = new Respuesta();
		respuesta.set("ingresos", alertas.contains("A_ACC"));
		respuesta.set("cambioClave", alertas.contains("A_CCL"));
		respuesta.set("desbloqueoClave", alertas.contains("A_DBC"));
		respuesta.set("agendarBeneficiario", alertas.contains("A_BNF"));

		return respuesta;
	}

	public static Respuesta modificarConfiguracionAlertas(ContextoHB contexto) {
		Boolean ingresos = contexto.parametros.bool("ingresos", false);
		Boolean cambioClave = contexto.parametros.bool("cambioClave", false);
		Boolean desbloqueoClave = contexto.parametros.bool("desbloqueoClave", false);
		Boolean agendarBeneficiario = contexto.parametros.bool("agendarBeneficiario", false);

		SqlResponse sqlResponseConsulta = RestNotificaciones.consultaConfiguracionAlertas(contexto);
		if (sqlResponseConsulta.hayError) {
			return Respuesta.error();
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

		SqlRequest sqlRequest = Sql.request("InsertOrUpdateConfiguracionAlertas", "homebanking");
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

		SqlResponse sqlResponseUpdate = Sql.response(sqlRequest);
		if (sqlResponseUpdate.hayError) {
			return Respuesta.error();
		}

		return Respuesta.exito();
	}

	public static Respuesta sendSmsOTP(ContextoHB contexto) {
		String telefono = contexto.parametros.string("telefono");
		String mensaje = contexto.parametros.string("mensaje");
		String codigo = contexto.parametros.string("codigo");
		String motivo = contexto.parametros.string("motivo");
		String tipoMotivo = contexto.parametros.string("tipoMotivo");

		try {
			if (telefono == null || telefono.isEmpty()) {
				return Respuesta.parametrosIncorrectos();
			}

			if (codigo == null || codigo.isEmpty()) {
				return Respuesta.parametrosIncorrectos();
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
				return Respuesta.estado("ERROR_TEXTO_MENSAJE");
			}

			ApiResponse response = RestNotificaciones.sendSms(contexto, telefono, mensaje, codigo);
			if (response.hayError()) {
				return Respuesta.error();
			}

			Respuesta respuesta = new Respuesta();
			return respuesta.set("respuesta", response.objetos());
		} catch (Exception exception) {
			if (exception.toString().contains("SocketTimeoutException")) {
				contexto.insertarLogEnvioOtp(contexto, telefono, null, null, null, "T");
			}
			return Respuesta.error();
		}
	}

	public static Respuesta notificationesDescargaAsync(ContextoHB contexto) {
		if (!ConfigHB.esProduccion() && !ConfigHB.esOpenShift()) {
			return notificationesDescarga(contexto);
		}

		Respuesta respuesta = new Respuesta();
		try {
			Future<Respuesta> future = pool.submit(() -> notificationesDescarga(contexto));
			respuesta = future.get(ConfigHB.integer("notificacionesDescarga", 20), TimeUnit.SECONDS);
		} catch (Exception e) {
		}
		return respuesta;
	}

	@SuppressWarnings("unchecked")
	public static Respuesta notificationesDescarga(ContextoHB contexto) {
		try {
			HBProcesos.desistirSolicitudes(contexto);
		} catch (Exception e) {
		}

		Respuesta respuesta = new Respuesta();
		ApiResponse response = RestPostventa.obtenerCasos(contexto);
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

			notificacion.put("nombre", caso.string("Nombres", contexto.persona().nombre()));
			notificacion.put("notasResolucion", notasResolucion);
			notificacion.put("parte1", "Ya nos ocupamos de tu solicitud número:");
			notificacion.put("parte2", "Te informamos que la misma se encuentra finalizada habiendo llegado a la siguiente resolución:");
			notificacion.put("parte3", "En caso de haber recibido un archivo adjunto vas a poder descargarlo desde tu mail personal.");

			if (caso.get("Adjuntos") == null || "{}".equals(caso.get("Adjuntos"))) {
				notificacion.put("attach", null);
			} else {
				Objeto adj = (Objeto) caso.get("Adjuntos");
				List<Objeto> attach = new ArrayList<Objeto>();
				for (Entry<String, Object> entry : adj.toMap().entrySet()) {
					Objeto adjunto = new Objeto();
					adjunto.set("name", entry.getKey()).set("fileId", entry.getValue());
					attach.add(adjunto);
				}
				notificacion.put("attach", attach);
			}
			respuesta.add("notificaciones", notificacion);
		}

		try {
			// AUMENTO LIMITE TC
			Respuesta respuestaNotifAL = HBAumentoLimiteTC.notificacionesAumetoLimite(contexto);
			if (!respuestaNotifAL.hayError()) {
				for (Objeto notif : (ArrayList<Objeto>) respuestaNotifAL.get("notificacionesAL")) {
					respuesta.add("notificaciones", notif);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}

		// TODO revisar para ordernar por fecha
		// respuesta.objeto("notificaciones").ordenar("dateDesc");
		return respuesta;
	}

	private static String urlCobranzas(String documento) {
		String url = "https://hipotecario-cobranzas.com.ar/login?";
		try {
			Integer suma = 0;
			String fechaActual = new SimpleDateFormat("dd/MM/yyyy").format(new Date());

			for (int i = 0; i < fechaActual.length(); i++) {
				if (Character.isDigit(fechaActual.charAt(i))) {
					suma += Integer.parseInt(Character.toString(fechaActual.charAt(i)));
				}
			}

			suma += Integer.parseInt(documento);

			String md5 = Encriptador.md5(suma.toString()).toLowerCase();
			url = String.format(url + "parametro=%s&token=%s", documento, md5);

			return url;
		} catch (Exception e) {
			e.printStackTrace();
			return url;
		}
	}

	public static Respuesta actualizarNotificacion(ContextoHB contexto) {
		String numeroCaso = contexto.parametros.string("numeroCaso");
		String type = contexto.parametros.string("type", "");

		if (Objeto.anyEmpty(numeroCaso)) {
			return Respuesta.parametrosIncorrectos();
		}

		try {
			Respuesta respuesta = new Respuesta();
			if (!type.isEmpty()) {
				Boolean leida = contexto.parametros.bool("notificacionLeida", false);
				Boolean borrar = contexto.parametros.bool("notificacionBorrada", false);
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
				ApiResponse response = RestPostventa.actualizarNotificaciones(contexto);
				if (response.hayError()) {
					respuesta.setEstado("ERROR");
					respuesta.set("mensaje", "Hubo un error al procesar la notificación, proba nuevamente mas tarde.");
					return respuesta;
				}
			}
			respuesta.set("mensaje", "Notificación actualizada correctamente.");
			return respuesta;
		} catch (Exception e) {
			return Respuesta.error();
		}
	}

	public static Respuesta consultarNotificacionPorParametro(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		String parametro = contexto.parametros.string("parametro", null);
		if (Objeto.anyEmpty(parametro)) {
			return Respuesta.parametrosIncorrectos();
		}

		SqlResponse sqlResponseConsulta = RestNotificaciones.consultaNotificacionesHabilitada(contexto, parametro);
		if (sqlResponseConsulta.hayError) {
			return Respuesta.error();
		}
		respuesta.set("existe", sqlResponseConsulta.registros.isEmpty() ? false : true);
		respuesta.set("habilitado", sqlResponseConsulta.registros.isEmpty() ? 0 : sqlResponseConsulta.registros.get(0).integer("habilita_notificacion") == 1);

		return respuesta;
	}

	public static Respuesta guardarNotificacionPorParametro(ContextoHB contexto) {
		String parametro = contexto.parametros.string("parametro", null);
		boolean habilitado = contexto.parametros.bool("habilitado", false);
		Respuesta respuestaConsulta = consultarNotificacionPorParametro(contexto);
		SqlRequest sqlRequest = validaCreaActualizaPermiso(respuestaConsulta, contexto, parametro, habilitado ? 1 : 0);
		SqlResponse sqlResponse = Sql.response(sqlRequest);
		if (sqlResponse.hayError) {
			return Respuesta.error();
		}
		return Respuesta.exito();
	}

	private static SqlRequest validaCreaActualizaPermiso(Respuesta respuestaConsulta, ContextoHB contexto, String parametro, int habilitado) {
		return !respuestaConsulta.bool("existe") ? registrarPermisoNotificacion(contexto, parametro, habilitado) : actualizarPermisoNotificacion(contexto, parametro, habilitado);
	}

	private static SqlRequest registrarPermisoNotificacion(ContextoHB contexto, String parametro, int habilitado) {
		SqlRequest sqlRequest = Sql.request("CrearPermisoCobis", "homebanking");
		sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[notificacion_por_cobis] (id_cobis, parametro, canal, habilita_notificacion) VALUES (?, ?, ?, ?)";
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(parametro);
		sqlRequest.parametros.add("HB");
		sqlRequest.parametros.add(habilitado);
		return sqlRequest;
	}

	private static SqlRequest actualizarPermisoNotificacion(ContextoHB contexto, String parametro, int habilitado) {
		SqlRequest sqlRequest = Sql.request("ActualizarPermisoCobis", "homebanking");
		sqlRequest.sql = "UPDATE [Homebanking].[dbo].[notificacion_por_cobis] set habilita_notificacion = ?, canal = ?  WHERE id_cobis = ? AND parametro =? ";
		sqlRequest.parametros.add(habilitado);
		sqlRequest.parametros.add("HB");
		sqlRequest.parametros.add(contexto.idCobis());
		sqlRequest.parametros.add(parametro);
		return sqlRequest;
	}

	public static Respuesta envioEmailPP(ContextoHB contexto) {
		try {

			if (true) {
				Objeto parametros = new Objeto();
				parametros.set("Subject", "Solicitaste un préstamo");
				parametros.set("NOMBRE", contexto.persona().nombre());
				parametros.set("APELLIDO", contexto.persona().apellido());
				parametros.set("NOMBRE_APELLIDO", contexto.persona().nombre() + " " + contexto.persona().apellido());
				Date hoy = new Date();
				parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
				parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
				parametros.set("CANAL", "Home Banking");
				parametros.set("TITULAR_CANAL", contexto.persona().apellido());
				parametros.set("NOMBRE_PRESTAMO", "personal");
				parametros.set("MOSTRAR_ARREPENTIMIENTO", "true");


				if ("true".equals(ConfigHB.string("salesforce_prendido_alta_prestamo")) && HBSalesforce.prendidoSalesforceAmbienteBajoConFF(contexto)) {
					var parametrosSf = Objeto.fromJson(contexto.sesion.cache.get(ConfigHB.string("salesforce_alta_prestamo")));
					String salesforce_alta_prestamo = ConfigHB.string("salesforce_alta_prestamo");
					parametros.set("MONTO_PRESTAMO", parametrosSf.string("MONTO_PRESTAMO"));
					if(parametrosSf.string("FECHA_VENCIMIENTO_CUOTA") != null) {
				        LocalDate today = LocalDate.now();
				        LocalDate fechaVencimiento = LocalDate.of(today.getYear(), today.getMonth().plus(1), Integer.parseInt(parametrosSf.string("FECHA_VENCIMIENTO_CUOTA")));
				        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
						parametros.set("FECHA_VENCIMIENTO_CUOTA", fechaVencimiento.format(formatter).toString());
					}
					parametros.set("MONTO_CUOTA", parametrosSf.string("MONTO_CUOTA"));
					parametros.set("NUMERO_CUOTA", "1");
					new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_alta_prestamo, parametros));
				}
				else
					RestNotificaciones.envioMail(contexto, ConfigHB.string("doppler_alta_prestamo"), parametros);


				String celular = contexto.persona().celular();
				if (celular != null && !"".equals(celular)) {
					RestNotificaciones.sendSms(contexto, celular, "Solicitaste un prestamo personal desde el canal Home Banking si no fuiste vos llamanos al 0810 222 7777", "");
				}
			}
		} catch (Exception e) {
		}
		contexto.sesion.cache.remove(ConfigHB.string("salesforce_alta_prestamo"));
		return Respuesta.exito();
	}

	public static Respuesta envioEmailSolicitaTuAdelanto(ContextoHB contexto) {
		try {
			Objeto parametros = new Objeto();
			parametros.set("Subject", contexto.persona().nombre() + ", ¡solicitá tu adelanto de sueldo!");
			parametros.set("NOMBRE", contexto.persona().nombre());
			Date hoy = new Date();
			parametros.set("FECHA_HOY", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
			Date fechaLimite = new Date(Calendar.DATE + 7);
			parametros.set("FECHA_LIMITE", new SimpleDateFormat("dd/MM/yyyy").format(fechaLimite));

			RestNotificaciones.envioMail(contexto, ConfigHB.string("doppler_solicita_ya_tu_adelanto"), parametros);
		} catch (Exception e) {
		}

		return Respuesta.exito();
	}

	public static Respuesta envioEmailDesembolsoAdelanto(ContextoHB contexto) {

		try {
			Objeto parametros = new Objeto();
			parametros.set("Subject", "Aviso de desembolso Adelanto de Sueldo");
			parametros.set("NOMBRE", contexto.persona().nombre());
			parametros.set("MONTO", contexto.parametros.bigDecimal("monto"));
			parametros.set("CUENTA", "******" + contexto.cuenta(contexto.parametros.string("cuenta")).ultimos4digitos());
			Date fechaLimite = new Date(Calendar.DATE + 45);
			parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(fechaLimite));

			if (!HBSalesforce.prendidoSalesforce(contexto.idCobis())) {
				RestNotificaciones.envioMail(contexto, "doppler_desembolso_adelanto", parametros);
			} else {
				var parametrosSf = Objeto.fromJson(contexto.sesion.cache.get(ConfigHB.string("salesforce_alta_prestamo")));
				String salesforce_desembolso_adelanto = ConfigHB.string("salesforce_desembolso_adelanto");
				parametros.set("APELLIDO", contexto.persona().apellido());
				parametros.set("FECHA_VENCIMIENTO", parametrosSf.string("FECHA_VENCIMIENTO_CUOTA"));
				parametros.set("NUMERO_CUENTA", "******" + contexto.cuenta(contexto.parametros.string("cuenta")).ultimos4digitos());
				parametros.set("CANAL", "Home Banking");
				new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_desembolso_adelanto, parametros));
			}
		} catch (Exception e) {
		}

		String celular = contexto.persona().celular();
		if (celular != null && !"".equals(celular)) {
			RestNotificaciones.sendSms(contexto, celular, "Solicitaste un Adelanto de sueldo desde el canal Banca Movíl si no fuiste vos llamanos al 0810 222 7777", "");
		}

		contexto.sesion.cache.remove(ConfigHB.string("salesforce_alta_prestamo"));
		return Respuesta.exito();
	}

	public static Respuesta envioEmailVencimientoAdelanto(ContextoHB contexto) {

		try {
			Objeto parametros = new Objeto();
			parametros.set("Subject", contexto.persona().nombre() + ", ¡tenés un vencimiento en los próximos días!");
			parametros.set("NOMBRE", contexto.persona().nombre());
			parametros.set("MONTO", contexto.parametros.bigDecimal("monto"));
			Date fechaLimite = new Date(Calendar.DATE + 3);
			parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(fechaLimite));
			parametros.set("CUENTA", "******" + contexto.cuenta(contexto.parametros.string("idCuenta")).ultimos4digitos());

			RestNotificaciones.envioMail(contexto, ConfigHB.string("doppler_vencimiento_adelanto"), parametros);
		} catch (Exception e) {
		}

		return Respuesta.exito();
	}

	public static Respuesta envioEmailSeDebitoTuAdelanto(ContextoHB contexto) {

		try {
			Objeto parametros = new Objeto();
			parametros.set("Subject", contexto.persona().nombre() + ", ¡Pagaste tu préstamo Adelanto de Sueldo!");
			parametros.set("NOMBRE", contexto.persona().nombre());

			RestNotificaciones.envioMail(contexto, ConfigHB.string("doppler_debito_adelanto"), parametros);
		} catch (Exception e) {
		}

		return Respuesta.exito();
	}

	// Envio Mail Canal Amarillo

	public static Respuesta envioEmailSolicitudOfertaMejorada(ContextoHB contexto) {
		try {
			Objeto parametros = new Objeto();
			parametros.set("Subject", contexto.persona().nombre() + ", ¡Solicitaste mejorar la oferta de tu Préstamo Personal!");
			parametros.set("NOMBRE", contexto.persona().nombre());

			RestNotificaciones.envioMail(contexto, ConfigHB.string("doppler_solicitud_oferta_mejorada"), parametros);

		} catch (Exception e) {
		}

		return Respuesta.exito();
	}
}
