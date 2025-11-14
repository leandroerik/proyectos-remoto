package ar.com.hipotecario.backend.conector.api;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Sesion;
import ar.com.hipotecario.backend.ThreeScale;
import ar.com.hipotecario.backend.ThreeScale.Token;
import ar.com.hipotecario.backend.base.Base;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.HttpRequest;
import ar.com.hipotecario.backend.base.HttpResponse;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.api.Api.Log;
import ar.com.hipotecario.backend.servicio.sql.SqlHomeBanking;
import ar.com.hipotecario.backend.servicio.sql.homebanking.LogsMonitor.LogMonitor;

public class ApiRequest extends Base {

	private static Logger log = LoggerFactory.getLogger(ApiRequest.class);

	/* ========== ATRIBUTOS ESTATICOS ========== */
	public static Config config = new Config();
	public static Boolean habilitar3scale = config.bool("backend_3scale", false);
	private static SecureRandom secureRandom = new SecureRandom();

	/* ========== ATRIBUTOS ========== */
	public Contexto contexto;
	public String servicio;
	private String api;
	private String metodo;
	private String url;
	private Map<String, String> headers = new LinkedHashMap<>();
	private Map<String, String> paths = new LinkedHashMap<>();
	private Map<String, String> querys = new LinkedHashMap<>();
	private Objeto body = new Objeto();

	/* ========== CACHE ========== */
	public Boolean cache = false;
	public List<Object> parametrosCache = new ArrayList<>();

	/* ========== CONSTRUCTORES ========== */
	public ApiRequest(String servicio, String api, String metodo, String url, Contexto contexto) {
		this.contexto = contexto;
		this.servicio = servicio;
		this.api = api;
		this.metodo = metodo;
		this.url = url;
		this.headers = headers();
	}

	/* ========== IDPROCESO ========== */
	private String generarIdProceso() {
		Integer random = secureRandom.nextInt(Integer.MAX_VALUE - 1) + 1;
		return random.toString();
	}

	/* ========== HEADERS ========== */
	private Map<String, String> headers() {
		StringBuilder log1 = new StringBuilder();
		append(log1, "estoy en linea 69");
		append(log1, "LA VARIABLE HABILITAR 3SCALE ESTA EN: " + habilitar3scale);
		//log.info("LINEA 71 de token, LA VARIABLE HABILITAR 3SCALE ESTA EN: " + habilitar3scale);

		//System.out.println("ENTRE EN METODO HEADERS LINEA 68");
		//System.out.println("LA VARIABLE HABILITAR 3SCALE ESTA EN: " + habilitar3scale);

		Sesion sesion = contexto.sesion();
		String usuario = sesion.idCobis;
		usuario = empty(usuario) || usuario.contains("-") ? sesion.cuil : usuario;

		String sucursal = sesion.sucursal;
		String idProceso = generarIdProceso();

		Map<String, String> headers = new LinkedHashMap<>();
		headers.put("x-idSesion", contexto.idSesion());
		headers.put("x-idProceso", idProceso);

		//System.out.println("estoy linea 80 metodo headers");

		headers.put("x-usuarioIP", contexto.ip());
		if (!api.equals("ventas")) {
			if (api.equals("veps")) {
				headers.put("x-usuario", validarContexto("x-usuario"));
				headers.put("x-subCanal", validarContexto("x-canal"));

			} else {
				headers.put("x-usuario", usuario);
				headers.put("x-subCanal", contexto.subCanal());
			}
			headers.put("x-canal", contexto.canal());
			//System.out.println("ENTRE EN METODO HEADERS LINEA 100 if");
			append(log1, "ENTRE EN METODO HEADERS LINEA 101 if");

			headers.put("x-operador", null);
			headers.put("x-sucursal", sucursal);
			headers.put("x-Sistema", contexto.canal());
			
			//if("altaCuentas".equals(servicio) && "inversiones".equals(api)) {
			if("relaciones".equals(servicio) && "productos".equals(api)) {
				headers.put("x-canal", "BBANK");
				headers.put("x-Sistema", "BBANK");
			}
			
		} else {
			//System.out.println("ENTRE EN METODO HEADERS LINEA 107 - else");
			append(log1, "ENTRE EN METODO HEADERS LINEA 107 if-ELSE");

			headers.put("X-Usuario", contexto.usuarioCanal());
			headers.put("X-Canal", contexto.canalVenta());
			headers.put("X-Subcanal", contexto.subCanalVenta());
			headers.put("X-Token", idProceso);
			headers.put("X-Ambiente", contexto.canal());
			headers.put("X-Starttime", Fecha.ahora().string("yyyy-MM-dd'T'HH:mm:ss"));
			headers.put("X-Handle", idProceso);
			headers.put("X-Sistema", contexto.canal());

		}
		if (habilitar3scale) {
			Token token = ThreeScale.token(contexto.canal(), api);
			append(log1, "Estoy linea 124 entro X EL HABILITAR , el token es: " + token.accessToken);
			log.info("ENTRO EN EL IF 126 DE SCALE");
			headers.put("Authorization", "Bearer " + token.accessToken);
		}
		log.toString();
		return headers;
	}

