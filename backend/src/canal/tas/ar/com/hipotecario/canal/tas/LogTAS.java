package ar.com.hipotecario.canal.tas;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Texto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;

import java.sql.SQLException;

public class LogTAS extends Modulo {

	public static void loguearRequest(ContextoTAS contexto, ApiRequest request, String evento) {
		try{
			Objeto datosLog = new Objeto();
			datosLog.set("url", request.httpRequest().url);
			datosLog.set("path", request.httpRequest().paths);
			datosLog.set("querys", request.httpRequest().querys);
			datosLog.set("body", request.httpRequest().body);			
			LogTAS.evento(contexto, evento, datosLog.toSimpleJson());
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void loguearResponse(ContextoTAS contexto, ApiResponse response, String evento){
		try {
			Objeto datosLog = new Objeto();
			datosLog.set("url", response.request.fullUrl());
			datosLog.set("codigo_http", response.codigoHttp);
			datosLog.set("response", response);		
			LogTAS.evento(contexto, evento, datosLog.toSimpleJson());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public static void evento(ContextoTAS contexto, String evento) {
		evento(contexto, evento, (String) null);
	}

	public static void evento(ContextoTAS contexto, String evento, Objeto json) {
		evento(contexto, evento, json != null ? json.toSimpleJson() : null);
	}

	public static void evento(ContextoTAS contexto, String evento, String json) {
		try {
			SesionTAS sesion = contexto.sesion();
			TASClientePersona cliente = sesion.clienteTAS;
			if (cliente != null) {
				String cobis = cliente.idCliente;
				String cuit = cliente.numeroIdentificacionTributaria;
				String documento = cliente.numeroDocumento;
				String endpoint = contexto.path();
				String datos = json;
				String error = null;
				String idProceso = null;
				String idTas = contexto.sesion().getIdTas() != null ? contexto.sesion().getIdTas() : contexto.parametros.string("tasId", "");
				LogTAS.insert(contexto, cobis, cuit, documento, endpoint, evento, datos, error, idProceso, idTas);
			} else {
				String cobis = contexto.parametros.string("idCliente", null);
				String cuit = contexto.parametros.string("cuil", null);
				String documento = contexto.parametros.string("nroDoc", null);
				String endpoint = contexto.path();
				String datos = json;
				String error = null;
				String idProceso = null;
				String idTas = contexto.sesion().getIdTas() != null ? contexto.sesion().getIdTas() : contexto.parametros.string("tasId", "");
				LogTAS.insert(contexto, cobis, cuit, documento, endpoint, evento, datos, error, idProceso, idTas);
			}
		} catch (Exception ex) {
		}
	}

	public static void evento_validacion_baja_ca(ContextoTAS contexto, Objeto json) {
		try {
			String cobis = contexto.parametros.string("idCliente", null);
			String cuit = contexto.parametros.string("cuil", null);
			String documento = contexto.parametros.string("nroDoc", null);
			String endpoint = contexto.path();
			String evento = json.string("estado");
			String datos = json.string("mensaje");
			String error = json.string("motivo");
			String idProceso = contexto.sesion().idSesionTAS;
			String idTas = contexto.sesion().getIdTas() != null ? contexto.sesion().getIdTas() : contexto.parametros.string("tasId", null);
			LogTAS.insert(contexto, cobis, cuit, documento, endpoint, evento, datos, error, idProceso, idTas);

		} catch (Exception ex) {
		}
	}

	public static void error(ContextoTAS contexto, ApiException e) {
		try {
			SesionTAS sesion = contexto.sesion();
			TASClientePersona cliente = sesion.clienteTAS;
			if (cliente != null) {
				String codigoError = e.codigoError;
				String codigoApi = e.response.string("codigo");
				String mensaje = e.response.string("mensajeAlUsuario", null);

				String cobis = cliente.idCliente;
				String cuit = cliente.numeroIdentificacionTributaria;
				String documento = cliente.numeroDocumento;
				String endpoint = contexto.path();
				String evento = "ERROR_API";
				String datos = String.format("%s:%s:%s", e.request.api(), e.request.metodo().toLowerCase(), e.request.url());
				String error = mensaje != null ? String.format("%s | %s | %s", codigoError, codigoApi, mensaje) : e.response.body;
				String idProceso = e.request.idProceso();
				String idTas = contexto.sesion().getIdTas() != null ? contexto.sesion().getIdTas() : contexto.parametros.string("tasId", null);
				LogTAS.insert(contexto, cobis, cuit, documento, endpoint, evento, datos, error, idProceso, idTas);
			} else {
				Throwable t = getCause(e);
				String codigoError = e.codigoError;
				String codigoApi = e.response.string("codigo");
				String mensaje = e.response.string("mensajeAlUsuario", null);
				String cobis = contexto.parametros.string("idCliente", null);
				String cuit = contexto.parametros.string("cuil", null);
				String documento = contexto.parametros.string("nroDoc", null);
				String idTas = contexto.sesion().getIdTas() != null ? contexto.sesion().getIdTas() : contexto.parametros.string("tasId", null);
				String endpoint = contexto.path();
				String evento = "ERROR_API";
				String datos = Texto.stackTrace(e);
				String error = mensaje != null ? String.format("%s | %s | %s", codigoError, codigoApi, mensaje) : e.response.body;String idProceso = null;
				LogTAS.insert(contexto, cobis, cuit, documento, endpoint, evento, datos, error, idProceso, idTas);
			}
		} catch (Exception ex) {
		}
	}

	public static void error(ContextoTAS contexto, SqlException e) {
		try {
			SesionTAS sesion = contexto.sesion();
			TASClientePersona cliente = sesion.clienteTAS;
			if (cliente != null) {
				String cobis = cliente.idCliente;
				String cuit = cliente.numeroIdentificacionTributaria;
				String documento = cliente.numeroDocumento;
				String endpoint = contexto.path();
				String evento = "ERROR_SQL";
				String datos = Texto.stackTrace(e);
				String error = "ERROR".equals(e.codigoError) ? e.getMessage() : e.codigoError;
				String idProceso = null;
				String idTas = contexto.sesion().getIdTas() != null ? contexto.sesion().getIdTas() : contexto.parametros.string("tasId", null);
				LogTAS.insert(contexto, cobis, cuit, documento, endpoint, evento, datos, error, idProceso, idTas);
			} else {
				Throwable t = getCause(e);
				StackTraceElement st = stackTraceElement(t);
				String exception = t.getClass().getSimpleName();
				String message = t.getMessage();
				String cobis = contexto.parametros.string("idCliente", null);
				String cuit = contexto.parametros.string("cuil", null);
				String documento = contexto.parametros.string("nroDoc", null);
				String idTas = contexto.sesion().getIdTas() != null ? contexto.sesion().getIdTas() : contexto.parametros.string("tasId", null);
				String endpoint = contexto.path();
				String evento = "ERROR_SQL";
				String datos = Texto.stackTrace(e);
				String error = message != null ? String.format("%s: %s", exception, message) : exception;
				String idProceso = null;
				LogTAS.insert(contexto, cobis, cuit, documento, endpoint, evento, datos, error, idProceso, idTas);
			}
		} catch (Exception ex) {
		}
	}
	public static void error(ContextoTAS contexto, SQLException e) {
		try {
			SesionTAS sesion = contexto.sesion();
			TASClientePersona cliente = sesion.clienteTAS;
			if (cliente != null) {
				String cobis = cliente.idCliente;
				String cuit = cliente.numeroIdentificacionTributaria;
				String documento = cliente.numeroDocumento;
				String endpoint = contexto.path();
				String evento = "ERROR_SQL";
				String datos = Texto.stackTrace(e);
				String error = "ERROR".equals(e.getErrorCode()) ? e.getMessage() : String.valueOf(e.getErrorCode());
				String idProceso = null;
				String idTas = contexto.sesion().getIdTas() != null ? contexto.sesion().getIdTas() : contexto.parametros.string("tasId", null);
				LogTAS.insert(contexto, cobis, cuit, documento, endpoint, evento, datos, error, idProceso, idTas);
			} else {
				Throwable t = getCause(e);
				StackTraceElement st = stackTraceElement(t);
				String exception = t.getClass().getSimpleName();
				String message = t.getMessage();
				String cobis = contexto.parametros.string("idCliente", null);
				String cuit = contexto.parametros.string("cuil", null);
				String documento = contexto.parametros.string("nroDoc", null);
				String idTas = contexto.sesion().getIdTas() != null ? contexto.sesion().getIdTas() : contexto.parametros.string("tasId", null);
				String endpoint = contexto.path();
				String evento = "ERROR_SQL";
				String datos = Texto.stackTrace(e);
				String error = message != null ? String.format("%s: %s", exception, message) : exception;
				String idProceso = null;
				LogTAS.insert(contexto, cobis, cuit, documento, endpoint, evento, datos, error, idProceso, idTas);
			}
		} catch (Exception ex) {
		}
	}

	public static void error(ContextoTAS contexto, Exception e) {
		try {
			SesionTAS sesion = contexto.sesion();
			TASClientePersona cliente = sesion.clienteTAS;
			if (cliente != null) {
				Throwable t = getCause(e);
				StackTraceElement st = stackTraceElement(t);
				String exception = t.getClass().getSimpleName();
				String message = t.getMessage();

				String cobis = cliente.idCliente;
				String cuit = cliente.numeroIdentificacionTributaria;
				String documento = cliente.numeroDocumento;
				String idTas = contexto.sesion().getIdTas() != null ? contexto.sesion().getIdTas() : contexto.parametros.string("tasId", null);
				String endpoint = contexto.path();
				String evento = "ERROR_CRITICO";
				String datos = Texto.stackTrace(e);
				String error = message != null ? String.format("%s: %s", exception, message) : exception;
				String idProceso = null;
				LogTAS.insert(contexto, cobis, cuit, documento, endpoint, evento, datos, error, idProceso, idTas);
			} else {
				Throwable t = getCause(e);
				StackTraceElement st = stackTraceElement(t);
				String exception = t.getClass().getSimpleName();
				String message = t.getMessage();
				String cobis = contexto.parametros.string("idCliente", null);
				String cuit = contexto.parametros.string("cuil", null);
				String documento = contexto.parametros.string("nroDoc", null);
				String idTas = contexto.sesion().getIdTas() != null ? contexto.sesion().getIdTas() : contexto.parametros.string("tasId", null);
				String endpoint = contexto.path();
				String evento = "ERROR_CRITICO";
				String datos = Texto.stackTrace(e);
				String error = message != null ? String.format("%s: %s", exception, message) : exception;
				String idProceso = null;
				LogTAS.insert(contexto, cobis, cuit, documento, endpoint, evento, datos, error, idProceso, idTas);
			}
		} catch (Exception ex) {
		}
	}

	public static Futuro<Boolean> insert(ContextoTAS contexto, String cobis, String cuit, String documento, String endpoint, String evento, String datos, String error, String idProceso, String tasId) {
		String sql = "";
		sql += "INSERT INTO [HipotecarioTAS].[dbo].[log] ";
		sql += "([momento],[cobis],[cuit],[documento],[kiosco_id],[endpoint],[evento],[datos],[error],[idProceso],[ip]) ";
		sql += "VALUES (GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ";
		Object[] parametros = new Object[10];
		parametros[0] = cobis;
		parametros[1] = cuit;
		parametros[2] = documento;
		parametros[3] = tasId;
		parametros[4] = endpoint;
		parametros[5] = evento;
		parametros[6] = datos;
		parametros[7] = error;
		parametros[8] = idProceso;
		parametros[9] = contexto.ip();
		String sqlFinal = sql;
		return new Futuro<>(() -> Sql.update(contexto, "hipotecariotas", sqlFinal, parametros) == 1);
	}
}
