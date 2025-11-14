package ar.com.hipotecario.mobile.conector;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.CacheMB;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.excepcion.ApiExceptionMB;
import ar.com.hipotecario.mobile.lib.*;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ApiMB {

	/* ========== ATRIBUTOS ========== */
	private static OkHttpClient http = Http.okHttpClient();
	private static Logger log = LoggerFactory.getLogger(ApiMB.class);
//	public static Boolean habilitarLog = Config.bool("habilitar_logs");

	/* ========== SYNC CACHE ========== */
	private static Map<String, ConcurrentHashMap<String, ReentrantLock>> mutexMap = new ConcurrentHashMap<>();


	/* ========== CUANDO EN LAS LLAMADAS EN PARALELO FINALIZA EL HILO PRINCIPAL ANTES DE QUE SE GENERE EL REQUEST DEL HILO SECUNDARIO, SE PIERDE LA SESION ========== */
	private static String getIdSession(ContextoMB contexto) {
		String idSesion = null;
		try{
			idSesion = String.valueOf(contexto.idSesion().hashCode()).replace("-", "");
		}
		catch(IllegalStateException e) {
			if(Objects.nonNull(contexto.getParametros().get(contexto.CONTEXT_SESSION_ID)))
				idSesion = contexto.getParametros().string(contexto.CONTEXT_SESSION_ID);
		}
		return idSesion;
	}
	private static void permitirSinLogin(ApiRequestMB request) {
		try{
			if (request.contexto.simplePath().contains("login"))
				request.permitirSinLogin = true;
		}
		catch(IllegalStateException e) {
			if (request.contexto.getParametros().string(request.contexto.CONTEXT_SIMPLE_PATH).contains("login"))
				request.permitirSinLogin = true;
		}
	}
	
	private static Boolean esUsuarioLogueado(ApiRequestMB request) {
		Boolean usuarioLogueado = null;
		try{
			usuarioLogueado = request.contexto.sesion().usuarioLogueado();
		}
		catch(IllegalStateException e) {
			if(Objects.nonNull(request.contexto.getParametros().get(request.contexto.CONTEXT_USUARIO_LOGUEADO)))
				usuarioLogueado = request.contexto.getParametros().bool(request.contexto.CONTEXT_USUARIO_LOGUEADO);
		}
		return usuarioLogueado;
	}
	
	/* ========== REQUEST ========== */
	public static ApiRequestMB request(String servicio, String api, String metodo, String recurso, ContextoMB contexto) {
		String idProceso = Util.idProceso();

		ApiRequestMB apiRequest = new ApiRequestMB();
		apiRequest.api = api;
		apiRequest.servicio = servicio;
		apiRequest.method = metodo;
		apiRequest.url = ConfigMB.string("api_url_" + api) + recurso;
		apiRequest.headers.put("Content-Type", "application/json; charset=utf-8");
		apiRequest.recurso = recurso;

		if (!api.equals("ventas_windows")) {
			apiRequest.headers.put("x-cobis", contexto.idCobis());
			apiRequest.headers.put("x-usuario", contexto.idCobis());
			apiRequest.headers.put("x-idSesion", getIdSession(contexto));
			apiRequest.headers.put("x-canal", "MB");
			apiRequest.headers.put("x-subCanal", contexto.config.string("subcanal", ConfigMB.esARO() ? "BACUNI-ARO" : "BACUNI"));
			apiRequest.headers.put("x-operador", null);
			apiRequest.headers.put("x-usuarioIP", ip(contexto));
			apiRequest.headers.put("x-idProceso", idProceso);
			apiRequest.headers.put("x-Sistema", "MB");
		}

		if (api.equals("ventas_windows")) {
			apiRequest.headers.put("x-cobis", contexto.idCobis());
			apiRequest.headers.put("x-idSesion", getIdSession(contexto));
			apiRequest.headers.put("x-idProceso", idProceso);
			apiRequest.headers.put("x-usuarioIP", ip(contexto));

			apiRequest.headers.put("X-Usuario", ConfigMB.string("configuracion_usuario"));
			apiRequest.headers.put("X-Canal", "26");
			apiRequest.headers.put("X-Subcanal", "0");
			apiRequest.headers.put("X-Token", idProceso);
			apiRequest.headers.put("X-Ambiente", "MB");
			apiRequest.headers.put("X-Starttime", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
			apiRequest.headers.put("X-Handle", idProceso);
			apiRequest.headers.put("X-Sistema", "MB");
		}
		
//		String usuarioOAG = ConfigMB.string("oag_usuario") + "@bh.com.ar";
//		String passwordOAG = ConfigMB.string("oag_clave");
//		
//		if(!api.equals("consentimiento")) {
//			apiRequest.headers.put("Authorization", "Basic " + Http.authBasic(usuarioOAG, passwordOAG));
//		}
		
		if ("true".equals(ConfigMB.string("threescale", "true"))) {
			String threeScale = ConfigMB.string("threescale_url_" + api, null);
			if (threeScale != null) {
				String clientId = threeScale.split(" ")[0];
				String secretId = threeScale.split(" ")[1];
				String url = threeScale.split(" ")[2];
				String token = ThreeScaleMB.token(api, clientId, secretId, false);
				apiRequest.url = url + recurso;
				apiRequest.headers.put("Authorization", "Bearer " + token);
			}
		}

		apiRequest.contexto = contexto;
		return apiRequest;
	}

	private static String ip(ContextoMB contexto) {
		return contexto.ip().startsWith("fe80:") 
				|| contexto.ip().equals("[0:0:0:0:0:0:0:1]")  ? "127.0.0.1" : contexto.ip();
	}

	/* ========== RESPONSE ========== */
	/**
	 * El metodo que realmente llama al servicio es responseRAW()
	 * Este es un metodo envoltorio que se encarga de ordenar los hilos para evitar doble llamadas a servicios.
	 * El funcionamiento basico es el siguiente:
	 *  - Cuando un usuario llama a un servicio cacheable se ejecuta un LOCK para ese servicio+parametros
	 *  - Si otro hilo quiere llamara al mismo servicio+parametros el hilo se bloquea hasta que el primero termine
	 *  - Una vez que el primer hilo termina la llamada a este servicio en concreto, el hilo actual se ejecuta
	 *  - Cuando se ejecuta el hilo actual, los datos ya estan en cache y se evita la llamada al servicio
	 */
	public static ApiResponseMB response(ApiRequestMB request, Object... camposClave) {
		ContextoMB contexto = request.contexto;

		if (!ConfigMB.bool("mb_habilitar_concurrencia", true)) {
			return responseRAW(request, camposClave);
		}
		
		String idCobis = contexto.idCobis();

		if (!request.cacheSesion || idCobis == null) {
			return responseRAW(request, camposClave);
		}
		
		String servicioConCamposClave = clave(request.servicio, camposClave);

		ConcurrentHashMap<String, ReentrantLock> mutexMapUsuario = mutexMap.get(idCobis);
		if (mutexMapUsuario == null) {
			mutexMapUsuario = new ConcurrentHashMap<>();
			mutexMap.put(idCobis, mutexMapUsuario);
		}

		ReentrantLock lock = mutexMapUsuario.computeIfAbsent(servicioConCamposClave, k -> new ReentrantLock());
		lock.lock();
        try {
            return responseRAW(request, camposClave);
        } finally {
            lock.unlock();
        }
	}

	public static ApiResponseMB responseRAW(ApiRequestMB request, Object... camposClave) {
//		Date inicio = new Date();
		String clave = clave(request.servicio, camposClave);

		try {
			request.header("uri", request.contexto.request.getRequestURI());
		} catch (Exception e) {
		}

		permitirSinLogin(request);
		
		if (!"true".equals(request.contexto.requestHeader("interno"))) {
			if (!esUsuarioLogueado(request) && !request.permitirSinLogin) {
				throw new RuntimeException(new IllegalAccessException());
			}

			if (request.requiereIdCobis && request.contexto.idCobis() == null) {
				throw new RuntimeException(new IllegalAccessException());
			}
		}

		if (request.method.equalsIgnoreCase("GET") && ConfigMB.bool("idproceso_url", false)) {
			request.query("idProceso", request.idProceso());
		}

		if (!ConfigMB.esProduccion() && "POST".equals(request.method) && request.url.contains("/v1/correoelectronico")) {
			String valor = request.body.string("para");
			if (!valor.contains("@hipotecario")) {
				SqlRequestMB sqlRequest = SqlMB.request("ConsultaEmailSmsDesarrollo", "hbs");
				sqlRequest.sql = "SELECT * FROM [hbs].[dbo].[email_sms_desarrollo] WHERE valor = ?";
				sqlRequest.add(valor);
				Integer cantidad = SqlMB.response(sqlRequest).registros.size();
				if (cantidad == 0) {
					return new ApiResponseMB();
				}
			}
		}
		if (!ConfigMB.esProduccion() && "POST".equals(request.method) && request.url.contains("/v1/notificaciones/sms")) {
			String valor = request.body.string("telefono");
			SqlRequestMB sqlRequest = SqlMB.request("ConsultaEmailSmsDesarrollo", "hbs");
			sqlRequest.sql = "SELECT * FROM [hbs].[dbo].[email_sms_desarrollo] WHERE valor = ?";
			sqlRequest.add(valor);
			Integer cantidad = SqlMB.response(sqlRequest).registros.size();
			if (cantidad == 0) {
				return new ApiResponseMB();
			}
		}

		if (request.cacheSesion) {
			String json = request.contexto.sesion().cache(clave);
			if (json == null) {
				json = request.contexto.cachePorRequest.get(clave);
			}
			if (json != null && !json.isEmpty()) {
				Integer codigoHttp = request.contexto.sesion().cacheHttp(clave);
				if (codigoHttp == null) {
					codigoHttp = request.contexto.cachePorRequestHttp.get(clave);
				}
				return new ApiResponseMB(null, codigoHttp != null ? codigoHttp : 200, json);
			}
		}

		if (request.cacheRedis) {
			String json = Redis.get(clave, String.class);
			if (json != null && !json.isEmpty()) {
				ApiResponseMB response = new ApiResponseMB(null, 200, json);
				if (request.habilitarLog) {
					log.info(request.log("REDIS"));
					log.info(response.log(request, "REDIS"));
				}
				return response;
			}
		}

		if (!ConfigMB.esOpenShift() && (request.contexto.parametros.bool("dummy", false) || request.dummy)) {
			String json = Archivo.leer(rutaDummy(request.servicio, clave));
			if (json != null) {
				ApiResponseMB response = new ApiResponseMB(null, 200, json);
				if (request.habilitarLog) {
					log.info(request.log("DUMMY"));
					log.info(response.log(request, "DUMMY"));
				}
				return response;
			}
		}

		try {
			if (!ConfigMB.esProduccion()) {
				String idCobis = request.contexto.idCobis();
				if (idCobis != null) {
					String servicioNormalizado = request.api + " " + request.method + " " + request.recurso;
					String servicioAlias = request.servicio;

					SqlResponseMB sqlResponse = CacheMB.mockServicios;
					if (sqlResponse != null) {
						for (Objeto registro : sqlResponse.registros) {
							Boolean aplicar = true;
							aplicar &= registro.string("idCobis").equals(idCobis);
							aplicar &= registro.string("servicio").equals(servicioNormalizado) || registro.string("servicio").equals(servicioAlias);
							if (!aplicar) {
								continue;
							}

							String condiciones = registro.string("condiciones");
							if (condiciones.isEmpty() || request.log().contains(condiciones)) {
								Integer httpCode = registro.integer("httpCode");
								String httpBody = registro.string("httpBody");
								ApiResponseMB response = new ApiResponseMB(null, httpCode, httpBody);
								return response;
							}
						}
					}
				}
			}
		} catch (Exception e) {
		}

		if (request.habilitarLog) {
			log.info(request.log());
		}

		Long inicio = new Date().getTime();
		try (Response httpResponse = http.newCall(request.build()).execute()) {
			Long fin = new Date().getTime();

			String head = "x-";
			if (Thread.currentThread().getName().equals(request.contexto.hiloPrincipal)) {
				head += "0";
			} else {
				Integer tid = Math.abs(Thread.currentThread().hashCode());
				head += String.format("%010d", tid);
			}
			head += "-api-" + request.servicio;
			head = head.replaceAll("[^a-zA-Z0-9_-]", "");
			request.contexto.mapaInvocaciones.put(head, (fin - inicio) + "ms");

			String json = httpResponse.body() != null ? httpResponse.body().string() : "";
			ApiResponseMB response = new ApiResponseMB(request, httpResponse.code(), json);

			Map<String, List<String>> headers = httpResponse.headers().toMultimap();
			for (String key : headers.keySet()) {
				String value = "";
				for (String localValue : headers.get(key)) {
					value += value.isEmpty() ? localValue : "; " + localValue;
				}
				response.headers.put(key, value);
			}

			if (request.habilitarLog) {
				log.info(response.log(request));
			}

			if (!request.habilitarLog && response.hayError()) {
				if (!"seguridad".equals(request.api)) {
					log.info(request.log());
				}
				log.info(response.log(request));
			}

			String cache404 = ConfigMB.string("mb_cache404", "");
			Set<String> cache404List = new HashSet<>(Arrays.asList(cache404.split(",")));
			Boolean cache404Response = cache404List.contains(request.servicio);

			if (request.cacheSesion && !response.hayError() && !json.isEmpty()) {
				request.contexto.cachePorRequest.put(clave, json);
				request.contexto.cachePorRequestHttp.put(clave, httpResponse.code());
				request.contexto.sesion().setCacheHttp(clave, json, httpResponse.code());
			} else if (request.cacheSesion && response.codigo == 404 && !json.isEmpty() && cache404Response) {
				request.contexto.cachePorRequest.put(clave, json);
				request.contexto.cachePorRequestHttp.put(clave, httpResponse.code());
				request.contexto.sesion().setCacheHttp(clave, json, httpResponse.code());
			} else if (request.cache204 && response.codigo == 204) {
				request.contexto.cachePorRequest.put(clave, "{}");
				request.contexto.cachePorRequestHttp.put(clave, httpResponse.code());
				request.contexto.sesion().setCacheHttp(clave, "{}", httpResponse.code());
			}

			if (request.cacheRedis && !response.hayError() && !json.isEmpty()) {
				Redis.set(clave, json);
			}

			if (!ConfigMB.esOpenShift() && !response.hayError() && !json.isEmpty()) {
				Archivo.escribir(rutaDummy(request.servicio, clave), json);
			}

			if (!ConfigMB.esProduccion()) {
				String subcodigo = response.hayError() && !response.string("codigo").isEmpty() ? ":" + response.string("codigo") : "";
				SqlRequestMB sqlRequest = SqlMB.request("InsertLog", "homebanking");
				sqlRequest.sql = "INSERT INTO [homebanking].[dbo].[log] VALUES (getdate(), ?, ?, ?, ?, ?, ?, ?)";
				sqlRequest.parametros.add(request.idProceso());
				sqlRequest.parametros.add(request.servicio);
				sqlRequest.parametros.add(request.idCobis());
				sqlRequest.parametros.add(response.codigo + subcodigo);
				sqlRequest.parametros.add(request.rawLog(false));
				sqlRequest.parametros.add(response.rawLog(request, false));
				sqlRequest.parametros.add(request.ip());
				new Futuro<SqlResponseMB>(() -> SqlMB.response(sqlRequest));
			}

//			String idLog = null;
//			if (Objeto.setOf("basic", "full").contains(Config.string("log_sql_modo"))) {
//				Date fin = new Date();
//				String subcodigo = response.hayError() && !response.string("codigo").isEmpty() ? ":" + response.string("codigo") : "";
//				try {
//					SqlRequest sqlRequest = Sql.request("InsertLogBasic", "visualizador");
//					sqlRequest.sql = "INSERT INTO [Visualizador].[dbo].[log_basic] (ba_eventId, ba_channelId, ba_subChannelId, ba_userId, ba_userIP, ba_sessionId, ba_operationId, ba_serviceId, ba_resultCode, ba_startTime, ba_endTime, ba_elapsedTime)";
//					sqlRequest.sql += " VALUES (NEWID(), 'HB', 'HB-LOG', ?, ?, ?, ?, ?, ?, ?, ?, ?);";
//					sqlRequest.sql += " SELECT SCOPE_IDENTITY() AS idLog";
//					sqlRequest.parametros.add(request.idCobis());
//					sqlRequest.parametros.add(request.ip().length() > 20 ? "IPV6" : request.ip());
//					sqlRequest.parametros.add(request.contexto.idSesion());
//					sqlRequest.parametros.add(request.idProceso());
//					sqlRequest.parametros.add("HB-" + request.servicio);
//					sqlRequest.parametros.add(response.codigo + subcodigo);
//					sqlRequest.parametros.add(new Timestamp(inicio.getTime()));
//					sqlRequest.parametros.add(new Timestamp(fin.getTime()));
//					sqlRequest.parametros.add(fin.getTime() - inicio.getTime());
//					SqlResponse sqlResponse = Sql.response(sqlRequest);
//					for (Objeto objeto : sqlResponse.registros) {
//						idLog = objeto.string("idLog");
//					}
//				} catch (Exception e) {
//				}
//			}

//			if (idLog != null && Objeto.setOf("full").contains(Config.string("log_sql_modo"))) {
//				try {
//					SqlRequest sqlRequest = Sql.request("InsertLogFull", "visualizador");
//					sqlRequest.sql = "INSERT INTO [Visualizador].[dbo].[log_xmlCanal] (xml_id_basic, xml_Entrada, xml_Salida, xml_startTime) VALUES (?, ?, ?, ?); ";
//					sqlRequest.parametros.add(idLog);
//					sqlRequest.parametros.add(Compresor.gzip("<api>\r\n" + request.rawLog(true).replace("&", "&amp;").replace("<", "&lt;") + "\r\n</api>"));
//					sqlRequest.parametros.add(Compresor.gzip("<api>\r\n" + response.rawLog(request, true).replace("&", "&amp;").replace("<", "&lt;") + "\r\n</api>"));
//					sqlRequest.parametros.add(new Timestamp(inicio.getTime()));
//					Sql.response(sqlRequest);
//				} catch (Exception e) {
//				}
//			}

			return response;
		} catch (Exception e) {
			if (!ConfigMB.esProduccion()) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				String mensaje = sw.toString();
				// TODO: LOG

				SqlRequestMB sqlRequest = SqlMB.request("InsertLog", "homebanking");
				sqlRequest.sql = "INSERT INTO [homebanking].[dbo].[log] VALUES (getdate(), ?, ?, ?, ?, ?, ?, ?)";
				sqlRequest.parametros.add(request.idProceso());
				sqlRequest.parametros.add(request.servicio);
				sqlRequest.parametros.add(request.idCobis());
				sqlRequest.parametros.add("ERROR");
				sqlRequest.parametros.add(request.rawLog(false));
				sqlRequest.parametros.add(mensaje);
				sqlRequest.parametros.add(request.ip());
				new Futuro<SqlResponseMB>(() -> SqlMB.response(sqlRequest));
			}

			if (!ConfigMB.bool("kibana", false)) {
				log.error(Texto.hora() + " idProceso: " + request.idProceso() + "", e);
			} else {
				Objeto registro = new Objeto();
				registro.set("idCobis", request.contexto.idCobis());
				registro.set("tipo", "error");
				registro.set("excepcion", e.getClass().getName());
				registro.set("idProceso", request.idProceso());
				registro.set("servicio", request.servicio);
				registro.set("timestamp", new Date().getTime());
				registro.set("fecha", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
				registro.set("hora", new SimpleDateFormat("HH:mm:ss").format(new Date()));
				registro.set("error", Texto.stackTrace(e));
				log.info(registro.toString().replace("\n", ""));
			}

			throw new ApiExceptionMB(e);
		}
	}

	/* ========== CACHE ========== */
	public static void eliminarCache(ContextoMB contexto, String servicio, Object... camposClave) {
		String clave = clave(servicio, camposClave);
		contexto.cachePorRequest.remove(clave);
		contexto.cachePorRequestHttp.remove(clave);
		contexto.sesion().delCache(clave);
		Redis.del(clave);
	}

	/* ========== UTIL ========== */
	private static String clave(String nombreServicio, Object... camposClave) {
		return camposClave.length == 0 ? nombreServicio : nombreServicio + "-" + Texto.unir("-", camposClave);
	}

	private static String rutaDummy(String nombreServicio, String clave) {
		String rutaDummy = ConfigMB.string("configuracion_carpeta_dummy");
		if (rutaDummy != null) {
			rutaDummy += ConfigMB.ambiente + "/" + nombreServicio + "/" + clave + ".json";
		}
		return rutaDummy;
	}
}