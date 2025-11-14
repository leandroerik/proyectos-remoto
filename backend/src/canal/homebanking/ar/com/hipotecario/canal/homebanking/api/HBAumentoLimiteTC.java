package ar.com.hipotecario.canal.homebanking.api;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaCredito;
import ar.com.hipotecario.canal.homebanking.servicio.RestAumentoLimiteTC;
import ar.com.hipotecario.canal.homebanking.servicio.RestVenta;
import ar.com.hipotecario.canal.homebanking.ventas.ApiVentaException;
import ar.com.hipotecario.canal.homebanking.ventas.ResolucionMotor;
import ar.com.hipotecario.canal.homebanking.ventas.Solicitud;
import ar.com.hipotecario.canal.homebanking.ventas.SolicitudAumentoLimite;

public class HBAumentoLimiteTC {
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

	private HBAumentoLimiteTC() {
	}

	public static Respuesta crearSolicitudAumentoLimiteTC(ContextoHB contexto) {
		String idTarjetaCredito = contexto.parametros.string(PARAM_IDTARJETACREDITO, null);
		if (Objeto.anyEmpty(idTarjetaCredito).booleanValue()) {
			return Respuesta.parametrosIncorrectos();
		}

		if (contexto.persona().esEmpleado() && !HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoAumentoLimiteEmpleados")) {
			return Respuesta.error();
		}

		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			return Respuesta.error();
		}

		// Generar Solicitud
		ApiResponse generarSolicitud = RestAumentoLimiteTC.generarSolicitud(contexto);
		if (hayErrores(generarSolicitud)) {
			new Futuro<>(() -> Solicitud.logApiVentas(contexto, "-", "generarSolicitud", generarSolicitud));
			return new Respuesta().setEstado(CLAVE_ERROR).set(VALOR_ERROR, mensajeError(generarSolicitud));
		}
		String idSolicitud = generarSolicitud.objetos(CLAVE_DATOS).get(0).string(PARAM_DATO_IDSOLICITUD);
		new Futuro<>(() -> Solicitud.logApiVentas(contexto, idSolicitud, "generarSolicitud", generarSolicitud));

		// Generar Integrante
		ApiResponse generarIntegrante = RestAumentoLimiteTC.generarIntegrante(contexto, idSolicitud);
		new Futuro<>(() -> Solicitud.logApiVentas(contexto, idSolicitud, "generarIntegrante", generarIntegrante));
		if (hayErrores(generarIntegrante)) {
			return new Respuesta().setEstado(CLAVE_ERROR).set(VALOR_ERROR, mensajeError(generarIntegrante));
		}

		// Generar solicitad aumento
		ApiResponse generarAumentoLimiteTC = RestAumentoLimiteTC.generarAumentoLimiteTC(contexto, idSolicitud, tarjetaCredito.cuenta());
		new Futuro<>(() -> Solicitud.logApiVentas(contexto, idSolicitud, "generarAumentoLimiteTC", generarAumentoLimiteTC));
		if (hayErrores(generarAumentoLimiteTC)) {
			return new Respuesta().setEstado(CLAVE_ERROR).set(VALOR_ERROR, mensajeError(generarAumentoLimiteTC));
		}

		// Transformando información
		SolicitudAumentoLimite solicitudAumentoLimite = transformarInformacionAumento(generarAumentoLimiteTC);

		// Ejecutar Motor
		ApiResponse responseMotor = ejecutarMotorResponse(contexto, idSolicitud, false, contexto.esPlanSueldo());
		if (hayErrores(responseMotor)) {
			return Respuesta.error().set(VALOR_ERROR, mensajeError(responseMotor));
		}

		String resolucionId = retornarDatoMotor(responseMotor, "ResolucionId");
		String derivarA = retornarDatoMotor(responseMotor, "DerivarA");
		String explicacion = retornarDatoMotor(responseMotor, "Explicacion");
		String esquemaEvaluacion = retornarDatoMotor(responseMotor, "EsquemaEvaluacion");

