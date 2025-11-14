package ar.com.hipotecario.canal.officebanking;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

import ar.com.hipotecario.backend.Parametros;
import ar.com.hipotecario.backend.Servidor;
import ar.com.hipotecario.backend.base.Encriptador;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Texto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.exception.*;
import ar.com.hipotecario.canal.officebanking.util.Validador3Scale;

public class CanalOfficeBanking extends Servidor {

	/* ========== ROLES ========== */
	public static String ROL_INVITADO = "INVITADO";
	public static String ROL_IDENTIFICADO = "IDENTIFICADO";
	public static String ROL_LOGUEADO = "LOGUEADO";
	public static String ROL_OB_ANTERIOR = "LOGUEADO_OB_ANTERIOR";
	public static String ROL_API_OB_ANTERIOR = "API_OB_ANTERIOR";
	public static String ROL_CRM = "ROL_CRM";

	/* ========== SERVIDOR ========== */
	private static Object servidor(ContextoOB contexto, Function<ContextoOB, Object> funcion, String rol) {
		try {
			// contexto.entityManager = ... // POSIBLE SOLUCION: un entity manager por cada
			// request

			String origin = Util.firstNonNull(contexto.request.headers("Origin"), contexto.request.headers("Referer"), "");
			contexto.response.header("Strict-Transport-Security", "max-age=31536000;");
			contexto.response.header("Content-Security-Policy", "default-src 'self' " + origin);
			contexto.response.header("Permissions-Policy", "geolocation=()");
			contexto.response.header("Feature-Policy", "geolocation 'none'");
			contexto.response.header("X-Content-Type-Options", "nosniff");
			if(contexto.sesion().userAgent.isEmpty()){
				contexto.sesion().userAgent = contexto.requestHeader("User-Agent");
				contexto.sesion().save();
			}else{
				if(!contexto.requestHeader("User-Agent").equals(contexto.sesion().userAgent)){
					System.out.println("userAgent matasesion");
					contexto.deleteSesion();
				}
			}

			if(contexto.sesion().ip == null){
				contexto.sesion().ip = contexto.request.ip();
				contexto.sesion().save();
			}else{
				if(!contexto.request.ip().equals(contexto.sesion().ip)){
					System.out.println("ip matasesion");
					contexto.deleteSesion();
				}
			}

			String xCanal = contexto.request.headers("x-canal");

			Boolean permitirCors = !contexto.esOpenShift();
			permitirCors |= contexto.esOpenShift() && contexto.esDesarrollo();
			if (contexto.config.bool("ob_cors_estricto")) {
				permitirCors |= contexto.esOpenShift() && contexto.esHomologacion() && origin.toLowerCase().startsWith("https://empresashomo.hipotecario.com.ar");
				permitirCors |= contexto.esOpenShift() && contexto.esHomologacion() && origin.toLowerCase().startsWith("https://bancaempresas-frontend-canales-homo.appd.bh.com.ar");
				permitirCors |= contexto.esOpenShift() && contexto.esHomologacion() && origin.toLowerCase().startsWith("https://bancaempresas-frontend-canales-homo.apps.aro-np.bh.com.ar");
			} else {
				permitirCors |= contexto.esOpenShift() && contexto.esHomologacion();
			}
			permitirCors |= contexto.esOpenShift() && contexto.esProduccion() && origin.toLowerCase().startsWith("https://empresas.hipotecario.com.ar");
			permitirCors |= contexto.esOpenShift() && "HB_BE".equals(xCanal);
			/*if (!permitirCors) {
				contexto.response.status(403);
				return "";
			}*/


			SesionOB sesion = contexto.sesion();
			SesionOBAnterior sesionOBAnterior = contexto.sesionOBAnterior();

//			String tokenSesion = contexto.requestHeader("tokensesion");
//			TokenOB tokenOB = null;
//			if (tokenSesion != null && !tokenSesion.isEmpty()) {
//				tokenOB = SqlHB_BE.tokenOB(contexto, tokenSesion).get();
//			}

			Boolean permitirOBanterior = xCanal != null && xCanal.equalsIgnoreCase("HB_BE");

			SinSesionException.throwIf(ROL_IDENTIFICADO.equals(rol) && !sesion.usuarioPseudoLogueado());
			SinSesionException.throwIf(ROL_LOGUEADO.equals(rol) && !sesion.usuarioLogueado());
			SinSesionException.throwIf(ROL_OB_ANTERIOR.equals(rol) && !sesionOBAnterior.usuarioLogueado());
			SinSesionException.throwIf(ROL_API_OB_ANTERIOR.equals(rol) && !permitirOBanterior /* && !tokenOB.expirado() */); // TODO: Manejar la expiracion de la sesion
			if (ROL_CRM.equals(rol) && !validarCRM(contexto)) {
				throw new UnauthorizedException();
			}
			SesionExpiradaException.throwIf(ROL_LOGUEADO.equals(rol) && sesion.expirada());
			sesion.actualizarFechaUltimaActividad();

			try {
				Parametros params;
				String sessionID = null;
				if (!Util.empty(contexto.parametros.get("sessionid"))) {
					System.out.println((contexto.parametros.get("sessionid").getClass()));
					sessionID =(String) contexto.parametros.get("sessionid");
				}

				if (!Util.empty(contexto.requestHeader("token"))) {
					String clave = Fecha.hoy().string("yyyy-MM-dd");
					String token = contexto.parametros.string("token");
					String parametros = Encriptador.desencriptarBase64(clave, token);
					params  = Parametros.fromJson(parametros);
					contexto.parametros = params;
				}
				contexto.parametros.set("sessionID", sessionID);

			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			Object object = funcion.apply(contexto);
			if (object instanceof Objeto) {
				String estado = ((Objeto) object).string("estado");
				String status = ((Objeto) object).string("status");
				if (!"0".equals(estado) && !"UP".equals(status)) {
					LogOB.error(contexto, estado);
				}
			}
			return object;
		} catch (SesionExpiradaException e) {
			String stackTrace = contexto.esProduccion() ? "" : Texto.stackTrace(e);
			LogOB.error(contexto, e);
			contexto.deleteSesion();
			return contexto.response(401, new Objeto().set("estado", "SESION_EXPIRADA").set("stack", stackTrace));
		} catch (SinSesionException e) {
			String stackTrace = contexto.esProduccion() ? "" : Texto.stackTrace(e);
			LogOB.error(contexto, e);
			return contexto.response(401, new Objeto().set("estado", "SIN_SESION").set("stack", stackTrace));
		} catch (ParametrosIncorrectosException e) {
			String stackTrace = contexto.esProduccion() ? "" : Texto.stackTrace(e);
			LogOB.error(contexto, e);
			return contexto.response(400, new Objeto().set("estado", "PARAMETROS_INCORRECTOS").set("mensaje", e.getMessage()).set("stack", stackTrace));
		} catch (ApiException e) {
			String stackTrace = contexto.esProduccion() ? "" : Texto.stackTrace(e);
			LogOB.error(contexto, e);
			return contexto.response(200, new Objeto().set("estado", e.codigoError).set("stack", stackTrace));
		} catch (SqlException e) {
			String stackTrace = contexto.esProduccion() ? "" : Texto.stackTrace(e);
			LogOB.error(contexto, e);
			return contexto.response(200, new Objeto().set("estado", e.codigoError).set("stack", stackTrace));
		} catch (UnauthorizedException e) {
			String stackTrace = contexto.esProduccion() ? "" : Texto.stackTrace(e);
			LogOB.error(contexto, e);
			return contexto.response(401, new Objeto().set("estado", "ERROR").set("stack", stackTrace));
		} catch (Exception e) {
			String stackTrace = contexto.esProduccion() ? "" : Texto.stackTrace(e);
			log(contexto, e);
			LogOB.error(contexto, e);
			if (e.getCause() != null && e.getCause() instanceof SocketTimeoutException) {
				return contexto.response(500, new Objeto().set("estado", "TIMEOUT").set("stack", stackTrace));
			}
			return contexto.response(200, new Objeto().set("estado", "ERROR").set("stack", stackTrace));
		}
	}

	/* ========== ENDPOINTS ========== */
	protected static void get(String url, Function<ContextoOB, Object> funcion, String rol) {
		Servidor.get("OB", url, contexto -> servidor(contexto, funcion, rol), ContextoOB.class);
	}

	protected static void post(String url, Function<ContextoOB, Object> funcion, String rol) {
		Servidor.post("OB", url, contexto -> servidor(contexto, funcion, rol), ContextoOB.class);
	}

	protected static void put(String url, Function<ContextoOB, Object> funcion, String rol) {
		Servidor.put("OB", url, contexto -> servidor(contexto, funcion, rol), ContextoOB.class);
	}

	protected static void patch(String url, Function<ContextoOB, Object> funcion, String rol) {
		Servidor.patch("OB", url, contexto -> servidor(contexto, funcion, rol), ContextoOB.class);
	}

	protected static void delete(String url, Function<ContextoOB, Object> funcion, String rol) {
		Servidor.delete("OB", url, contexto -> servidor(contexto, funcion, rol), ContextoOB.class);
	}

	/* ========== ENDPOINTS USUARIOS LOGUEADOS ========== */
	protected static void get(String url, Function<ContextoOB, Object> funcion) {
		get(url, funcion, ROL_LOGUEADO);
	}

	protected static void post(String url, Function<ContextoOB, Object> funcion) {
		post(url, funcion, ROL_LOGUEADO);
	}

	protected static void put(String url, Function<ContextoOB, Object> funcion) {
		put(url, funcion, ROL_LOGUEADO);
	}

	protected static void patch(String url, Function<ContextoOB, Object> funcion) {
		patch(url, funcion, ROL_LOGUEADO);
	}

	protected static void delete(String url, Function<ContextoOB, Object> funcion) {
		delete(url, funcion, ROL_LOGUEADO);
	}

	/* ========== UTILITARIOS ========== */
	protected static boolean validarCRM(ContextoOB contexto) {
        String xUsuario = null;
        try {
            xUsuario = contexto.request.headers("x-usuario");
			xUsuario = Optional.ofNullable(contexto.request.headers("x-usuario")).orElse("USR_NO_DEF");
            LogCrmOB.evento(contexto, 0, 0, "ValidarCRM - Validando acceso a CRM", xUsuario);
            String auth = contexto.request.headers("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                LogCrmOB.evento(contexto, 0, 0, "ValidarCRM - Error: Authorization no existe o se encuentra mal formulado", xUsuario);
                return false;
            }
            String token = auth.substring(7);
            if (!Validador3Scale.validarJwt(token, "crm",false)) {
                LogCrmOB.evento(contexto, 0, 0, "ValidarCRM - Error: Token no valido o ocurrio un error en su validacion", xUsuario);
                return false;
            }

            String canal = contexto.request.headers("x-canal");
            String subcanal = contexto.request.headers("x-subcanal");
            return "CRME".equals(canal) && "CRME".equals(subcanal);
        } catch (Exception e) {
			String mensajeError = Optional.ofNullable(e.getMessage()).orElse("Sin mensaje de error");
			String mensajeTruncado = mensajeError.length() > 20 ? mensajeError.substring(0, 20) : mensajeError;
			LogCrmOB.evento(contexto, 0, 0, "ValidarCRM - Error: Exception " + mensajeTruncado, xUsuario);
            return false;
        }
    }
}