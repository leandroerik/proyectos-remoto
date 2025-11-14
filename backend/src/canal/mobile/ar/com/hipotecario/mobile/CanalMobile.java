package ar.com.hipotecario.mobile;

import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

import ar.com.hipotecario.backend.Servidor;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.HttpResponse;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Texto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.exception.MaximoReintentosException;
import ar.com.hipotecario.backend.exception.ParametrosIncorrectosException;
import ar.com.hipotecario.backend.exception.SesionExpiradaException;
import ar.com.hipotecario.backend.exception.SinSesionException;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Util;
import ar.gabrielsuarez.glib.G;

public class CanalMobile extends Servidor {

	public static String ESTADO = "estado";

	/* ========== ATRIBUTOS ========== */
	public static Map<String, Function<ContextoMB, Object>> endpoints = new HashMap<>();
	public static final ThreadLocal<ContextoMB> threadLocal = new ThreadLocal<>();
	public static final ConcurrentMap<String, LongAdder> mapaContador = new ConcurrentHashMap<>();
	public static final ConcurrentMap<String, LongAdder> mapaContadorOK = new ConcurrentHashMap<>();
	public static final ConcurrentMap<String, LongAdder> mapaContadorErrores = new ConcurrentHashMap<>();
	public static final ConcurrentMap<String, Futuro<Object>> superCache = new ConcurrentHashMap<>();