		if (resolucionId.equals("AV") && esquemaEvaluacion.equals("CON_DATO_SIN_DOC_PLAN_SUELDO")) {
			try {
				ApiResponse docResponse = RestAumentoLimiteTC.documentacionObligatoriaSolicitud(contexto, idSolicitud);
				if (docResponse.get("Datos") != null) {
					for (Objeto doc : docResponse.objetos("Datos").get(0).objetos("Documentacion")) {
						if (!doc.string("Id").equals("1100")) {
							new Futuro<>(() -> contador(contexto, "SUELDO_" + resolucionId + "_DOC"));
							return Respuesta.estado(ESTADO_AMARILLO).set(PARAM_IDSOLICITUD, idSolicitud);
						}
					}
					new Futuro<>(() -> contador(contexto, "SUELDO_" + resolucionId + "_SIN_DOC"));
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}

		if ((resolucionId.equals("AV") || resolucionId.equals("AA")) && esquemaEvaluacion.contains("CON_DOC")) {
			ApiResponse obtenerConsultaAumento = RestAumentoLimiteTC.obtenerConsultaAumento(contexto, idSolicitud, solicitudAumentoLimite.Id);
			solicitudAumentoLimite = transformarInformacionAumento(obtenerConsultaAumento);
			new Futuro<>(() -> contador(contexto, resolucionId + "_CON_DOC"));
			SolicitudAumentoLimite finalSolicitudAumentoLimite2 = solicitudAumentoLimite;
			new Futuro<>(() -> contador(contexto, resolucionId + "_CON_DOC" + "_MONTOF_" + finalSolicitudAumentoLimite2.MontoOfrecido));
			return Respuesta.estado("CON_DOC").set(CLAVE_DATOS, formatearSolicitudAumento(solicitudAumentoLimite, idSolicitud));
		}

		if (!resolucionId.equals("AV")) {
			String estado = CLAVE_ERROR;
			estado = resolucionId.equals("AA") ? ESTADO_APROBADO_AMARILLO : estado;
			estado = resolucionId.equals("CT") ? ESTADO_AMARILLO : estado;
			estado = resolucionId.equals("RE") ? ESTADO_ROJO : estado;
			if (estado.equals(ESTADO_APROBADO_AMARILLO)) {
				ApiResponse obtenerConsultaAumento = RestAumentoLimiteTC.obtenerConsultaAumento(contexto, idSolicitud, solicitudAumentoLimite.Id);
				solicitudAumentoLimite = transformarInformacionAumento(obtenerConsultaAumento);
				new Futuro<>(() -> contador(contexto, resolucionId + "_CON_DOC"));
				SolicitudAumentoLimite finalSolicitudAumentoLimite1 = solicitudAumentoLimite;
				new Futuro<>(() -> contador(contexto, resolucionId + "_CON_DOC" + "_MONTOF_" + finalSolicitudAumentoLimite1.MontoOfrecido));
				return Respuesta.estado(ESTADO_APROBADO_AMARILLO).set(CLAVE_DATOS, formatearSolicitudAumento(solicitudAumentoLimite, idSolicitud));
			}
			// CT y RE sale por aca
			new Futuro<>(() -> contador(contexto, resolucionId));

			// TODO: guardar motivo de rechazo o controlar
			try {
				new Futuro<>(() -> Util.insertarLogMotor(contexto, idSolicitud, resolucionId, explicacion, "INCREMENTO LIMITE DE COMPRA TC", esquemaEvaluacion));
			} catch (Exception e) {
				// TODO: handle exception
			}

			return new Respuesta().setEstado(estado).set(VALOR_ERROR, explicacion).set(PARAM_IDSOLICITUD, idSolicitud);
		}

		if (derivarA != null && derivarA.equals("S")) {
			new Futuro<>(() -> contador(contexto, resolucionId + "_DERIVAR_" + derivarA));
			return Respuesta.estado(ESTADO_AMARILLO).set(PARAM_IDSOLICITUD, idSolicitud);
		}

		if (resolucionId.equals("AV")) {
			if (!esquemaEvaluacion.equals("CON_DATO_SIN_DOC_PLAN_SUELDO")) {
				new Futuro<>(() -> contador(contexto, resolucionId));
			}
		}

		// Consultar solicitad aumento
		ApiResponse obtenerConsultaAumento = RestAumentoLimiteTC.obtenerConsultaAumento(contexto, idSolicitud, solicitudAumentoLimite.Id);
		new Futuro<>(() -> Solicitud.logApiVentas(contexto, idSolicitud, "obtenerConsultaAumento", obtenerConsultaAumento));
		if (hayErrores(obtenerConsultaAumento)) {
			return new Respuesta().setEstado(CLAVE_ERROR).set(VALOR_ERROR, mensajeError(obtenerConsultaAumento));
		}

		// Transformando información
		solicitudAumentoLimite = transformarInformacionAumento(obtenerConsultaAumento);
		Respuesta respuesta = new Respuesta();
		respuesta.set("datos", formatearSolicitudAumento(solicitudAumentoLimite, idSolicitud));

		SolicitudAumentoLimite finalSolicitudAumentoLimite = solicitudAumentoLimite;
		new Futuro<>(() -> contador(contexto, resolucionId + "_MONTOF_" + finalSolicitudAumentoLimite.MontoOfrecido));

		return respuesta;
	}

	private static String retornarDatoMotor(ApiResponse respuestaMotor, String parametro) {
		return respuestaMotor.objetos(CLAVE_DATOS).get(0).string(parametro);
	}

	private static SolicitudAumentoLimite transformarInformacionAumento(Objeto datos) {
		Objeto datosAumentoLimite = datos.objetos(CLAVE_DATOS).get(0);
		return (new Gson()).fromJson(datosAumentoLimite.toJson(), SolicitudAumentoLimite.class);
	}

	private static Object formatearSolicitudAumento(SolicitudAumentoLimite solicitudAumentoLimite, String idSolicitud) {
		Objeto item = new Objeto();
		item.set(PARAM_IDSOLICITUD, idSolicitud);
		item.set("idSolicitudAumentoLimite", solicitudAumentoLimite.Id);
		item.set("montoAceptado", solicitudAumentoLimite.MontoAceptado);
		item.set("montoOfrecido", solicitudAumentoLimite.MontoOfrecido);
		item.set("cuenta", solicitudAumentoLimite.cuenta);
		item.set("monto", solicitudAumentoLimite.monto);
		item.set("acuerdo", solicitudAumentoLimite.Acuerdo);
		item.set("advertencias", solicitudAumentoLimite.Advertencias);
		item.set("rechazadoMotor", solicitudAumentoLimite.RechazadoMotor);
		return item;
	}

	public static Respuesta confirmarSolicitudAumentoLimiteTC(ContextoHB contexto) {
		String idTarjetaCredito = contexto.parametros.string(PARAM_IDTARJETACREDITO, null);

		String idSolicitud = contexto.parametros.string(PARAM_IDSOLICITUD);
		String idSolicitudAumentoLimite = contexto.parametros.string("idSolicitudAumentoLimite");
		if (Objeto.anyEmpty(idTarjetaCredito, idSolicitud, idSolicitudAumentoLimite).booleanValue()) {
			return Respuesta.parametrosIncorrectos();
		}

		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			return Respuesta.error();
		}

		// Consultar solicitad aumento
		ApiResponse obtenerConsultaAumento = RestAumentoLimiteTC.obtenerConsultaAumento(contexto, idSolicitud, idSolicitudAumentoLimite);
		Solicitud.logApiVentas(contexto, idSolicitud, "obtenerConsultaAumento", obtenerConsultaAumento);
		if (hayErrores(obtenerConsultaAumento)) {
			throw new ApiVentaException(obtenerConsultaAumento);
		}
		// Transformando información
		Objeto datosAumentoLimite = obtenerConsultaAumento.objetos(CLAVE_DATOS).get(0);
		SolicitudAumentoLimite solicitudAumentoLimite = (new Gson()).fromJson(datosAumentoLimite.toJson(), SolicitudAumentoLimite.class);
		ApiResponse responseAumentoLimiteTC = RestAumentoLimiteTC.actualizarAumentoLimiteTC(contexto, idSolicitud, tarjetaCredito.cuenta(), solicitudAumentoLimite);
		Solicitud.logApiVentas(contexto, idSolicitud, "actualizarAumentoLimiteTC", responseAumentoLimiteTC);
		if (hayErrores(responseAumentoLimiteTC)) {
			if (responseAumentoLimiteTC.objetos(VALOR_ERRORES).get(0).string("MensajeDesarrollador").contains("FAULTCODE:40003 FAULTMSJ:Producto bancario deshabilitado")) {
				return new Respuesta().setEstado("ERROR_CORRIENDO_BATCH");
			}
			return new Respuesta().setEstado(CLAVE_ERROR).set(VALOR_ERROR, mensajeError(responseAumentoLimiteTC));
		}

		ApiResponse responseFinalizar = RestAumentoLimiteTC.finalizarSolicitud(contexto, idSolicitud);
		if (hayErrores(responseFinalizar)) {
			String estado = estadoErrorFinalizaSolicitud(contexto, idSolicitud, responseFinalizar);
			return new Respuesta().setEstado(estado).set(VALOR_ERROR, mensajeError(responseFinalizar));
		}
		insertarLogApiVentas(contexto, idSolicitud, FINALIZAR_SERVICIO_AUMENTO, "OK", "Finalizado", "200");
		return Respuesta.exito();
	}