	private String validarContexto(String cadena) {
		return !Util.empty(contexto.requestHeader(cadena)) ? contexto.requestHeader(cadena) : "";
	}

	public String api() {
		return api;
	}

	public String metodo() {
		return metodo;
	}

	public String url() {
		return url;
	}

	public String idProceso() {
		return this.headers.get("x-idProceso");
	}

	public String handle() {
		return !api.equals("ventas") ? idProceso() : this.headers.get("X-Handle");
	}

	/* ========== METODOS ========== */
	public void header(String clave, String valor) {
		header(clave, valor, true, false);
	}

	public void header(String clave, String valor, Boolean cache, Boolean enmascarado) {
		addIf(cache, parametrosCache, valor);
		this.headers.put(clave, valor);
	}

	public void path(String clave, String valor) {
		path(clave, valor, true, false);
	}

	public void path(String clave, String valor, Boolean cache, Boolean enmascarado) {
		addIf(cache, parametrosCache, valor);
		this.paths.put(clave, valor);
	}

	public void query(String clave, Object valor) {
		query(clave, valor, true, false);
	}

	public void query(String clave, Object valor, Boolean cache, Boolean enmascarado) {
		if (valor != null) {
			addIf(cache, parametrosCache, valor);
			this.querys.put(clave, valor.toString());
		}
	}

	public void body(String clave, Object valor) {
		body(clave, valor, true, false);
	}

	public void body(String clave, Object valor, Boolean cache, Boolean enmascarado) {
		addIf(cache, parametrosCache, valor);
		this.body.set(clave, valor);
	}

	public void bodyIfNotNull(String clave, String valor) {
		bodyIfNotNull(clave, valor, true, false);
	}

	public void bodyIfNotNull(String clave, Object valor, Boolean cache, Boolean enmascarado) {
		if (valor != null) {
			addIf(cache, parametrosCache, valor);
			this.body.set(clave, valor);
		}
	}

	public void body(Objeto valor) {
		body(valor, true, false);
	}

	public void body(Objeto valor, Boolean cache, Boolean enmascarado) {
		addIf(cache, parametrosCache, valor);
		this.body = valor;
	}

	/* ========== EJECUCION ========== */
	public String fullUrl() {
		String url = contexto.config.string("backend_api_" + api) + this.url;
		if (habilitar3scale) {
			url = contexto.config.string("backend_3scale_" + api).trim().split(" ")[0] + this.url;
		}
		return url;
	}

	public HttpRequest httpRequest() {
		String url = contexto.config.string("backend_api_" + api) + this.url;
		if (habilitar3scale) {
			url = contexto.config.string("backend_3scale_" + api).trim().split(" ")[0] + this.url;
		}
		HttpRequest httpRequest = new HttpRequest(metodo, url);
		httpRequest.headers.putAll(this.headers);
		httpRequest.paths.putAll(this.paths);
		httpRequest.querys.putAll(this.querys);
		httpRequest.body = this.body;
		return httpRequest;
	}

