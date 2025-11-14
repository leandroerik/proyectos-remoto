package ar.com.hipotecario.canal.homebanking.conector;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.com.hipotecario.backend.Eventos;
import ar.com.hipotecario.backend.ThreeScale;
import ar.com.hipotecario.backend.ThreeScale.Token;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.CacheHB;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.excepcion.ApiException;
import ar.com.hipotecario.canal.homebanking.lib.Archivo;
import ar.com.hipotecario.canal.homebanking.lib.Http;
import ar.com.hipotecario.canal.homebanking.lib.Redis;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class Api {

	private static Map<String, Boolean> errores = new ConcurrentHashMap<>();
	private static Map<String, Integer> contadorErrores = new ConcurrentHashMap<>();
	public static List<String> tiempos = new CopyOnWriteArrayList<>();

	/* ========== SYNC CACHE ========== */
	private static Map<String, ConcurrentHashMap<String, ReentrantLock>> mutexMap = new ConcurrentHashMap<>();

	/* ========== ATRIBUTOS ========== */
	private static OkHttpClient http = Http.okHttpClient();
	private static Logger log = LoggerFactory.getLogger(Api.class);

	/* ========== REQUEST ========== */
	public static ApiRequest request(String servicio, String api, String metodo, String recurso, ContextoHB contexto) {
		return request(servicio, api, metodo, recurso, contexto, null);
	}

	public static ApiRequest request(String servicio, String api, String metodo, String recurso, ContextoHB contexto, Boolean tscale) {
		String idProceso = Util.idProceso();

		ApiRequest apiRequest = new ApiRequest();
		apiRequest.api = api;
		apiRequest.servicio = servicio;
		apiRequest.method = metodo;
		apiRequest.url = ConfigHB.string("api_url_" + api) + recurso;
		apiRequest.headers.put("Content-Type", "application/json; charset=utf-8");
		apiRequest.recurso = recurso;

		if (!api.equals("ventas_windows")) {
			apiRequest.headers.put("x-cobis", contexto.idCobis());
			apiRequest.headers.put("x-usuario", contexto.idCobis());
			apiRequest.headers.put("x-idSesion", String.valueOf(contexto.idSesion().hashCode()).replace("-", ""));
			apiRequest.headers.put("x-canal", "HB");
			apiRequest.headers.put("x-subCanal", contexto.config.string("subcanal", ConfigHB.esARO() ? "ARO" : "BACUNI"));
			apiRequest.headers.put("x-operador", null);
			apiRequest.headers.put("x-usuarioIP", ip(contexto));
			apiRequest.headers.put("x-idProceso", idProceso);
			apiRequest.headers.put("x-Sistema", "HB");
			try {
				if (contexto.sesion.canal != null) {
					apiRequest.headers.put("x-canal", contexto.sesion.canal.toUpperCase());
					apiRequest.headers.put("x-subCanal", contexto.sesion.canal.toUpperCase());
					apiRequest.headers.put("x-Sistema", contexto.sesion.canal.toUpperCase());
				}
			} catch (Exception e) {
			}
		}

		if (api.equals("ventas_windows")) {
			apiRequest.headers.put("x-cobis", contexto.idCobis());
			apiRequest.headers.put("x-idSesion", String.valueOf(contexto.idSesion().hashCode()).replace("-", ""));
			apiRequest.headers.put("x-idProceso", idProceso);
			apiRequest.headers.put("x-usuarioIP", ip(contexto));

			apiRequest.headers.put("X-Usuario", ConfigHB.string("configuracion_usuario"));
			apiRequest.headers.put("X-Canal", "26");
			apiRequest.headers.put("X-Subcanal", "0");
			apiRequest.headers.put("X-Token", idProceso);
			apiRequest.headers.put("X-Ambiente", "HB");
			apiRequest.headers.put("X-Starttime", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").format(new Date()));
			apiRequest.headers.put("X-Handle", idProceso);
			apiRequest.headers.put("X-Sistema", "HB");
		}

		if (!contexto.esProduccion()) {
			apiRequest.headers.put("x-thread", "" + Thread.currentThread().getName().hashCode());
		}

//		String usuarioOAG = ConfigHB.string("oag_usuario") + "@bh.com.ar";
//		String passwordOAG = ConfigHB.string("oag_clave");
//		apiRequest.headers.put("Authorization", "Basic " + Http.authBasic(usuarioOAG, passwordOAG));

		if(api.equals("postventa") && ConfigHB.esHomologacion()){
			tscale = true;
		}

		if (tscale == null) {
			tscale = "true".equals(ConfigHB.string("backend_3scale", "false"));
		}

		if (tscale) {
			String threeScale = ConfigHB.string("backend_3scale_" + api, null);
			if (threeScale != null) {
				String url = threeScale.split(" ")[0];
				Token token = ThreeScale.token(contexto.canal(), api);
				if (token != null) {
					apiRequest.url = url + recurso;
					apiRequest.headers.put("Authorization", "Bearer " + token.accessToken);
				}
			}
		}

		apiRequest.contexto = contexto;
		return apiRequest;
	}

	private static String ip(ContextoHB contexto) {
		return contexto.ip().startsWith("fe80:") 
				|| contexto.ip().equals("[0:0:0:0:0:0:0:1]")  ? "127.0.0.1" : contexto.ip();
	}

	/* ========== RESPONSE ========== */
	private static Map<String, Boolean> serviciosRegistrados = new ConcurrentHashMap<>();

	/**
	 * El metodo que realmente llama al servicio es responseRAW()
	 * Este es un metodo envoltorio que se encarga de ordenar los hilos para evitar doble llamadas a servicios.
	 * El funcionamiento basico es el siguiente:
	 *  - Cuando un usuario llama a un servicio cacheable se ejecuta un LOCK para ese servicio+parametros
	 *  - Si otro hilo quiere llamara al mismo servicio+parametros el hilo se bloquea hasta que el primero termine
	 *  - Una vez que el primer hilo termina la llamada a este servicio en concreto, el hilo actual se ejecuta
	 *  - Cuando se ejecuta el hilo actual, los datos ya estan en cache y se evita la llamada al servicio
	 */
	public static ApiResponse response(ApiRequest request, Object... camposClave) {
		ContextoHB contexto = request.contexto;

		if (!ConfigHB.bool("hb_habilitar_concurrencia", true)) {
			return responseRAW(request, camposClave);
		}

		String idCobis = contexto.idCobis();

		if ((!request.cacheSesion && !request.cacheRequest) || idCobis == null) {
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

	public static ApiResponse responseRAW(ApiRequest request, Object... camposClave) {
		try {
			ContextoHB contexto = request.contexto;
			if (!contexto.esProduccion()) {
				String uriFront = "GET:" + contexto.request.headers("uri");
				String uriBack = contexto.request.requestMethod() + ":" + contexto.request.uri();
				String uriMW = request.api + ":" + request.method + ":" + request.recurso;
				String clave = uriFront + " | " + uriBack + " | " + uriMW;

				if (!serviciosRegistrados.containsKey(clave)) {
					serviciosRegistrados.put(clave, true);
					SqlRequest sqlRequest = Sql.request("RegistrarServicios", "homebanking");
					sqlRequest.sql += "INSERT INTO [Homebanking].[dbo].[servicios_mw] ";
					sqlRequest.sql += "([uriFront],[uriBack],[uriMW])";
					sqlRequest.sql += "VALUES (?, ?, ?)";
					sqlRequest.parametros.add(uriFront);
					sqlRequest.parametros.add(uriBack);
					sqlRequest.parametros.add(uriMW);
					new Futuro<SqlResponse>(() -> Sql.response(sqlRequest));
				}
			}
		} catch (Throwable t) {
		}

		String clave = clave(request.servicio, camposClave);

		try {
			request.header("uri", request.contexto.request.uri());
		} catch (Exception e) {
		}

		if (!request.contexto.sesion.usuarioLogueado && !request.permitirSinLogin) {
			throw new RuntimeException(new IllegalAccessException());
		}

		if (request.requiereIdCobis && request.contexto.idCobis() == null) {
			throw new RuntimeException(new IllegalAccessException());
		}

		if (!ConfigHB.esProduccion() && "POST".equals(request.method) && request.url.contains("/v1/correoelectronico")) {
			String valor = request.body.string("para");
			if (!valor.contains("@hipotecario")) {
				SqlRequest sqlRequest = Sql.request("ConsultaEmailSmsDesarrollo", "hbs");
				sqlRequest.sql = "SELECT * FROM [hbs].[dbo].[email_sms_desarrollo] WHERE valor = ?";
				sqlRequest.add(valor);
				Integer cantidad = Sql.response(sqlRequest).registros.size();
				if (cantidad == 0) {
					return new ApiResponse();
				}
			}
		}
		if (!ConfigHB.esProduccion() && "POST".equals(request.method) && request.url.contains("/v1/notificaciones/sms")) {
			String valor = request.body.string("telefono");
			SqlRequest sqlRequest = Sql.request("ConsultaEmailSmsDesarrollo", "hbs");
			sqlRequest.sql = "SELECT * FROM [hbs].[dbo].[email_sms_desarrollo] WHERE valor = ?";
			sqlRequest.add(valor);
			Integer cantidad = Sql.response(sqlRequest).registros.size();
			if (cantidad == 0) {
				return new ApiResponse();
			}
		}

		if (request.cacheSesion) {
			String json = request.contexto.sesion.cache.get(clave);
			if (json != null && !json.isEmpty()) {
				Integer codigoHttp = request.contexto.sesion.cacheHttp.get(clave);
				return new ApiResponse(null, codigoHttp != null ? codigoHttp : 200, json);
			}
		}

		if (request.cacheRequest) {
			String json = request.contexto.cacheRequest.get(clave);
			if (json != null && !json.isEmpty()) {
				return new ApiResponse(null, 200, json);
			}
		}

		if (request.cacheRedis) {
			String json = Redis.get(clave, String.class);
			if (json != null && !json.isEmpty()) {
				ApiResponse response = new ApiResponse(null, 200, json);
				if (request.habilitarLog) {
					log.info(request.log("REDIS"));
					log.info(response.log(request, "REDIS"));
				}
				return response;
			}
		}

		if (!ConfigHB.esOpenShift() && (request.contexto.parametros.bool("dummy", false) || request.dummy)) {
			String json = Archivo.leer(rutaDummy(request.servicio, clave));
			if (json != null) {
				ApiResponse response = new ApiResponse(null, 200, json);
				if (request.habilitarLog) {
					log.info(request.log("DUMMY"));
					log.info(response.log(request, "DUMMY"));
				}
				return response;
			}
		}

		if (!ConfigHB.esOpenShift() && request.archivoDummy != null) {
			String json = Archivo.leer(request.archivoDummy);
			if (json != null) {
				ApiResponse response = new ApiResponse(null, 200, json);
				if (request.habilitarLog) {
					log.info(request.log("DUMMY"));
					log.info(response.log(request, "DUMMY"));
				}
				return response;
			}
		}

		try {
			if (!ConfigHB.esProduccion()) {
				String idCobis = request.contexto.idCobis();
				if (idCobis != null) {
					String servicioNormalizado = request.api + " " + request.method + " " + request.recurso;
					String servicioAlias = request.servicio;

					SqlResponse sqlResponse = CacheHB.mockServicios;
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
								ApiResponse response = new ApiResponse(null, httpCode, httpBody);
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

			String momento = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
			String uriFront = "GET:" + request.contexto.request.headers("uri");
			String uriBack = request.contexto.request.requestMethod() + ":" + request.contexto.request.uri();
			String uriMW = request.api + ":" + request.method + ":" + request.recurso;

			// logActivo solo si la fecha es anterior a 2024-09-20 12:00:00 // 
			Boolean logActivo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(ConfigHB.string("log_activo", "2024-09-20 12:00:00")).after(new Date());

			if (request.contexto.idCobis() != null && logActivo) {
				SimpleDateFormat formato = new SimpleDateFormat("HH:mm:ss.SSS");
				String tiempoInicio = formato.format(new Date(inicio));
				String tiempoFin = formato.format(new Date(fin));
				String idSesion = String.valueOf(request.contexto.idSesion().hashCode()).replace("-", "");
				String idCobis = request.contexto.idCobis();
				String descripcion = idCobis + " | " + idSesion + " | " + uriMW + " | " + tiempoInicio + " | " + tiempoFin + " | " + request.idProceso();
				tiempos.add(descripcion);
			}

			String evento = momento + ": " + uriFront + " | " + uriBack + " | " + uriMW + " | " + (fin - inicio) + "ms";
			Eventos.registrar(request.idCobis(), evento);

			try {
				String head = "x-";
				if (Thread.currentThread().getName().equals(request.contexto.hiloPrincipal)) {
					head += "0";
				} else {
					Integer tid = Math.abs(Thread.currentThread().hashCode());
					head += String.format("%010d", tid);
				}
				head += "-api-" + request.servicio.replace(" ", "-");
				head = head.replaceAll("[^a-zA-Z0-9_-]", "");
				if (request.contexto.mapaInvocaciones.containsKey(head)) {
					String texto = Long.toHexString(Double.doubleToLongBits(Math.random())).substring(8, 13);
					head += "-" + texto;
				}
				request.contexto.mapaInvocaciones.put(head, (fin - inicio) + "ms");
			} catch (Throwable t) {
			}

			String json = httpResponse.body() != null ? httpResponse.body().string() : "";
			ApiResponse response = new ApiResponse(request, httpResponse.code(), json);

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

			if (request.cacheSesion && !response.hayError() && !json.isEmpty()) {
				request.contexto.sesion.cache.put(clave, json);
				request.contexto.sesion.cacheHttp.put(clave, response.codigo);
			} else if (request.cacheSesion && response.codigo == 404 && !json.isEmpty()) {
				request.contexto.sesion.cache.put(clave, json);
				request.contexto.sesion.cacheHttp.put(clave, response.codigo);
			} else if (request.cacheSesion && response.codigo == 204) {
				request.contexto.sesion.cache.put(clave, "{}");
				request.contexto.sesion.cacheHttp.put(clave, response.codigo);
			}

			if (request.cacheRequest && !response.hayError() && !json.isEmpty()) {
				request.contexto.cacheRequest.put(clave, json);
			} else if (request.cacheRequest && response.codigo == 404 && !json.isEmpty()) {
				request.contexto.cacheRequest.put(clave, json);
			} else if (request.cacheRequest && response.codigo == 204) {
				request.contexto.cacheRequest.put(clave, "{}");
			}

			if (request.cacheRedis && !response.hayError() && !json.isEmpty()) {
				Redis.set(clave, json);
			}

			if (!ConfigHB.esOpenShift() && !response.hayError() && !json.isEmpty()) {
				Archivo.escribir(rutaDummy(request.servicio, clave), json);
			}

			if (!ConfigHB.esProduccion()) {
				String subcodigo = response.hayError() && !response.string("codigo").isEmpty() ? ":" + response.string("codigo") : "";
				SqlRequest sqlRequest = Sql.request("InsertLog", "homebanking");
				sqlRequest.sql = "INSERT INTO [homebanking].[dbo].[log] VALUES (getdate(), ?, ?, ?, ?, ?, ?, ?)";
				sqlRequest.parametros.add(request.idProceso());
				sqlRequest.parametros.add(request.servicio);
				sqlRequest.parametros.add(request.idCobis());
				sqlRequest.parametros.add(response.codigo + subcodigo);
				sqlRequest.parametros.add(request.rawLog(false));
				sqlRequest.parametros.add(response.rawLog(request, false));
				sqlRequest.parametros.add(request.ip());
				new Futuro<SqlResponse>(() -> Sql.response(sqlRequest));
			}

			try {
				if (!ConfigHB.esProduccion()) {
					String key = "" + request.servicio + ":" + response.codigo.toString() + ":" + response.string("codigo");

					String keyext = key;
					if (response.string("mensajeAlUsuario").startsWith("[sp")) {
						keyext += ":" + response.string("mensajeAlUsuario").hashCode();
					}

					if (!contadorErrores.containsKey(key)) {
						contadorErrores.put(key, 0);
					}

					Boolean insertar = true;
					insertar &= !response.string("codigo").isEmpty();
					insertar &= !response.string("mensajeAlUsuario").isEmpty();
					insertar &= !response.string("mensajeAlDesarrollador").isEmpty();
					insertar &= !errores.containsKey(keyext);
					insertar &= contadorErrores.get(key) < 30;

					if (insertar) {
						errores.put(keyext, true);
						contadorErrores.put(key, contadorErrores.get(key) + 1);

						String codigo = response.string("codigo");
						if (response.string("mensajeAlUsuario").startsWith("[sp")) {
							codigo += ":" + response.string("mensajeAlUsuario").hashCode();
						}

						String sql = "";
						sql += " INSERT INTO [Homebanking].[dbo].[log_errores] (servicio, http, codigo, mensajeAlUsuario, masInformacion, mensajeAlDesarrollador, detalle, recurso)";
						sql += " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

						SqlRequest sqlRequest = Sql.request("InsertLogError", "homebanking");
						sqlRequest.sql = sql;
						sqlRequest.parametros.add(request.servicio);
						sqlRequest.parametros.add(response.codigo.toString());
						sqlRequest.parametros.add(Texto.substring(codigo, 250));
						sqlRequest.parametros.add(Texto.substring(response.string("mensajeAlUsuario"), 250));
						sqlRequest.parametros.add(Texto.substring(response.string("masInformacion"), 250));
						sqlRequest.parametros.add(Texto.substring(response.string("mensajeAlDesarrollador"), 250));
						sqlRequest.parametros.add(Texto.substring(response.string("detalle"), 250));
						if (request.urlEnmascarada != null) {
							sqlRequest.parametros.add(Texto.substring("API " + request.api + " - " + request.method + " " + request.urlEnmascarada, 250));
						} else {
							sqlRequest.parametros.add(Texto.substring("API " + request.api + " - " + request.method + " " + request.url, 250));
						}
						new Futuro<SqlResponse>(() -> Sql.response(sqlRequest));
					}
				}
			} catch (Exception e) {
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
			if (!ConfigHB.esProduccion()) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				String mensaje = sw.toString();
				// TODO: LOG

				SqlRequest sqlRequest = Sql.request("InsertLog", "homebanking");
				sqlRequest.sql = "INSERT INTO [homebanking].[dbo].[log] VALUES (getdate(), ?, ?, ?, ?, ?, ?, ?)";
				sqlRequest.parametros.add(request.idProceso());
				sqlRequest.parametros.add(request.servicio);
				sqlRequest.parametros.add(request.idCobis());
				sqlRequest.parametros.add("ERROR");
				sqlRequest.parametros.add(request.rawLog(false));
				sqlRequest.parametros.add(mensaje);
				sqlRequest.parametros.add(request.ip());
				new Futuro<SqlResponse>(() -> Sql.response(sqlRequest));
			}

			if (!ConfigHB.bool("kibana", false)) {
				log.error(Texto.hora() + " idProceso: " + request.idProceso() + "", e);
			} else {
				Objeto registro = new Objeto();
				registro.set("idCobis", request.contexto.idCobis());
				registro.set("tipo", "error");
				registro.set("excepcion", e.getClass().getName());
				registro.set("idProceso", request.idProceso());
				registro.set("servicio", request.servicio);
				registro.set("fecha", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				registro.set("error", Texto.stackTrace(e));
				log.info(registro.toString().replace("\n", ""));
			}

			throw new ApiException(e);
		}
	}

	/* ========== CACHE ========== */
	public static void eliminarCache(ContextoHB contexto, String servicio, Object... camposClave) {
		String clave = clave(servicio, camposClave);
		contexto.sesion.cache.remove(clave);
		contexto.sesion.cacheHttp.remove(clave);
		Redis.del(clave);
	}

	/* ========== UTIL ========== */
	private static String clave(String nombreServicio, Object... camposClave) {
		return camposClave.length == 0 ? nombreServicio : nombreServicio + "-" + Texto.unir("-", camposClave);
	}

	private static String rutaDummy(String nombreServicio, String clave) {
		String rutaDummy = ConfigHB.string("configuracion_carpeta_dummy");
		if (rutaDummy != null) {
			rutaDummy += ConfigHB.ambiente + "/" + nombreServicio + "/" + clave + ".json";
		}
		return rutaDummy;
	}
}