	public static Respuesta desistirSolicitudAumentoLimiteTC(ContextoHB contexto) {
		String idTarjetaCredito = contexto.parametros.string(PARAM_IDTARJETACREDITO, null);
		String idSolicitud = contexto.parametros.string(PARAM_IDSOLICITUD, null);
		if (Objeto.anyEmpty(idTarjetaCredito, idSolicitud).booleanValue()) {
			return Respuesta.parametrosIncorrectos();
		}
		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			return Respuesta.error();
		}
		ApiResponse response = RestVenta.desistirSolicitud(contexto, idSolicitud);
		Solicitud.logApiVentas(contexto, idSolicitud, "desistirSolicitudAumentoLimite", response);
		if (hayErrores(response)) {
			String estado = CLAVE_ERROR;
			return new Respuesta().setEstado(estado).set(VALOR_ERROR, mensajeError(response));
		}
		return Respuesta.exito();
	}

	public static Respuesta consultarSolicitudesPorId(ContextoHB contexto) {
		String idSolicitud = contexto.parametros.string(PARAM_IDSOLICITUD);
		Respuesta respuesta = new Respuesta();
		if (Objeto.anyEmpty(idSolicitud).booleanValue()) {
			return Respuesta.parametrosIncorrectos();
		}
		Objeto productos = new Objeto();
		ApiResponse solicitud = RestAumentoLimiteTC.consultarSolicitud(contexto, idSolicitud);
		if (hayErrores(solicitud)) {
			Solicitud.logApiVentas(contexto, "-", "consultarSolicitud", solicitud);
			return new Respuesta().setEstado(CLAVE_ERROR).set(VALOR_ERROR, !solicitud.objetos(VALOR_ERRORES).isEmpty() ? solicitud.objetos(VALOR_ERRORES).get(0).string(CLAVE_MENSAJE_CLIENTE) : null);
		}
		List<Objeto> objProductos = solicitud.objetos(CLAVE_DATOS).get(0).objetos("Productos").stream().filter(prod -> prod.string("tipoProducto").equals("17")).collect(Collectors.toList());
		if (objProductos.isEmpty()) {
			return new Respuesta().setEstado(CLAVE_ERROR).set(VALOR_ERROR, "No existe producto solicitado.");
		}
		for (Objeto producto : objProductos) {
			Objeto item = new Objeto();
			item.set("Oficina", producto.string("Oficina"));
			item.set("Validado", producto.string("Validado"));
			item.set("Oficial", producto.string("Oficial"));
			item.set("RechazadoMotor", producto.string("RechazadoMotor"));
			item.set("Id", producto.string("Id"));
			ApiResponse obtenerConsultaAumento = RestAumentoLimiteTC.obtenerConsultaAumento(contexto, idSolicitud, producto.string("Id"));
			Objeto datosAumentoLimite = obtenerConsultaAumento.objetos(CLAVE_DATOS).get(0);
			if (!datosAumentoLimite.objetos().isEmpty()) {
				item.set("montoAceptado", datosAumentoLimite.string("MontoAceptado"));
				item.set("montoOfrecido", datosAumentoLimite.string("MontoOfrecido"));
			}
			productos.add("productos", item);
		}
		respuesta.set(CLAVE_DATOS, productos);
		return respuesta;
	}

	public static Respuesta consultarSolicitudades(ContextoHB contexto) {
		Long cantidadDias = contexto.parametros.longer("cantidadDias", ConfigHB.longer("solicitud_dias_vigente", 30L));
		Respuesta respuesta = new Respuesta();

		ApiResponse solicitudResponse = RestVenta.consultarSolicitudes(contexto, cantidadDias);
		if (hayErrores(solicitudResponse)) {
			Solicitud.logApiVentas(contexto, "-", "consultarSolicitud", solicitudResponse);
			return new Respuesta().setEstado(CLAVE_ERROR).set(VALOR_ERROR, mensajeError(solicitudResponse));
		}
		List<Objeto> objProductos = new ArrayList<>();
		for (Objeto objetoDato : solicitudResponse.objetos(CLAVE_DATOS)) {
			for (Objeto objetoProducto : objetoDato.objetos("Productos").stream().filter(prod -> prod.string("tipoProducto").equals("17")).collect(Collectors.toList())) {
				Objeto item = new Objeto();
				item.set("FechaAlta", objetoDato.string("FechaAlta"));
				item.set("Producto", objetoProducto.string("Producto"));
				Solicitud solicitud = (new Gson()).fromJson(objetoDato.toJson(), Solicitud.class);
				ResolucionMotor resolucion = solicitud.consultarMotor(contexto, false);
				String resolucionId = resolucion.ResolucionId;
				String derivarA = resolucion.DerivarA == null ? "" : resolucion.DerivarA;
				String esquemaEvaluacion = resolucion.EsquemaEvaluacion;
				if (resolucionId != null) {
					if ((resolucionId.equals("AV") || resolucionId.equals("AA")) && esquemaEvaluacion.contains("CON_DOC")) {
						item.set(CLAVE_ESTADO_MOTOR, "CON_DOC");
					}
					if (!resolucionId.equals("AV")) {
						String estado = "";
						estado = resolucionId.equals("AA") ? ESTADO_APROBADO_AMARILLO : estado;
						estado = resolucionId.equals("CT") ? ESTADO_AMARILLO : estado;
						estado = resolucionId.equals("RE") ? ESTADO_ROJO : estado;
						item.set(CLAVE_ESTADO_MOTOR, estado);
					} else {
						item.set(CLAVE_ESTADO_MOTOR, "VERDE");
					}
					if (derivarA.equals("S")) {
						item.set(CLAVE_ESTADO_MOTOR, ESTADO_AMARILLO);

					}
				}
				item.set("Id", objetoProducto.string("Id"));
				objProductos.add(item);
				item.set("IdSolicitud", objetoDato.string("IdSolicitud"));
			}
		}
		if (objProductos.isEmpty()) {
			return new Respuesta().setEstado("-1").set(VALOR_ERROR, "No existe solicitudes.");
		}
		respuesta.set(CLAVE_DATOS, objProductos);
		return respuesta;
	}

	public static Respuesta isAumentoLimiteTCSolicitado(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();

		if (Util.fueraHorarioProcesosBatch()) {
			return respuesta.set(CLAVE_ESTADO, "FUERA_HORARIO");
		}

		Objeto solicitud = getDetalleSolicitud(contexto);
		Boolean chequeado = false;

		if (!solicitud.string("estado").isEmpty() && !Objeto.setOf("F", "R", "D", "RECLAMODOC").contains(solicitud.string("estado"))) {
			respuesta.set(CLAVE_ESTADO, VALOR_ESTADO_ENCURSO);
		}

		if (!solicitud.string("estado").isEmpty() && Objeto.setOf("F").contains(solicitud.string("estado"))) {
			// cargar VE con 3 para las 72 hs
			Integer cantidadDias = ConfigHB.integer("cantidad_dias_", 3) + 1;
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
					respuesta.set(CLAVE_ESTADO, "EN_CURSO_F");
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
			Integer cantidadDias = ConfigHB.integer("cantidad_dias_", 3) + 1;
			String momento = "";
			SqlRequest sqlRequest = Sql.request("ConsultaContador", "homebanking");
			sqlRequest.sql = "SELECT top 1 momento FROM [Homebanking].[dbo].[log_api_ventas] WITH (NOLOCK) WHERE idCobis = ?  AND servicio='finalizarSolicitudAumentoLimiteTC' AND momento > DATEADD(day, -7, GETDATE()) order by momento desc";
			sqlRequest.add(contexto.idCobis());
			Integer cantidad = Sql.response(sqlRequest).registros.size();
			Integer diasPlus = 0;
			if (cantidad >= 1) {
				momento = Sql.response(sqlRequest).registros.get(0).string("momento");
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

	public static Respuesta actualizarSolicitudAumentoLimiteTC(ContextoHB contexto) {
		String idSolicitud = contexto.parametros.string(PARAM_IDSOLICITUD, null);
		String idSolicitudAumento = contexto.parametros.string("idSolicitudAumento", null);
		String idTarjetaCredito = contexto.parametros.string(PARAM_IDTARJETACREDITO, null);
		if (Objeto.anyEmpty(idSolicitud, idSolicitudAumento, idTarjetaCredito).booleanValue()) {
			return Respuesta.parametrosIncorrectos();
		}

		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjetaCredito);
		if (tarjetaCredito == null) {
			return Respuesta.error();
		}
		// Actualizar Solicitud
		ApiResponse actualizarCanalSolicitud = RestAumentoLimiteTC.actualizarCanalSolicitud(contexto, idSolicitud);
		if (hayErrores(actualizarCanalSolicitud)) {
			Solicitud.logApiVentas(contexto, "-", "actualizarSolicitudAumentoLimiteTC", actualizarCanalSolicitud);
			return new Respuesta().setEstado(CLAVE_ERROR).set(VALOR_ERROR, mensajeError(actualizarCanalSolicitud));
		}
		Solicitud.logApiVentas(contexto, idSolicitud, "actualizarSolicitudAumentoLimiteTC", actualizarCanalSolicitud);

		ApiResponse obtenerConsultaAumento = RestAumentoLimiteTC.obtenerConsultaAumento(contexto, idSolicitud, idSolicitudAumento);
		SolicitudAumentoLimite solicitudAumentoLimite = transformarInformacionAumento(obtenerConsultaAumento);

		ApiResponse responseAumentoLimiteTC = RestAumentoLimiteTC.actualizarAumentoLimiteTC(contexto, idSolicitud, tarjetaCredito.cuenta(), solicitudAumentoLimite);
		Solicitud.logApiVentas(contexto, idSolicitud, "actualizarAumentoLimiteTC", responseAumentoLimiteTC);
		if (hayErrores(responseAumentoLimiteTC)) {
			if (responseAumentoLimiteTC.objetos(VALOR_ERRORES).get(0).string("MensajeDesarrollador").contains("FAULTCODE:40003 FAULTMSJ:Producto bancario deshabilitado")) {
				return new Respuesta().setEstado("ERROR_CORRIENDO_BATCH");
			}
			return new Respuesta().setEstado(CLAVE_ERROR).set(VALOR_ERROR, mensajeError(responseAumentoLimiteTC));
		}

		ApiResponse responseFinalizar = RestAumentoLimiteTC.finalizarSolicitud(contexto, idSolicitud);
		if (hayErrores(responseFinalizar)) {
			String estado = estadoErrorFinalizaSolicitud(contexto, idSolicitud, responseFinalizar);
			return new Respuesta().setEstado(estado).set(VALOR_ERROR, mensajeError(responseFinalizar));
		}
		insertarLogApiVentas(contexto, idSolicitud, FINALIZAR_SERVICIO_AUMENTO, "OK", "Finalizado", "200");
		return Respuesta.exito();

	}

	private static String estadoErrorFinalizaSolicitud(ContextoHB contexto, String idSolicitud, ApiResponse responseFinalizar) {
		Solicitud.logApiVentas(contexto, idSolicitud, FINALIZAR_SERVICIO_AUMENTO, responseFinalizar);
		if (responseFinalizar.objetos(VALOR_ERRORES).get(0).string("Codigo").equals("1831609")) {
			return "IR_A_SUCURSAL";
		} else if (responseFinalizar.objetos(VALOR_ERRORES).get(0).string("Codigo").equals("1831602")) {
			return "EN_PROCESO_ACTUALIZACION";
		} else {
			return CLAVE_ERROR;
		}

	}

	private static ApiResponse ejecutarMotorResponse(ContextoHB contexto, String idSolicitud, boolean flagSolicitaComprobarIngresos, Boolean esPlanSueldo) {
		ApiResponse responseMotor = RestAumentoLimiteTC.ejecutarMotorResponse(contexto, idSolicitud, flagSolicitaComprobarIngresos, esPlanSueldo);
		Solicitud.logMotor(contexto, idSolicitud, responseMotor);
		return responseMotor;
	}

	private static boolean hayErrores(ApiResponse error) {
		return error.hayError() || !error.objetos(VALOR_ERRORES).isEmpty();
	}

	private static String mensajeError(ApiResponse error) {
		return !error.objetos(VALOR_ERRORES).isEmpty() ? error.objetos(VALOR_ERRORES).get(0).string(CLAVE_MENSAJE_CLIENTE) : null;
	}

	public static void insertarLogApiVentas(ContextoHB contexto, String numeroSolicitud, String servicio, String mensajeCliente, String mensajeDesarrollador, String http) {
		try {
			String sql = "";
			sql += " INSERT INTO [Homebanking].[dbo].[log_api_ventas] (momento,idCobis,numeroDocumento,numeroSolicitud,servicio,resolucionMotor,explicacionMotor,mensajeCliente,mensajeDesarrollador,canal)";
			sql += " VALUES (GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";
			SqlRequest sqlRequest = Sql.request("InsertLogApiVentas", "homebanking");
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
			Sql.response(sqlRequest);
		} catch (Exception e) {
			System.err.println("LA EXCEPCION FUE: " + e);
		}
	}

	public static Respuesta notificacionesAumetoLimite(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		try {

			if (contexto.persona().esEmpleado() && !HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoAumentoLimiteEmpleados")) {
				return respuesta;
			}

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
					notif.set("title", ConfigHB.string("aumento_title_F"));
					notif.set("parte1", ConfigHB.string("aumento_parte1_F"));
					notif.set("estado", "LIMITE_DISPONIBLE");
					notificacionesAL.add(notif);
					contador(contexto, "AUMENTOTC_NOTIF_F");
					break;
				case "RECLAMODOC":
					if (contexto.persona().esEmpleado()) {
						notif.set("title", ConfigHB.string("aumento_title_empleado_RECLAMODOC"));
						notif.set("parte1", ConfigHB.string("aumento_parte1_empleado_RECLAMODOC"));
					} else {
						notif.set("title", ConfigHB.string("aumento_title_RECLAMODOC"));
						notif.set("parte1", ConfigHB.string("aumento_parte1_RECLAMODOC"));
					}
					notif.set("estado", "ARCHIVO_FALTANTE");
					notif.set("datos", sol.get("reclamos"));
					notificacionesAL.add(notif);
					contador(contexto, "AUMENTOTC_NOTIF_RECLAMODOC");
					break;
				case "R":
					if (contexto.persona().esEmpleado()) {
						notif.set("title", ConfigHB.string("aumento_title_empleado_R"));
						notif.set("parte1", ConfigHB.string("aumento_parte1_empleado_R"));
					} else {
						notif.set("title", ConfigHB.string("aumento_title_R"));
						notif.set("parte1", ConfigHB.string("aumento_parte1_R"));
					}
					notif.set("estado", "MAXIMO_PERMITIDO");
					notificacionesAL.add(notif);
					contador(contexto, "AUMENTOTC_NOTIF_R");
					break;
				case "D":
					notif.set("title", ConfigHB.string("aumento_title_D"));
					notif.set("parte1", ConfigHB.string("aumento_parte1_D"));
					notif.set("estado", "RESUBIR_DOCUMENTACION");
					notificacionesAL.add(notif);
					contador(contexto, "AUMENTOTC_NOTIF_D");
					break;
				default:
					break;
				}

				for (Objeto item : notificacionesAL) {
					respuesta.add("notificacionesAL", item);
				}
				// respuesta.set("notificacionesAL", notificacionesAL);
			}

		} catch (Exception e) {
			return Respuesta.error();
		}

		return respuesta;
	}

	private static Boolean contador(ContextoHB contexto, String nemonico) {
		contexto.parametros.set("nemonico", nemonico);
		Util.contador(contexto);
		return true;
	}

	@SuppressWarnings("unchecked")
	private static Objeto getDetalleSolicitud(ContextoHB contexto) {
		Date fechaAlta = null;
		Objeto sol = new Objeto();
		try {
			Respuesta solicitudesEstado = HBProcesos.estadoSolicitudes(contexto);

			if (solicitudesEstado.hayError()) {
				return Respuesta.estado("ERROR_ESTADO_SOLICITUDES");
			}
			List<Objeto> solicitudes = new ArrayList<Objeto>();
			try {
				solicitudes = (List<Objeto>) solicitudesEstado.get("solicitudes");
			} catch (Exception e) {
				solicitudes = solicitudesEstado.objetos("solicitudes");
			}

			if (solicitudes.isEmpty()) {
				solicitudes = (List<Objeto>) solicitudesEstado.objetos("solicitudes");
			}

			for (Objeto soli : solicitudes) {
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

}