	/* ========== SERVIDOR ========== */
	private static Object servidor(ContextoMB contexto, Function<ContextoMB, Object> funcion, Boolean validarSesion, Boolean esPFWeb) {
		threadLocal.set(contexto);

		var objeto = new ar.com.hipotecario.mobile.lib.Objeto();
		String uriBack = contexto.request.getMethod() + ":" + contexto.request.getRequestURI();

		try {
			String idCobis = contexto.idCobis();
			String interno = contexto.requestHeader("interno");
			if (idCobis != null && !"true".equals(interno)) {
				Futuro<Object> cache = superCache.get(idCobis + ":" + uriBack);
				if (cache != null) {
					contexto.responseHeader("cache", "true");
					superCache.remove(idCobis + ":" + uriBack);
					HttpResponse response = (HttpResponse) cache.get();
					return contexto.response(response.code, response.body);
				}
			}

			Long inicio = new Date().getTime();
			Object object = funcion.apply(contexto);
			Long fin = new Date().getTime();
			mapaContador.computeIfAbsent(uriBack, k -> new LongAdder()).increment();
			if (object instanceof ar.com.hipotecario.mobile.lib.Objeto) {
				String estado = ((ar.com.hipotecario.mobile.lib.Objeto) object).string("estado");
				if (!"0".equals(estado)) {
					mapaContadorErrores.computeIfAbsent(uriBack, k -> new LongAdder()).increment();
					registrarError(contexto, (ar.com.hipotecario.mobile.lib.Objeto) object, null);
					try {
						if (uriBack.contains("login")) {
							contexto.eliminarSesion(true, contexto.idCobis());
						}
					} catch (Exception e) {
					}
				} else {
					mapaContadorOK.computeIfAbsent(uriBack, k -> new LongAdder()).increment();
				}
				if (!ConfigMB.esProduccion()) {
					try{
						System.out.println("[" + Fecha.horaSegundoActual() + "] REQUEST APP [cobis:" + contexto.idCobis() + "] " + uriBack + " " + contexto.parametros.toString().replace("\n", "").replace("\r", "").replace("  ", " ")
								+ "\n" + "RESPONSE APP " + object.toString().replace("\n", "").replace("\r", "").replace("    ", " ").replace("  ", " "));
					} catch (Exception e){}

					String ambiente = contexto.config.string("subambiente", ConfigMB.esARO() ? "ARO" : "ONPREM");
					new Futuro<Boolean>(() -> insertarTiempo(ambiente, uriBack, fin - inicio));
				}
			}
			return object;
		} catch (SesionExpiradaException e) {
			mapaContadorErrores.computeIfAbsent(uriBack, k -> new LongAdder()).increment();
			e.printStackTrace();
			registrarError(contexto, objeto.set("stackTrace", G.toString(e)), e);
			return contexto.response(200, new Objeto().set(ESTADO, "SESION_EXPIRADA"));
		} catch (MaximoReintentosException e) {
			mapaContadorErrores.computeIfAbsent(uriBack, k -> new LongAdder()).increment();
			e.printStackTrace();
			registrarError(contexto, objeto.set("stackTrace", G.toString(e)), e);
			return contexto.response(200, new Objeto().set(ESTADO, "MAXIMO_REINTENTOS"));
		} catch (SinSesionException e) {
			mapaContadorErrores.computeIfAbsent(uriBack, k -> new LongAdder()).increment();
			e.printStackTrace();
			registrarError(contexto, objeto.set("stackTrace", G.toString(e)), e);
			return contexto.response(200, new Objeto().set(ESTADO, "SIN_SESION"));
		} catch (ParametrosIncorrectosException e) {
			mapaContadorErrores.computeIfAbsent(uriBack, k -> new LongAdder()).increment();
			e.printStackTrace();
			registrarError(contexto, objeto.set("stackTrace", G.toString(e)), e);
			return contexto.response(200, new Objeto().set(ESTADO, "PARAMETROS_INCORRECTOS").set("mensaje", e.getMessage()));
		} catch (ApiException e) {
			mapaContadorErrores.computeIfAbsent(uriBack, k -> new LongAdder()).increment();
			e.printStackTrace();
			registrarError(contexto, objeto.set("stackTrace", G.toString(e)), e);
			if (e.request.api().equalsIgnoreCase("ventas")) {
				return contexto.response(500, new Objeto().set("ERROR", e.codigoError)).set("trace", ConfigMB.esProduccion() ? null : Texto.stackTrace(e));
			} else {
				return contexto.response(500, new Objeto().set(ESTADO, e.codigoError)).set("trace", ConfigMB.esProduccion() ? null : Texto.stackTrace(e));
			}
		} catch (SqlException e) {
			mapaContadorErrores.computeIfAbsent(uriBack, k -> new LongAdder()).increment();
			e.printStackTrace();
			registrarError(contexto, objeto.set("stackTrace", G.toString(e)), e);
			return contexto.response(500, new Objeto().set(ESTADO, e.codigoError)).set("trace", ConfigMB.esProduccion() ? null : Texto.stackTrace(e));
		} catch (Exception e) {
			mapaContadorErrores.computeIfAbsent(uriBack, k -> new LongAdder()).increment();
			e.printStackTrace();
			registrarError(contexto, objeto.set("stackTrace", G.toString(e)), e);
			if (e.getCause() != null && e.getCause() instanceof SocketTimeoutException) {
				return contexto.response(500, new Objeto().set(ESTADO, "TIMEOUT")).set("trace", ConfigMB.esProduccion() ? null : Texto.stackTrace(e));
			}
			return contexto.response(500, new Objeto().set(ESTADO, "ERROR")).set("trace", ConfigMB.esProduccion() ? null : Texto.stackTrace(e));
		} finally {
			try {
				if (!contexto.esProduccion()) {
					Integer cantidadMaxima = cantidadMaxima(contexto);
					insertarContadorInvocaciones(contexto, cantidadMaxima);
				}
				if ("true".equals(contexto.requestHeader("tiempos"))) {
					Integer cantidadMaxima = cantidadMaxima(contexto);
					Map<String, String> mapaInvocaciones = contexto.mapaInvocaciones;
					for (String key : mapaInvocaciones.keySet()) {
						contexto.responseHeader(key, mapaInvocaciones.get(key));
					}
					contexto.responseHeader("z-max-serial", cantidadMaxima.toString());
				}
			} catch (Throwable t) {
			}
		}
	}

	public static Integer cantidadMaxima(ContextoMB contexto) {
		Map<String, Integer> cantidades = new HashMap<>();

		for (String key : contexto.mapaInvocaciones.keySet()) {
			if (!key.contains("-")) {
				continue;
			}
			String hilo = key.split("-")[1];
			cantidades.put(hilo, cantidades.getOrDefault(hilo, 0) + 1);
		}

		Integer cantidadMaxima = 0;
		for (Integer cantidad : cantidades.values()) {
			cantidadMaxima = Math.max(cantidadMaxima, cantidad);
		}
		return cantidadMaxima;
	}
	
