package ar.com.hipotecario.mobile.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.google.gson.Gson;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.excepcion.ApiVentaExceptionMB;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Texto;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.negocio.Solicitud;
import ar.com.hipotecario.mobile.negocio.SolicitudAumentoLimite;
import ar.com.hipotecario.mobile.negocio.TarjetaCredito;
import ar.com.hipotecario.mobile.servicio.RestAumentoLimiteTC;

public class MBAumentoLimiteTC {

	static final String PARAM_IDTARJETACREDITO = "idTarjetaCredito";
	static final String PARAM_IDSOLICITUD = "idSolicitud";
	static final String PARAM_DATO_IDSOLICITUD = "IdSolicitud";
	static final String CLAVE_ERROR = "ERROR";
	static final String VALOR_ERROR = "error";
	static final String VALOR_ERRORES = "Errores";
	static final String CLAVE_DATOS = "Datos";
	static final String CLAVE_ESTADO = "estado";
	static final String VALOR_ESTADO_ENCURSO = "EN_CURSO";
	static final String CLAVE_ESTADO_MOTOR = "estadoMotor";
	static final String CLAVE_MENSAJE_CLIENTE = "MensajeCliente";
	static final String FINALIZAR_SERVICIO_AUMENTO = "finalizarSolicitudAumentoLimiteTC";
	static final String ESTADO_AMARILLO = "AMARILLO";
	static final String ESTADO_APROBADO_AMARILLO = "APROBADO_AMARILLO";
	static final String ESTADO_ROJO = "ROJO";

	public static RespuestaMB crearSolicitudAumentoLimiteTC(ContextoMB contexto) {

		String idTarjetaCredito = contexto.parametros.string(PARAM_IDTARJETACREDITO, null);
		if (Objeto.anyEmpty(idTarjetaCredito).booleanValue()) {
			return RespuestaMB.parametrosIncorrectos();
		}

		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			return RespuestaMB.error();
		}

		// Generar Solicitud
		ApiResponseMB generarSolicitud = RestAumentoLimiteTC.generarSolicitud(contexto);
		if (hayErrores(generarSolicitud)) {
			Solicitud.logApiVentas(contexto, "-", "generarSolicitud", generarSolicitud);
			return new RespuestaMB().setEstado(CLAVE_ERROR).set(VALOR_ERROR, mensajeError(generarSolicitud));
		}
		String idSolicitud = generarSolicitud.objetos(CLAVE_DATOS).get(0).string(PARAM_DATO_IDSOLICITUD);
		Solicitud.logApiVentas(contexto, idSolicitud, "generarSolicitud", generarSolicitud);

		// Generar Integrante
		ApiResponseMB generarIntegrante = RestAumentoLimiteTC.generarIntegrante(contexto, idSolicitud);
		Solicitud.logApiVentas(contexto, idSolicitud, "generarIntegrante", generarIntegrante);
		if (hayErrores(generarIntegrante)) {
			return new RespuestaMB().setEstado(CLAVE_ERROR).set(VALOR_ERROR, mensajeError(generarIntegrante));
		}

		// Generar solicitad aumento
		ApiResponseMB generarAumentoLimiteTC = RestAumentoLimiteTC.generarAumentoLimiteTC(contexto, idSolicitud, tarjetaCredito.cuenta());
		Solicitud.logApiVentas(contexto, idSolicitud, "generarAumentoLimiteTC", generarAumentoLimiteTC);
		if (hayErrores(generarAumentoLimiteTC)) {
			return new RespuestaMB().setEstado(CLAVE_ERROR).set(VALOR_ERROR, mensajeError(generarAumentoLimiteTC));
		}

		// Transformando información
		SolicitudAumentoLimite solicitudAumentoLimite = transformarInformacionAumento(generarAumentoLimiteTC);

		// Ejecutar Motor
		ApiResponseMB responseMotor = ejecutarMotorResponse(contexto, idSolicitud, false, contexto.esPlanSueldo());
		if (hayErrores(responseMotor)) {
			return RespuestaMB.error().set(VALOR_ERROR, mensajeError(responseMotor));
		}

