package ar.com.hipotecario.canal.buhobank;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import ar.com.hipotecario.backend.base.Fecha;
import com.github.jknack.handlebars.Handlebars.Utils;

import ar.com.hipotecario.backend.Servidor;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.exception.MaximoReintentosException;
import ar.com.hipotecario.backend.exception.ParametrosIncorrectosException;
import ar.com.hipotecario.backend.exception.SesionExpiradaException;
import ar.com.hipotecario.backend.exception.SinSesionException;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.backend.exception.UnauthorizedException;

public class CanalBuhoBank extends Servidor {

	public static String ESTADO = "estado";

	/* ========== ATRIBUTOS ========== */
	public static Map<String, Function<ContextoBB, Object>> endpoints = new HashMap<>();

	/* ========== SERVIDOR ========== */
	private static Object servidor(ContextoBB contexto, Function<ContextoBB, Object> funcion, Boolean validarSesion, Boolean esPFWeb, Boolean esUsuarioConfig) {
		String token = null;
		try {
			String origin = Util.firstNonNull(contexto.request.headers("Origin"), contexto.request.headers("Referer"), "");
			contexto.response.header("Strict-Transport-Security", "max-age=31536000;");
			contexto.response.header("Content-Security-Policy", "default-src 'self' " + origin);
			contexto.response.header("Permissions-Policy", "geolocation=()");
			contexto.response.header("Feature-Policy", "geolocation 'none'");
			contexto.response.header("X-Content-Type-Options", "nosniff");

			if (esUsuarioConfig && contexto.esProduccion()) {

				String usuario = BBSeguridad.getUsuarioJWT(contexto);
				if (Utils.isEmpty(usuario)) {
					return contexto.response(401, BBSeguridad.getValueJWT(contexto));
				}
			}

			SesionBB sesion = contexto.sesion();

			SinSesionException.throwIf(validarSesion && !sesion.usuarioLogueado());
			token = sesion.token;
			SesionExpiradaException.throwIf(validarSesion && sesion.expirada() && contexto.prendidoExpiracionSesion());

			if (validarSesion) {
				sesion.actualizarFechaUltimaActividad();
			}

			Object object = funcion.apply(contexto);

			MaximoReintentosException.throwIf(validarSesion && sesion.maximoReintentos() && contexto.prendidoMaximoReintentos());

			if (esPFWeb && object instanceof Objeto) {
				String uuid = contexto.request.headers("uuid");
				if (uuid == null || uuid.isEmpty()) {
					((Objeto) object).set("uuid", UUID.randomUUID().toString());
				}
			}

			if (!contexto.esProduccion()) {
				try{
					String uriBack = contexto.request.requestMethod() + ":" + contexto.request.uri();
					if(!uriBack.contains("health")){
						System.out.println("[" + Fecha.horaSegundoActual() + "] REQUEST APP [cobis:" + contexto.sesion().idCobis + "] " + uriBack + " " + contexto.parametros.toString().replace("\n", "").replace("\r", "").replace("  ", " ")
								+ "\n" + "RESPONSE APP " + object.toString().replace("\n", "").replace("\r", "").replace("    ", " ").replace("  ", " "));
					}
				} catch (Exception e){}
			}

			return object;
		} catch (UnauthorizedException e) {
			contexto.response.status(401);
			contexto.response.header("www-authenticate", "Basic");
			return "";
		}
		catch (SesionExpiradaException e) {
			LogBB.error(contexto, "SESION_EXPIRADA", token);
			contexto.deleteSesion();
			return contexto.response(200, new Objeto().set(ESTADO, "SESION_EXPIRADA"));
		} catch (MaximoReintentosException e) {
			LogBB.error(contexto, "MAXIMO_REINTENTOS", token);
			contexto.deleteSesion();
			return contexto.response(200, new Objeto().set(ESTADO, "MAXIMO_REINTENTOS"));
		} catch (SinSesionException e) {
			return contexto.response(200, new Objeto().set(ESTADO, "SIN_SESION"));
		} catch (ParametrosIncorrectosException e) {
			return contexto.response(200, new Objeto().set(ESTADO, "PARAMETROS_INCORRECTOS").set("mensaje", e.getMessage()));
		} catch (ApiException e) {
			log(contexto, e);
			if (e.request.api().equalsIgnoreCase("ventas")) {
				String mensajeError = e.response.string("Errores.0.MensajeCliente");
				LogBB.error((ContextoBB) contexto, mensajeError);
				return contexto.response(500, new Objeto().set("ERROR", e.codigoError));
			} else {
				LogBB.error(contexto, e);
				return contexto.response(500, new Objeto().set(ESTADO, e.codigoError));
			}
		} catch (SqlException e) {
			log(contexto, e);
			LogBB.error(contexto, e);
			return contexto.response(500, new Objeto().set(ESTADO, e.codigoError));
		} catch (Exception e) {
			log(contexto, e);
			LogBB.error(contexto, e);
			if (e.getCause() != null && e.getCause() instanceof SocketTimeoutException) {
				return contexto.response(500, new Objeto().set(ESTADO, "TIMEOUT"));
			}
			return contexto.response(500, new Objeto().set(ESTADO, "ERROR"));
		}
	}

