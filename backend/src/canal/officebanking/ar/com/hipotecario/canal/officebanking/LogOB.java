package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Texto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.backend.servicio.sql.SqlHB_BE;
import ar.com.hipotecario.backend.servicio.sql.hb_be.LogsOfficeBanking;

public class LogOB extends Modulo {

	public static void evento(ContextoOB contexto, String evento) {
		evento(contexto, evento, (Objeto) null);
	}

	public static void evento(ContextoOB contexto, String evento, String json) {
		try {
			SesionOB sesion = contexto.sesion();
			String usuario = sesion.usuarioOB != null ? sesion.usuarioOB.cuil.toString() : "0";
			if (usuario != null) {
				String empresa = sesion.empresaOB != null ? sesion.empresaOB.cuit.toString() : "0";
				String endpoint = !contexto.path().equals("") ? contexto.path() : null;
				String error = null;
				String idProceso = null;
				SqlHB_BE.logOB(contexto, empresa, usuario, endpoint, evento, json, error, idProceso);
			}
		} catch (Exception ex) {
		}
	}

	public static void evento(ContextoOB contexto, String evento, String json, String batch) {
		try {
			SesionOB sesion = contexto.sesion();
			String usuario = sesion.usuarioOB != null ? sesion.usuarioOB.cuil.toString() : "0";
			if (usuario != null) {
				String empresa = sesion.empresaOB != null ? sesion.empresaOB.cuit.toString() : "0";
				String endpoint = batch;
				String error = null;
				String idProceso = null;
				SqlHB_BE.logOB(contexto, empresa, usuario, endpoint, evento, json, error, idProceso);
			}
		} catch (Exception ex) {
		}
	}

	public static void evento(ContextoOB contexto, String evento, Objeto datos) {
		try {
			SesionOB sesion = contexto.sesion();
			String usuario = sesion.usuarioOB.cuil.toString();
			if (usuario != null) {
				String empresa = sesion.empresaOB.cuit.toString();
				String endpoint = contexto.path();
				String error = null;
				String idProceso = null;
				SqlHB_BE.logOB(contexto, empresa, usuario, endpoint, evento, datos != null ? datos.toSimpleJson() : null, error, idProceso);
			}
		} catch (Exception ex) {
		}
	}
	
	public static void evento(ContextoOB contexto, String evento, String empresa, String usuario, String datos) {
		try {
			if (usuario != null) {
				String endpoint = contexto.path();
				String error = null;
				String idProceso = null;
				SqlHB_BE.logOB(contexto, empresa, usuario, endpoint, evento, datos, error, idProceso);
			}
		} catch (Exception ex) {
		}
	}
	
	public static void evento_sinSesion(ContextoOB contexto, String evento, Objeto datos, String cuil) {
		try {
			String endpoint = contexto.path();
			String _cuil = cuil != null ? cuil : " ";
			String error = null;
			String idProceso = null;
			SqlHB_BE.logOB(contexto, null, _cuil, endpoint, evento, datos != null ? datos.toSimpleJson() : null, error, idProceso);			
		} catch (Exception ex) {
		}
	}

	public static void error(ContextoOB contexto, String estado) {
		try {
			SesionOB sesion = contexto.sesion();
			String usuario = sesion.usuarioOB.cuil.toString();
			if (usuario != null) {
				String empresa = sesion.empresaOB.cuit.toString();
				String endpoint = contexto.path();
				String evento = "ERROR";
				String datos = null;
				String error = estado;
				String idProceso = null;
				SqlHB_BE.logOB(contexto, empresa, usuario, endpoint, evento, datos, error, idProceso);
			}
		} catch (Exception ex) {
		}
	}

	public static void error(ContextoOB contexto, ApiException e) {
		try {
			SesionOB sesion = contexto.sesion();
			String usuario = sesion.usuarioOB.cuil.toString();
			if (usuario != null) {
				String codigoError = e.codigoError;
				String codigoApi = e.response.string("codigo");
				String mensaje = e.response.string("mensajeAlUsuario", null);

				String empresa = sesion.empresaOB.cuit.toString();
				String endpoint = contexto.path();
				String evento = "ERROR_API";
				String datos = String.format("%s:%s:%s", e.request.api(), e.request.metodo().toLowerCase(), e.request.url());
				String error = mensaje != null ? String.format("%s | %s | %s", codigoError, codigoApi, mensaje) : e.response.body;
				String idProceso = e.request.idProceso();
				SqlHB_BE.logOB(contexto, empresa, usuario, endpoint, evento, datos, error, idProceso);
			}
		} catch (Exception ex) {
		}
	}

	public static void error(ContextoOB contexto, SqlException e) {
		try {
			SesionOB sesion = contexto.sesion();
			String usuario = sesion.usuarioOB.cuil.toString();
			if (usuario != null) {
				String empresa = sesion.empresaOB.cuit.toString();
				String endpoint = contexto.path();
				String evento = "ERROR_SQL";
				String datos = Texto.stackTrace(e);
				String error = "ERROR".equals(e.codigoError) ? e.getMessage() : e.codigoError;
				String idProceso = null;
				SqlHB_BE.logOB(contexto, empresa, usuario, endpoint, evento, datos, error, idProceso);
			}
		} catch (Exception ex) {
		}
	}

	public static void error(ContextoOB contexto, Exception e) {
		try {
			SesionOB sesion = contexto.sesion();
			String usuario = sesion.usuarioOB.cuil.toString();
			if (usuario != null) {
				Throwable t = getCause(e);
				StackTraceElement st = stackTraceElement(t);
				String exception = t.getClass().getSimpleName();
				String message = t.getMessage();

				String empresa = sesion.empresaOB.cuit.toString();
				String endpoint = contexto.path();
				String evento = "ERROR_CRITICO";
				String datos = st != null ? String.format("%s:%s:%s()", st.getFileName(), st.getLineNumber(), st.getMethodName()) : null;
				String error = message != null ? String.format("%s: %s", exception, message) : exception;
				String idProceso = null;
				SqlHB_BE.logOB(contexto, empresa, usuario, endpoint, evento, datos, error, idProceso);
			}
		} catch (Exception ex) {
		}
	}
	public static LogsOfficeBanking getLogs(ContextoOB contexto,String fecha1,String fecha2,String cuitEmpresa) {
		try {
			LogsOfficeBanking resultado =  SqlHB_BE.selectPorFecha(contexto, fecha1, fecha2, cuitEmpresa ).tryGet() ;
			return resultado;
		} catch (Exception ex) {
		error(contexto,ex);
		}
		return null;
	}
}