		String resolucionId = retornarDatoMotor(responseMotor, "ResolucionId");
		String derivarA = retornarDatoMotor(responseMotor, "DerivarA");
		String explicacion = retornarDatoMotor(responseMotor, "Explicacion");
		String esquemaEvaluacion = retornarDatoMotor(responseMotor, "EsquemaEvaluacion");

		// TODO interpretarlo como un amarillo ir a sucursal si tiene pedido algun doc
		// ¿estoy descartando el 1100?
		if (resolucionId.equals("AV") && esquemaEvaluacion.equals("CON_DATO_SIN_DOC_PLAN_SUELDO")) {
			try {
				ApiResponseMB docResponse = RestAumentoLimiteTC.documentacionObligatoriaSolicitud(contexto, idSolicitud);
				if (docResponse.get("Datos") != null) {
					for (Objeto doc : docResponse.objetos("Datos").get(0).objetos("Documentacion")) {
						if (!doc.string("Id").equals("1100")) {
							contador(contexto, "SUELDO_" + resolucionId + "_DOC");
							return RespuestaMB.estado(ESTADO_AMARILLO).set(PARAM_IDSOLICITUD, idSolicitud);
						}
					}
					contador(contexto, "SUELDO_" + resolucionId + "_SIN_DOC");
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		if ((resolucionId.equals("AV") || resolucionId.equals("AA")) && esquemaEvaluacion.contains("CON_DOC")) {
			ApiResponseMB obtenerConsultaAumento = RestAumentoLimiteTC.obtenerConsultaAumento(contexto, idSolicitud, solicitudAumentoLimite.Id);
			solicitudAumentoLimite = transformarInformacionAumento(obtenerConsultaAumento);
			contador(contexto, resolucionId + "_CON_DOC");
			contador(contexto, resolucionId + "_CON_DOC" + "_MONTOF_" + solicitudAumentoLimite.MontoOfrecido);
			return RespuestaMB.estado("CON_DOC").set(CLAVE_DATOS, formatearSolicitudAumento(solicitudAumentoLimite, idSolicitud));
		}

		if (!resolucionId.equals("AV")) {
			String estado = CLAVE_ERROR;
			estado = resolucionId.equals("AA") ? ESTADO_APROBADO_AMARILLO : estado;
			estado = resolucionId.equals("CT") ? ESTADO_AMARILLO : estado;
			estado = resolucionId.equals("RE") ? ESTADO_ROJO : estado;
			if (estado.equals(ESTADO_APROBADO_AMARILLO)) {
				ApiResponseMB obtenerConsultaAumento = RestAumentoLimiteTC.obtenerConsultaAumento(contexto, idSolicitud, solicitudAumentoLimite.Id);
				solicitudAumentoLimite = transformarInformacionAumento(obtenerConsultaAumento);
				contador(contexto, resolucionId + "_CON_DOC");
				contador(contexto, resolucionId + "_CON_DOC" + "_MONTOF_" + solicitudAumentoLimite.MontoOfrecido);
				return RespuestaMB.estado(ESTADO_APROBADO_AMARILLO).set(CLAVE_DATOS, formatearSolicitudAumento(solicitudAumentoLimite, idSolicitud));
			}
			// CT y RE sale por aca
			contador(contexto, resolucionId);

			// TODO: guardar motivo de rechazo o controlar
			try {
				Util.insertarLogMotor(contexto, idSolicitud, resolucionId, explicacion, "INCREMENTO LIMITE DE COMPRA TC", esquemaEvaluacion);
			} catch (Exception e) {
				// TODO: handle exception
			}

			return new RespuestaMB().setEstado(estado).set(VALOR_ERROR, explicacion).set(PARAM_IDSOLICITUD, idSolicitud);
		}

		if (derivarA != null && derivarA.equals("S")) {
			contador(contexto, resolucionId + "_DERIVAR_" + derivarA);
			return RespuestaMB.estado(ESTADO_AMARILLO).set(PARAM_IDSOLICITUD, idSolicitud);
		}

		if (resolucionId.equals("AV")) {
			if (!esquemaEvaluacion.equals("CON_DATO_SIN_DOC_PLAN_SUELDO")) {
				contador(contexto, resolucionId);
			}
		}

		// Consultar solicitad aumento
		ApiResponseMB obtenerConsultaAumento = RestAumentoLimiteTC.obtenerConsultaAumento(contexto, idSolicitud, solicitudAumentoLimite.Id);
		Solicitud.logApiVentas(contexto, idSolicitud, "obtenerConsultaAumento", obtenerConsultaAumento);
		if (hayErrores(obtenerConsultaAumento)) {
			return new RespuestaMB().setEstado(CLAVE_ERROR).set(VALOR_ERROR, mensajeError(obtenerConsultaAumento));
		}

		// Transformando información
		solicitudAumentoLimite = transformarInformacionAumento(obtenerConsultaAumento);
		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("datos", formatearSolicitudAumento(solicitudAumentoLimite, idSolicitud));

		contador(contexto, resolucionId + "_MONTOF_" + solicitudAumentoLimite.MontoOfrecido);

		return respuesta;
	}

	private static boolean hayErrores(ApiResponseMB error) {
		return error.hayError() || !error.objetos("Errores").isEmpty();
	}

	private static SolicitudAumentoLimite transformarInformacionAumento(Objeto datos) {
		Objeto datosAumentoLimite = datos.objetos(CLAVE_DATOS).get(0);
		return (new Gson()).fromJson(datosAumentoLimite.toJson(), SolicitudAumentoLimite.class);
	}

	private static ApiResponseMB ejecutarMotorResponse(ContextoMB contexto, String idSolicitud, boolean flagSolicitaComprobarIngresos, Boolean esPlanSueldo) {
		ApiResponseMB responseMotor = RestAumentoLimiteTC.ejecutarMotorResponse(contexto, idSolicitud, flagSolicitaComprobarIngresos, esPlanSueldo);
		Solicitud.logMotor(contexto, idSolicitud, responseMotor);
		return responseMotor;
	}

	private static String retornarDatoMotor(ApiResponseMB respuestaMotor, String parametro) {
		return respuestaMotor.objetos(CLAVE_DATOS).get(0).string(parametro);
	}

	private static Object formatearSolicitudAumento(SolicitudAumentoLimite solicitudAumentoLimite, String idSolicitud) {
		Objeto item = new Objeto();
		item.set(PARAM_IDSOLICITUD, idSolicitud);
		item.set("idSolicitudAumentoLimite", solicitudAumentoLimite.Id);
		item.set("montoAceptado", solicitudAumentoLimite.MontoAceptado);
		item.set("montoOfrecido", solicitudAumentoLimite.MontoOfrecido);

		try {
			item.set("montoAceptadoFormateado", Formateador.importePeso(solicitudAumentoLimite.MontoAceptado));
		} catch (Exception e) {
			item.set("montoAceptadoFormateado", "$ " + solicitudAumentoLimite.MontoAceptado);
		}
		try {
			item.set("montoOfrecidoFormateado", Formateador.importePeso(solicitudAumentoLimite.MontoOfrecido));
		} catch (Exception e) {
			item.set("montoOfrecidoFormateado", "$ " + solicitudAumentoLimite.MontoOfrecido);

		}

		item.set("cuenta", solicitudAumentoLimite.cuenta);
		item.set("monto", solicitudAumentoLimite.monto);
		item.set("acuerdo", solicitudAumentoLimite.Acuerdo);
		item.set("advertencias", solicitudAumentoLimite.Advertencias);
		item.set("rechazadoMotor", solicitudAumentoLimite.RechazadoMotor);
		return item;
	}

	public static RespuestaMB isAumentoLimiteTCSolicitado(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();

		if (fueraHorario()) {
			return respuesta.set(CLAVE_ESTADO, "FUERA_HORARIO");
		}

		Objeto solicitud = getDetalleSolicitud(contexto);
		Boolean chequeado = false;

		if (!solicitud.string("estado").isEmpty() && !Objeto.setOf("F", "R", "D", "RECLAMODOC").contains(solicitud.string("estado"))) {
			respuesta.set(CLAVE_ESTADO, VALOR_ESTADO_ENCURSO);
		}

		if (!solicitud.string("estado").isEmpty() && Objeto.setOf("F").contains(solicitud.string("estado"))) {
			// cargar VE con 3 para las 72 hs
			Integer cantidadDias = ConfigMB.integer("cantidad_dias_", 3) + 1;
			Integer diasPlus = 0;
			String momento = Fecha.formato(solicitud.string("dateDesc"), "dd/MM/yyyy", "yyyy-MM-dd");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar cal = Calendar.getInstance();
			try {
				cal.setTime(sdf.parse(momento));
				Integer count = 0;
				String momentoPlusDias = "";
				Integer countCorte = cantidadDias + 5;
				while (diasPlus < cantidadDias) {
					if (count >= countCorte) {
						respuesta.set(CLAVE_ESTADO, VALOR_ESTADO_ENCURSO);
						return respuesta;
					}
					count++;
					cal.setTime(sdf.parse(momento));
					cal.add(Calendar.DAY_OF_MONTH, count);
					momentoPlusDias = sdf.format(cal.getTime());
					if (Util.isDiaHabil(contexto, momentoPlusDias).booleanValue()) {
						diasPlus++;
					}
				}
				Calendar fechaActual = Calendar.getInstance();
				fechaActual.setTime(new Date());
				Date dFechaActual = fechaActual.getTime();
				Date fechaMomentoPlusDias = new SimpleDateFormat("yyyy-MM-dd").parse(momentoPlusDias);
				if (dFechaActual.compareTo(fechaMomentoPlusDias) >= 0) {
					// puede pedir nuevamente solicitud
					respuesta.set(CLAVE_ESTADO, "0");
				} else {
					// solicitud ya pedidda o en curso
					respuesta.set(CLAVE_ESTADO, "EN_CURSO");
				}
			} catch (Exception e) {
				// definir que opcion tomar en caso de error (frenar o dejar pedir)
				respuesta.set(CLAVE_ESTADO, "0");
			}
		}

		if (!solicitud.string("estado").isEmpty() && Objeto.setOf("R", "D").contains(solicitud.string("estado"))) {
			respuesta.set(CLAVE_ESTADO, "0");
			chequeado = true;
		}

		// fix porque se encontraro casos que se generan pero en los servicios no queda
		// marcada la solicitu en curso
		// optimizar
		if (respuesta.get("estado").equals("0") && !chequeado) {
			Integer cantidadDias = ConfigMB.integer("cantidad_dias_", 3) + 1;
			String momento = "";
			SqlRequestMB sqlRequest = SqlMB.request("ConsultaContador", "homebanking");
			sqlRequest.sql = "SELECT top 1 momento FROM [Homebanking].[dbo].[log_api_ventas] WITH (NOLOCK) WHERE idCobis = ?  AND servicio='finalizarSolicitudAumentoLimiteTC' AND momento > DATEADD(day, -7, GETDATE()) order by momento desc";
			sqlRequest.add(contexto.idCobis());
			Integer cantidad = SqlMB.response(sqlRequest).registros.size();
			Integer diasPlus = 0;
			if (cantidad >= 1) {
				momento = SqlMB.response(sqlRequest).registros.get(0).string("momento");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar cal = Calendar.getInstance();
				try {
					cal.setTime(sdf.parse(momento));
					Integer count = 0;
					String momentoPlusDias = "";
					Integer countCorte = cantidadDias + 5;
					while (diasPlus < cantidadDias) {
						if (count >= countCorte) {
							respuesta.set(CLAVE_ESTADO, VALOR_ESTADO_ENCURSO);
							return respuesta;
						}
						count++;
						cal.setTime(sdf.parse(momento));
						cal.add(Calendar.DAY_OF_MONTH, count);
						momentoPlusDias = sdf.format(cal.getTime());
						if (Util.isDiaHabil(contexto, momentoPlusDias).booleanValue()) {
							diasPlus++;
						}
					}
					Calendar fechaActual = Calendar.getInstance();
					fechaActual.setTime(new Date());
					Date dFechaActual = fechaActual.getTime();
					Date fechaMomentoPlusDias = new SimpleDateFormat("yyyy-MM-dd").parse(momentoPlusDias);
					if (dFechaActual.compareTo(fechaMomentoPlusDias) >= 0) {
						// puede pedir nuevamente solicitud
						respuesta.set(CLAVE_ESTADO, "0");
					} else {
						// solicitud ya pedidda o en curso
						respuesta.set(CLAVE_ESTADO, VALOR_ESTADO_ENCURSO);
					}
				} catch (Exception e) {
					// definir que opcion tomar en caso de error (frenar o dejar pedir)
					respuesta.set(CLAVE_ESTADO, VALOR_ESTADO_ENCURSO);
				}
			} else {
				respuesta.set(CLAVE_ESTADO, "0");
			}
		}
		return respuesta;
	}

	@SuppressWarnings("unchecked")
	private static Objeto getDetalleSolicitud(ContextoMB contexto) {
		Date fechaAlta = null;
		Objeto sol = new Objeto();
		try {
			RespuestaMB solicitudesEstado = MBProcesos.estadoSolicitudes(contexto);

			if (solicitudesEstado.hayError()) {
				return RespuestaMB.estado("ERROR_ESTADO_SOLICITUDES");
			}

			for (Objeto soli : (ArrayList<Objeto>) solicitudesEstado.get("solicitudes")) {
				if ("INCREMENTO LIMITE DE COMPRA TC".equalsIgnoreCase(soli.string("producto"))) {
					if (fechaAlta == null || fechaAlta.compareTo(soli.date("fechaDeAlta", "yyyy-MM-dd")) < 0) {
						fechaAlta = soli.date("fechaDeAlta", "yyyy-MM-dd");
						sol = soli;
					}
				}
			}
		} catch (Exception e) {
			return new Objeto();
		}
		return sol;
	}

	public static RespuestaMB confirmarSolicitudAumentoLimiteTC(ContextoMB contexto) {
		String idTarjetaCredito = contexto.parametros.string(PARAM_IDTARJETACREDITO, null);

		String idSolicitud = contexto.parametros.string(PARAM_IDSOLICITUD);
		String idSolicitudAumentoLimite = contexto.parametros.string("idSolicitudAumentoLimite");
		if (Objeto.anyEmpty(idTarjetaCredito, idSolicitud, idSolicitudAumentoLimite).booleanValue()) {
			return RespuestaMB.parametrosIncorrectos();
		}

		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			return RespuestaMB.error();
		}

		// Consultar solicitad aumento
		ApiResponseMB obtenerConsultaAumento = RestAumentoLimiteTC.obtenerConsultaAumento(contexto, idSolicitud, idSolicitudAumentoLimite);
		Solicitud.logApiVentas(contexto, idSolicitud, "obtenerConsultaAumento", obtenerConsultaAumento);
		if (hayErrores(obtenerConsultaAumento)) {
			throw new ApiVentaExceptionMB(obtenerConsultaAumento);
		}
		// Transformando información
		Objeto datosAumentoLimite = obtenerConsultaAumento.objetos(CLAVE_DATOS).get(0);
		SolicitudAumentoLimite solicitudAumentoLimite = (new Gson()).fromJson(datosAumentoLimite.toJson(), SolicitudAumentoLimite.class);
		ApiResponseMB responseAumentoLimiteTC = RestAumentoLimiteTC.actualizarAumentoLimiteTC(contexto, idSolicitud, tarjetaCredito.cuenta(), solicitudAumentoLimite);
		Solicitud.logApiVentas(contexto, idSolicitud, "actualizarAumentoLimiteTC", responseAumentoLimiteTC);
		if (hayErrores(responseAumentoLimiteTC)) {
			if (responseAumentoLimiteTC.objetos(VALOR_ERRORES).get(0).string("MensajeDesarrollador").contains("FAULTCODE:40003 FAULTMSJ:Producto bancario deshabilitado")) {
				return new RespuestaMB().setEstado("ERROR_CORRIENDO_BATCH");
			}
			return new RespuestaMB().setEstado(CLAVE_ERROR).set(VALOR_ERROR, mensajeError(responseAumentoLimiteTC));
		}

		ApiResponseMB responseFinalizar = RestAumentoLimiteTC.finalizarSolicitud(contexto, idSolicitud);
		if (hayErrores(responseFinalizar)) {
			String estado = estadoErrorFinalizaSolicitud(contexto, idSolicitud, responseFinalizar);
			return new RespuestaMB().setEstado(estado).set(VALOR_ERROR, mensajeError(responseFinalizar));
		}
		insertarLogApiVentas(contexto, idSolicitud, FINALIZAR_SERVICIO_AUMENTO, "OK", "Finalizado", "200");
		return RespuestaMB.exito();
	}

	private static String mensajeError(ApiResponseMB error) {
		return !error.objetos(VALOR_ERRORES).isEmpty() ? error.objetos(VALOR_ERRORES).get(0).string(CLAVE_MENSAJE_CLIENTE) : null;
	}

	private static String estadoErrorFinalizaSolicitud(ContextoMB contexto, String idSolicitud, ApiResponseMB responseFinalizar) {
		Solicitud.logApiVentas(contexto, idSolicitud, FINALIZAR_SERVICIO_AUMENTO, responseFinalizar);
		if (responseFinalizar.objetos(VALOR_ERRORES).get(0).string("Codigo").equals("1831609")) {
			return "IR_A_SUCURSAL";
		} else if (responseFinalizar.objetos(VALOR_ERRORES).get(0).string("Codigo").equals("1831602")) {
			return "EN_PROCESO_ACTUALIZACION";
		} else {
			return CLAVE_ERROR;
		}
	}

	public static void insertarLogApiVentas(ContextoMB contexto, String numeroSolicitud, String servicio, String mensajeCliente, String mensajeDesarrollador, String http) {
		try {
			String sql = "";
			sql += " INSERT INTO [Homebanking].[dbo].[log_api_ventas] (momento,idCobis,numeroDocumento,numeroSolicitud,servicio,resolucionMotor,explicacionMotor,mensajeCliente,mensajeDesarrollador,canal)";
			sql += " VALUES (GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			SqlRequestMB sqlRequest = SqlMB.request("InsertLogApiVentas", "homebanking");
			sqlRequest.sql = sql;
			sqlRequest.parametros.add(Texto.substring(contexto.idCobis(), 250));
			sqlRequest.parametros.add(Texto.substring(contexto.persona().numeroDocumento(), 250));
			sqlRequest.parametros.add(Texto.substring(numeroSolicitud, 250));
			sqlRequest.parametros.add(Texto.substring(servicio, 250));
			sqlRequest.parametros.add(null);
			sqlRequest.parametros.add(null);
			sqlRequest.parametros.add(Texto.substring(http + " - " + mensajeCliente, 990));
			sqlRequest.parametros.add(Texto.substring(mensajeDesarrollador, 990));
			sqlRequest.parametros.add("HB");
			SqlMB.response(sqlRequest);
		} catch (Exception e) {
			System.err.println("LA EXCEPCION FUE: " + e);
		}
	}

	public static RespuestaMB desistirSolicitudAumentoLimiteTC(ContextoMB contexto) {
		String idTarjetaCredito = contexto.parametros.string(PARAM_IDTARJETACREDITO, null);
		String idSolicitud = contexto.parametros.string(PARAM_IDSOLICITUD, null);
		if (Objeto.anyEmpty(idTarjetaCredito, idSolicitud).booleanValue()) {
			return RespuestaMB.parametrosIncorrectos();
		}
		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			return RespuestaMB.error();
		}
		ApiResponseMB response = RestAumentoLimiteTC.desistirSolicitud(contexto, idSolicitud);
		Solicitud.logApiVentas(contexto, idSolicitud, "desistirSolicitudAumentoLimite", response);
		if (hayErrores(response)) {
			String estado = CLAVE_ERROR;
			return new RespuestaMB().setEstado(estado).set(VALOR_ERROR, mensajeError(response));
		}
		return RespuestaMB.exito();
	}