	/* ========== VALIDA SESION ========== */
	protected static void get(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("GET:" + url, funcion);
		Servidor.get(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, true, false, false), ContextoBB.class);
	}

	protected static void post(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("POST:" + url, funcion);
		Servidor.post(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, true, false, false), ContextoBB.class);
	}

	protected static void put(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("PUT:" + url, funcion);
		Servidor.put(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, true, false, false), ContextoBB.class);
	}

	protected static void patch(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("PATCH:" + url, funcion);
		Servidor.patch(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, true, false, false), ContextoBB.class);
	}

	protected static void delete(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("DELETE:" + url, funcion);
		Servidor.delete(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, true, false, false), ContextoBB.class);
	}

	/* ========== NO VALIDA SESION ========== */
	protected static void iget(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("GET:" + url, funcion);
		Servidor.get(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, false, false, false), ContextoBB.class);
	}

	protected static void ipost(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("POST:" + url, funcion);
		Servidor.post(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, false, false, false), ContextoBB.class);
	}

	protected static void iput(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("PUT:" + url, funcion);
		Servidor.put(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, false, false, false), ContextoBB.class);
	}

	protected static void ipatch(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("PATCH:" + url, funcion);
		Servidor.patch(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, false, false, false), ContextoBB.class);
	}

	protected static void idelete(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("DELETE:" + url, funcion);
		Servidor.delete(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, false, false, false), ContextoBB.class);
	}

	/* ========== PLAZO FIJO WEB ========== */
	protected static void oget(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("GET:" + url, funcion);
		Servidor.get(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, false, true, false), ContextoBB.class);
	}

	protected static void opost(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("POST:" + url, funcion);
		Servidor.post(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, false, true, false), ContextoBB.class);
	}

	protected static void oput(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("PUT:" + url, funcion);
		Servidor.put(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, false, true, false), ContextoBB.class);
	}

	protected static void opatch(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("PATCH:" + url, funcion);
		Servidor.patch(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, false, true, false), ContextoBB.class);
	}

	protected static void odelete(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("DELETE:" + url, funcion);
		Servidor.delete(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, false, true, false), ContextoBB.class);
	}

	/* ========== USUARIO CONFIG ========== */
	protected static void aget(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("GET:" + url, funcion);
		Servidor.get(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, false, false, true), ContextoBB.class);
	}

	protected static void apost(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("POST:" + url, funcion);
		Servidor.post(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, false, false, true), ContextoBB.class);
	}

	protected static void aput(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("PUT:" + url, funcion);
		Servidor.put(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, false, false, true), ContextoBB.class);
	}

	protected static void apatch(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("PATCH:" + url, funcion);
		Servidor.patch(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, false, false, true), ContextoBB.class);
	}

	protected static void adelete(String url, Function<ContextoBB, Object> funcion) {
		endpoints.put("DELETE:" + url, funcion);
		Servidor.delete(GeneralBB.CANAL_CODIGO, url, contexto -> servidor(contexto, funcion, false, false, true), ContextoBB.class);
	}
}