	public ApiResponse ejecutar() {
//		contexto.parametros.set("mocks", true);

		Boolean mocks = contexto.parametros.bool("mocks", false);

		Boolean kibana = contexto.config.bool("kibana");
		HttpRequest httpRequest = httpRequest();

		if (!contexto.esProduccion() && mocks) {
			// MOCKS ESPECIFICO
//			String jsonMock = Archivo.leer(rutaMocks(servicio, parametrosCache));
//			
//			if(jsonMock == null) {
//				jsonMock = Archivo.leer(rutaMocks(servicio, null));
//				// MOCKS GENERAL
//			}

//			if(jsonMock != null) {
//				HttpResponse httpResponse = new HttpResponse(jsonMock);
//				
//				if(Api.habilitarLog) {
//					log.info(log(httpResponse, false , kibana, mocks));
//				}
//				registrarLogMonitor(httpResponse, httpRequest, null);
//				
//				ApiResponse response = new ApiResponse(this, httpResponse);
//				return response;
//			}
		}

		if (Api.habilitarLog) {
			log.info(log(httpRequest, kibana));
		}

		String datosCache = cache ? Api.getCache(contexto, servicio, parametrosCache) : null;
		HttpResponse httpResponse = datosCache == null ? httpRequest.run() : new HttpResponse(datosCache.substring(0, 3), datosCache.substring(4));
		if (Api.habilitarLog) {
			log.info(log(httpResponse, datosCache != null, kibana, false));
		}

		if (cache && datosCache == null) {
			Api.setCache(contexto, servicio, httpResponse.code + ":" + httpResponse.body, parametrosCache);
		}

		registrarLogMonitor(httpResponse, httpRequest, datosCache);

		// if (!contexto.esProduccion() && !apiResponse.body.isEmpty() &&
		// !apiResponse.hayError()) {
//			Archivo.escribir(rutaMocks(servicio, parametrosCache), apiResponse.body);
//			Archivo.escribir(rutaMocks(servicio, null), apiResponse.body); // ACTUALIZAMOS EL GENERAL 
//		}

		return new ApiResponse(this, httpResponse);
	}
	public ApiResponse ejecutar(String valor) {
//		contexto.parametros.set("mocks", true);

		Boolean mocks = contexto.parametros.bool("mocks", false);

		Boolean kibana = contexto.config.bool("kibana");
		HttpRequest httpRequest = httpRequest();

		if (!contexto.esProduccion() && mocks) {
			// MOCKS ESPECIFICO
//			String jsonMock = Archivo.leer(rutaMocks(servicio, parametrosCache));
//
//			if(jsonMock == null) {
//				jsonMock = Archivo.leer(rutaMocks(servicio, null));
//				// MOCKS GENERAL
//			}

//			if(jsonMock != null) {
//				HttpResponse httpResponse = new HttpResponse(jsonMock);
//
//				if(Api.habilitarLog) {
//					log.info(log(httpResponse, false , kibana, mocks));
//				}
//				registrarLogMonitor(httpResponse, httpRequest, null);
//
//				ApiResponse response = new ApiResponse(this, httpResponse);
//				return response;
//			}
		}

		if (Api.habilitarLog) {
			log.info(log(httpRequest, kibana));
		}

		String datosCache = cache ? Api.getCache(contexto, servicio, parametrosCache) : null;
		HttpResponse httpResponse = datosCache == null ? httpRequest.run() : new HttpResponse(datosCache.substring(0, 3), datosCache.substring(4));
		if (Api.habilitarLog) {
			log.info(log(httpResponse, datosCache != null, kibana, false));
		}

		if (cache && datosCache == null) {
			Api.setCache(contexto, servicio, httpResponse.code + ":" + httpResponse.body, parametrosCache);
		}

		registrarLogMonitor(httpResponse, httpRequest, datosCache);

		//		if (!contexto.esProduccion() && !apiResponse.body.isEmpty() && !apiResponse.hayError()) {
//			Archivo.escribir(rutaMocks(servicio, parametrosCache), apiResponse.body);
//			Archivo.escribir(rutaMocks(servicio, null), apiResponse.body); // ACTUALIZAMOS EL GENERAL
//		}
		httpResponse.body=valor;
		httpResponse.code=200;

		return new ApiResponse(this, httpResponse);
	}

