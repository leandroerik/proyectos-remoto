package ar.com.hipotecario.canal.tas;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import ar.com.hipotecario.backend.Servidor;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.exception.SqlException;

public class CanalTAS extends Servidor {

	/* ========== ATRIBUTOS ========== */
	public static Map<String, Function<ContextoTAS, Object>> endpoints = new HashMap<>();

	/* ========== SERVIDOR ========== */
	private static Object servidor(ContextoTAS contexto, Function<ContextoTAS, Object> funcion) {
		// SesionTAS sesion = contexto.sesion();
		try {
			String url = contexto.request.uri();
			Boolean generarNuevaSesion = url.endsWith("/login") || url.endsWith("/logout"); 
			String uuid = contexto.requestHeader("uuid");
			
			if (generarNuevaSesion) {
				uuid = UUID.randomUUID().toString();
				contexto._uuid_ = uuid;
				contexto.responseHeader("uuid", uuid);
			}
			
			Object object = funcion.apply(contexto);
			if (object instanceof Objeto) {
				Objeto objeto = (Objeto) object;
				if (generarNuevaSesion) {
					objeto.set("_uuid_", uuid);
				}
				
				String estado = objeto.string("estado");
				if (estado != null && !estado.isEmpty()) {
					if(!estado.contains("ERROR")){
						LogTAS.evento(contexto, estado, contexto.parametrosOfuscados());
					}
				}
			}
			return object;
		} catch (ApiException e) {
			log(contexto, e);
			LogTAS.error(contexto, e);
			return contexto.response(500, new Objeto().set("estado", "ERROR"));
		} catch (SqlException e) {
			log(contexto, e);
			LogTAS.error(contexto, e);
			return contexto.response(500, new Objeto().set("estado", "ERROR"));
		} catch (Exception e) {
			log(contexto, e);
			LogTAS.error(contexto, e);
			if (e.getCause() != null && e.getCause() instanceof SocketTimeoutException) {
				return contexto.response(500, new Objeto().set("estado", "TIMEOUT"));
			}
			return contexto.response(500, new Objeto().set("estado", "ERROR"));
		} finally {
			// sesion.save();
		}
	}

	/* ========== ENDPOINTS ========== */
	protected static void get(String url, Function<ContextoTAS, Object> funcion) {
		endpoints.put("GET:" + url, funcion);
		Servidor.get("TAS", url, contexto -> servidor(contexto, funcion), ContextoTAS.class);
	}

	protected static void post(String url, Function<ContextoTAS, Object> funcion) {
		endpoints.put("POST:" + url, funcion);
		Servidor.post("TAS", url, contexto -> servidor(contexto, funcion), ContextoTAS.class);
	}

	protected static void put(String url, Function<ContextoTAS, Object> funcion) {
		endpoints.put("PUT:" + url, funcion);
		Servidor.put("TAS", url, contexto -> servidor(contexto, funcion), ContextoTAS.class);
	}

	protected static void patch(String url, Function<ContextoTAS, Object> funcion) {
		endpoints.put("PATCH:" + url, funcion);
		Servidor.patch("TAS", url, contexto -> servidor(contexto, funcion), ContextoTAS.class);
	}

	protected static void delete(String url, Function<ContextoTAS, Object> funcion) {
		endpoints.put("DELETE:" + url, funcion);
		Servidor.delete("TAS", url, contexto -> servidor(contexto, funcion), ContextoTAS.class);
	}
}