	private static Boolean insertarContadorInvocaciones(ContextoMB contexto, Integer cantidadMaxima) {
		SqlRequestMB request = SqlMB.request("InsertarTiempos", "hbs");
		request.sql = """
				INSERT INTO [hbs].[dbo].[contador_invocaciones]
				(canal, subcanal, idCobis, servicio, cantidad, fecha)
				VALUES (?, ?, ?, ?, ?, ?)
				""";
		request.add("HB");
		request.add("-");
		request.add(contexto.idCobis());
		request.add(contexto.request.getRequestURI());
		request.add(cantidadMaxima);
		request.add(new Date());
		new Futuro<SqlResponseMB>(() -> SqlMB.response(request));
		return true;
	}

	private static Boolean insertarTiempo(String ambiente, String url, Long tiempo) {
		SqlRequestMB request = SqlMB.request("InsertarTiempos", "mobile");
		request.sql = "INSERT INTO [mobile].[dbo].[log_tiempos_mobile] (ambiente, endpoint, tiempo) VALUES (?, ?, ?)";
		request.add(ambiente);
		request.add(url);
		request.add(tiempo);
		SqlResponseMB response = SqlMB.response(request);
		return response.hayError;
	}

	public static void registrarError(ContextoMB contexto, ar.com.hipotecario.mobile.lib.Objeto response, Exception exception) {
		if (!ConfigMB.bool("activar_log_errores", false)) {
			registrarErrorKibana(contexto, response, exception);
			return;
		}
		try {
			String estado = response.string("estado");
			Boolean estadoValido = false;
			estadoValido |= estado.equals("NO_POSEE_CAMPANA_PREAPROBADA");
			estadoValido |= estado.equals("DOCUMENTACION_AL_DIA");
			estadoValido |= estado.equals("SIN_CUENTA_COMITENTE");
			estadoValido |= estado.equals("NO_TIENE_ACCESOS_BIOMETRIA");
			estadoValido |= estado.equals("ACCOUNT_OFFICER_NO_ENCONTRADO");
			estadoValido |= estado.equals("VENCIDO");
			estadoValido |= estado.equals("ERROR_TIENE_DISP_REGISTRADOS");
			estadoValido |= estado.equals("OPCION_NO_HABILITADA");
			estadoValido |= estado.equals("FUERA_HORARIO");
			estadoValido |= estado.equals("NO_TIENE_ACCESS_ACTIVOS_BIOMETRIA");
			estadoValido |= estado.equals("-1");
			if (estadoValido) {
				return;
			}

			String uri = contexto.request.getRequestURI();
			Boolean ignorar = false;
			if (uri != null) {
				ignorar |= uri.endsWith("/api/validar-id-dispositivo");
				if (ignorar) {
					return;
				}
			}

			Set<String> parametrosOfuscar = new HashSet<>();
			parametrosOfuscar.add("clave");
			parametrosOfuscar.add("usuario");
			parametrosOfuscar.add("importe");
			parametrosOfuscar.add("monto");

			Objeto parametros = new Objeto();
			Map<String, Object> mapaParametros = contexto.parametros.toMap();
			for (String parametro : mapaParametros.keySet()) {
				Boolean ofuscar = false;
				for (String parametroOfuscar : parametrosOfuscar) {
					ofuscar |= parametro.contains(parametroOfuscar);
				}
				parametros.set(parametro, !ofuscar ? mapaParametros.get(parametro) : "***");
			}

			String resultado = exception == null ? response.string("estado") : exception.getClass().getSimpleName();
			if (resultado == null) {
				resultado = "";
			}
			if (resultado.length() > 30) {
				resultado = resultado.substring(0, 30);
			}
			resultado = "ERROR".equals(resultado) || resultado.toUpperCase().contains("EXCEPTION") ? "500" : "422";

			ApiRequestMB apiRequest = ApiMB.request("auditor", "auditor", "POST", "/v1/reportes", contexto);
			apiRequest.permitirSinLogin = true;
			apiRequest.habilitarLog = false;
			apiRequest.body.set("canal", "MB");
			apiRequest.body.set("subCanal", "BACUNI");
			apiRequest.body.set("usuario", contexto.sesion().idCobis());
			apiRequest.body.set("idProceso", Util.idProceso());
			apiRequest.body.set("sesion", String.valueOf(contexto.sesion().id().hashCode()).replace("-", ""));
			apiRequest.body.set("servicio", "API-MB" + contexto.request.getRequestURI().replace('/', '_'));
			apiRequest.body.set("resultado", resultado);
			apiRequest.body.set("duracion", 0L);
			var mensajes = apiRequest.body.set("mensajes");
			mensajes.set("entrada", parametros.toJson());
			mensajes.set("salida", response.toJson());
			ApiMB.response(apiRequest);
		} catch (Exception e) {
		}
	}