	public static RespuestaMB notificacionesAumetoLimite(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		try {

			ArrayList<Objeto> notificacionesAL = new ArrayList<Objeto>();
			Objeto sol = getDetalleSolicitud(contexto);

			if (sol.string("producto").isEmpty()) {
				return respuesta;
			}

			Objeto soliNotif = Util.getOrUpdateNotificacionEstadoProducto(contexto, sol);
			if (!soliNotif.bool("borrar")) {
				Objeto notif = new Objeto();
				notif.set("type", "aumento-limite");
				notif.set("read", soliNotif.bool("leido"));
				notif.set("dateDesc", sol.get("dateDesc"));
				notif.set("numeroCaso", sol.get("idSolicitud"));
				switch (sol.string("estado")) {
				case "F":
					notif.set("title", Texto.htmlToText(ConfigMB.string("aumento_title_F")));
					notif.set("parte1", ConfigMB.string("aumento_parte1_F"));
					notif.set("estado", "LIMITE_DISPONIBLE");
					notificacionesAL.add(notif);
					contador(contexto, "AUMENTOTC_NOTIF_F");
					break;
				// por MVP solo ruta verde
//				case "RECLAMODOC":
//					notif.set("title", Config.string("aumento_title_RECLAMODOC"));
//					notif.set("parte1", Config.string("aumento_parte1_RECLAMODOC"));
//					notif.set("estado", "ARCHIVO_FALTANTE");
//					notif.set("datos", sol.get("reclamos"));
//					notificacionesAL.add(notif);
//					contador(contexto, "AUMENTOTC_NOTIF_RECLAMODOC");
//					break;
//				case "R":
//					notif.set("title", Config.string("aumento_title_R"));
//					notif.set("parte1", Config.string("aumento_parte1_R"));
//					notif.set("estado", "MAXIMO_PERMITIDO");
//					notificacionesAL.add(notif);
//					contador(contexto, "AUMENTOTC_NOTIF_R");
//					break;
//				case "D":
//					notif.set("title", Config.string("aumento_title_D"));
//					notif.set("parte1", Config.string("aumento_parte1_D"));
//					notif.set("estado", "RESUBIR_DOCUMENTACION");
//					notificacionesAL.add(notif);
//					contador(contexto, "AUMENTOTC_NOTIF_D");
//					break;
				default:
					break;
				}

				respuesta.set("notificacionesAL", notificacionesAL);
			}

		} catch (Exception e) {
			return RespuestaMB.error();
		}

		return respuesta;
	}

	private static void contador(ContextoMB contexto, String nemonico) {
		contexto.parametros.set("nemonico", nemonico);
		Util.contador(contexto);
	}

	private static Boolean fueraHorario() {
		try {
			// Horario 22 a 7 hs no disponible
			// es decir que de 7 a 22 esta disponible
			if (Util.isfueraHorario(ConfigMB.integer("aumentoLimite_horaInicio", 7), ConfigMB.integer("aumentoLimite_horaFin", 22))) {
				return true;
			}

		} catch (Exception e) {
			//
		}
		return false;
	}

}