	/* ========== LOG ========== */
	public void registrarLogMonitor(HttpResponse httpResponse, HttpRequest httpRequest, String datosCache) {
		if (!contexto.esProduccion() && !"true".equals(System.getenv("ARO"))) {
			LogMonitor logMonitor = new LogMonitor();
			logMonitor.proceso = Integer.valueOf(idProceso());
			logMonitor.servicio = this.servicio;
			logMonitor.cobis = contexto.sesion().idCobis;
			logMonitor.codigo_respuesta = String.valueOf(httpResponse.code);
			logMonitor.request = log(httpRequest, false);
			logMonitor.response = log(httpResponse, datosCache != null, false, false);
			logMonitor.ip = contexto.ip();
			SqlHomeBanking.registrarLogMonitor(contexto, logMonitor);
		}
	}

	public String log(HttpRequest httpRequest, Boolean kibana) {
		StringBuilder log = new StringBuilder();
		append(log, "API REQUEST [%s, Proceso: %s]\n", servicio, idProceso());
		append(log, "%s %s\n", metodo, httpRequest.url());
		for (String clave : headers.keySet()) {
			if (headers.get(clave) != null) {
				append(log, "%s: %s\n", clave, headers.get(clave));
			}
		}
		if (!"GET".equals(metodo)) {
			append(log, headers.isEmpty() ? "" : "\n");
			append(log, "%s\n", body);
		}

		if (kibana) {
			Log json = new Log();
			json.canal = contexto.canal();
			json.idCobis = contexto.sesion().idCobis;
			json.tipo = "request";
			json.servicio = this.servicio;
			json.idProceso = idProceso();
			json.request = log.toString();
			return json.toString();
		} else {
			return Fecha.ahora().string("[HH:mm:ss] ") + log.toString();
		}
	}

	public String log(HttpResponse httpResponse, Boolean cache, Boolean kibana, Boolean mocks) {
		StringBuilder log = new StringBuilder();
		append(log, "API RESPONSE [%s, Proceso: %s, Http: %s]%s\n", servicio, idProceso(), httpResponse.code, mocks ? "( MOKC )" : (cache ? " (CACHE)" : ""));
		try {
			append(log, "%s\n", Objeto.fromJson(httpResponse.body).toJson());
		} catch (Exception e) {
			append(log, "%s\n", httpResponse.body);
		}
		if (kibana) {
			Log json = new Log();
			json.canal = contexto.canal();
			json.cuit = contexto.sesion().cuil;
			json.idCobis = contexto.sesion().idCobis;
			json.tipo = "response";
			json.servicio = this.servicio;
			json.idProceso = idProceso();
			json.http = String.valueOf(httpResponse.code);
			json.response = log.toString();
			return json.toString();
		} else {
			return Fecha.ahora().string("[HH:mm:ss] ") + log.toString().replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t");
		}
	}

	public static String rutaMocks(String nombreServicio, List<Object> parametros) {
		String rutaMock = new Config().string("directorio_mocks");

		String claves = "";

		if (parametros != null) {
			for (Integer i = 0; i < parametros.size(); i++) {
				if (parametros.get(i).equals(""))
					continue;

				claves += "_" + parametros.get(i);
			}
		}

		String rutaClave = nombreServicio + claves + ".json";

		return rutaMock != null ? rutaMock += Config.ambiente() + "/" + nombreServicio + "/" + rutaClave : rutaMock;
	}
}