	public static void registrarErrorKibana(ContextoMB contexto, ar.com.hipotecario.mobile.lib.Objeto response, Exception exception) {
		if (!contexto.esOpenShift()) {
			return;
		}
		try {
			String estado = response.string("estado");
			Boolean estadoValido = false;
			estadoValido |= estado.equals("NO_POSEE_CAMPANA_PREAPROBADA");
			estadoValido |= estado.equals("DOCUMENTACION_AL_DIA");
			estadoValido |= estado.equals("SIN_CUENTA_COMITENTE");
			estadoValido |= estado.equals("NO_TIENE_ACCESOS_BIOMETRIA");
			estadoValido |= estado.equals("ACCOUNT_OFFICER_NO_ENCONTRADO");
			estadoValido |= estado.equals("VENCIDO");
			estadoValido |= estado.equals("ERROR_TIENE_DISP_REGISTRADOS");
			estadoValido |= estado.equals("OPCION_NO_HABILITADA");
			estadoValido |= estado.equals("FUERA_HORARIO");
			estadoValido |= estado.equals("NO_TIENE_ACCESS_ACTIVOS_BIOMETRIA");
			estadoValido |= estado.equals("-1");
			if (estadoValido) {
				return;
			}

			String uri = contexto.request.getRequestURI();
			Boolean ignorar = false;
			if (uri != null) {
				ignorar |= uri.endsWith("/api/validar-id-dispositivo");
				if (ignorar) {
					return;
				}
			}

			Set<String> parametrosOfuscar = new HashSet<>();
			parametrosOfuscar.add("clave");
			parametrosOfuscar.add("usuario");
			parametrosOfuscar.add("importe");
			parametrosOfuscar.add("monto");

			Objeto parametros = new Objeto();
			Map<String, Object> mapaParametros = contexto.parametros.toMap();
			for (String parametro : mapaParametros.keySet()) {
				Boolean ofuscar = false;
				for (String parametroOfuscar : parametrosOfuscar) {
					ofuscar |= parametro.contains(parametroOfuscar);
				}
				parametros.set(parametro, !ofuscar ? mapaParametros.get(parametro) : "***");
			}

			String resultado = exception == null ? response.string("estado") : exception.getClass().getSimpleName();
			if (resultado == null) {
				resultado = "";
			}
			if (resultado.length() > 30) {
				resultado = resultado.substring(0, 30);
			}
			resultado = "ERROR".equals(resultado) || resultado.toUpperCase().contains("EXCEPTION") ? "500" : "422";

			Objeto datos = new Objeto();
			datos.set("canal", "HB");
			datos.set("subCanal", "BACUNI");
			datos.set("usuario", contexto.idCobis());
			datos.set("sesion", String.valueOf(contexto.idSesion().hashCode()).replace("-", ""));
			datos.set("uriBack", contexto.request.getRequestURI());
			datos.set("duracion", 0L);
			datos.set("entrada", parametros);
			datos.set("salida", response.toMap());

			System.out.println(datos.toSimpleJson());
		} catch (Exception e) {
		}
	}

	/* ========== METODOS ========== */
	protected static void get(String url, Function<ContextoMB, Object> funcion) {
		endpoints.put("GET:" + url, funcion);
		Servidor.get("mobile", url, contexto -> servidor(contexto, funcion, true, false), ContextoMB.class);
	}

	protected static void post(String url, Function<ContextoMB, Object> funcion) {
		endpoints.put("POST:" + url, funcion);
		Servidor.post("mobile", url, contexto -> servidor(contexto, funcion, true, false), ContextoMB.class);
	}

	protected static void put(String url, Function<ContextoMB, Object> funcion) {
		endpoints.put("PUT:" + url, funcion);
		Servidor.put("mobile", url, contexto -> servidor(contexto, funcion, true, false), ContextoMB.class);
	}

	protected static void patch(String url, Function<ContextoMB, Object> funcion) {
		endpoints.put("PATCH:" + url, funcion);
		Servidor.patch("mobile", url, contexto -> servidor(contexto, funcion, true, false), ContextoMB.class);
	}

	protected static void delete(String url, Function<ContextoMB, Object> funcion) {
		endpoints.put("DELETE:" + url, funcion);
		Servidor.delete("mobile", url, contexto -> servidor(contexto, funcion, true, false), ContextoMB.class);
	}
}
