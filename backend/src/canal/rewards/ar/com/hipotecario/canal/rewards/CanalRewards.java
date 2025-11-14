package ar.com.hipotecario.canal.rewards;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import ar.com.hipotecario.backend.Servidor;
import ar.com.hipotecario.backend.Sesion;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.exception.ParametrosIncorrectosException;
import ar.com.hipotecario.backend.exception.SqlException;

public class CanalRewards extends Servidor {

	public static String SEGURIDAD_INVITADO = "INVITADO";
	public static String SEGURIDAD_LOGUEADO = "LOGUEADO";

	/* ========== ATRIBUTOS ========== */
	public static Map<String, Function<ContextoRewards, Object>> endpoints = new HashMap<>();

	/* ========== SERVIDOR ========== */
	private static Object servidor(ContextoRewards contexto, Function<ContextoRewards, Object> funcion,
			String seguridad) {
		try {
			if (seguridad.equals(SEGURIDAD_LOGUEADO)) {
				SesionRewards sesion = contexto.sesion();
				if (sesion == null || sesion.usr == null) {
					return contexto.response(401, new Objeto().set("estado", "SIN_SESION"));
				}
			}
			Object object = funcion.apply(contexto);
			return object;
		} catch (ApiException e) {
			return contexto.response(500, new Objeto().set("estado", "ERROR"));
		} catch (SqlException e) {
			return contexto.response(500, new Objeto().set("estado", "ERROR"));
		} catch (ParametrosIncorrectosException e) {
			return contexto.response(400, new Objeto().set("estado", "PARAMETROS_INCORRECTOS"));
		} catch (Exception e) {
			if (e.getCause() != null && e.getCause() instanceof SocketTimeoutException) {
				return contexto.response(500, new Objeto().set("estado", "TIMEOUT"));
			}
			return contexto.response(500, new Objeto().set("estado", "ERROR"));
		} finally {
			SesionRewards sesion = contexto.sesion();
			if (sesion != null && sesion.sesionModificada()) {
				// Sesion sesionGeneral = contexto.sesion(Sesion.class);
				// sesionGeneral.save();
				sesion.save();
			}
		}
	}

	/* ========== ENDPOINTS ========== */
	protected static void get(String url, Function<ContextoRewards, Object> funcion, String seguridad) {
		endpoints.put("GET:" + url, funcion);
		Servidor.get("REWARDS", url, contexto -> servidor(contexto, funcion, seguridad), ContextoRewards.class);
	}

	protected static void post(String url, Function<ContextoRewards, Object> funcion, String seguridad) {
		endpoints.put("POST:" + url, funcion);
		Servidor.post("REWARDS", url, contexto -> servidor(contexto, funcion, seguridad), ContextoRewards.class);
	}

	protected static void put(String url, Function<ContextoRewards, Object> funcion, String seguridad) {
		endpoints.put("PUT:" + url, funcion);
		Servidor.put("REWARDS", url, contexto -> servidor(contexto, funcion, seguridad), ContextoRewards.class);
	}

	protected static void patch(String url, Function<ContextoRewards, Object> funcion, String seguridad) {
		endpoints.put("PATCH:" + url, funcion);
		Servidor.patch("REWARDS", url, contexto -> servidor(contexto, funcion, seguridad), ContextoRewards.class);
	}

	protected static void delete(String url, Function<ContextoRewards, Object> funcion, String seguridad) {
		endpoints.put("DELETE:" + url, funcion);
		Servidor.delete("REWARDS", url, contexto -> servidor(contexto, funcion, seguridad), ContextoRewards.class);
	}

	/* ========== ENDPOINTS ========== */
	protected static void get(String url, Function<ContextoRewards, Object> funcion) {
		endpoints.put("GET:" + url, funcion);
		Servidor.get("REWARDS", url, contexto -> servidor(contexto, funcion, SEGURIDAD_LOGUEADO),
				ContextoRewards.class);
	}

	protected static void post(String url, Function<ContextoRewards, Object> funcion) {
		endpoints.put("POST:" + url, funcion);
		Servidor.post("REWARDS", url, contexto -> servidor(contexto, funcion, SEGURIDAD_LOGUEADO),
				ContextoRewards.class);
	}

	protected static void put(String url, Function<ContextoRewards, Object> funcion) {
		endpoints.put("PUT:" + url, funcion);
		Servidor.put("REWARDS", url, contexto -> servidor(contexto, funcion, SEGURIDAD_LOGUEADO),
				ContextoRewards.class);
	}

	protected static void patch(String url, Function<ContextoRewards, Object> funcion) {
		endpoints.put("PATCH:" + url, funcion);
		Servidor.patch("REWARDS", url, contexto -> servidor(contexto, funcion, SEGURIDAD_LOGUEADO),
				ContextoRewards.class);
	}

	protected static void delete(String url, Function<ContextoRewards, Object> funcion) {
		endpoints.put("DELETE:" + url, funcion);
		Servidor.delete("REWARDS", url, contexto -> servidor(contexto, funcion, SEGURIDAD_LOGUEADO),
				ContextoRewards.class);
	}
}
