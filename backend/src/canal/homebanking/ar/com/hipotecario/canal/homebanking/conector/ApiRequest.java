package ar.com.hipotecario.canal.homebanking.conector;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ApiRequest {

	private static MediaType JSON = MediaType.get("application/json; charset=utf-8");

	/* ========== ATRIBUTOS ========== */
	public String servicio;
	public String api;
	public String method;
	public String url;
	public Map<String, String> headers = new LinkedHashMap<>();
	public Objeto body = new Objeto();
	public String recurso;

	/* ========== GENERAL ========== */
	public ContextoHB contexto;
	public String urlEnmascarada;
	public Boolean habilitarLog = !ConfigHB.esOpenShift();

	/* ========== PERMISOS ========== */
	public Boolean permitirSinLogin = false;
	public Boolean requiereIdCobis = false;

	/* ========== CACHE ========== */
	public Boolean cacheRedis = false;
	public Boolean cacheSesion = false;
	public Boolean cacheRequest = false;
	public Boolean cache204 = ConfigHB.bool("cache204", true);
	public Boolean dummy = false;
	public String archivoDummy;

	/* ========== GET ========== */
	public String idCobis() {
		return contexto.idCobis();
	}

	public String idProceso() {
		return headers.get("x-idProceso");
	}

	public String ip() {
		return contexto.ip();
	}

	public Objeto body() {
		return body;
	}

	/* ========== SET ========== */
	public String header(String clave, String valor) {
		return headers.put(clave, valor);
	}

	public void path(String clave, String valor) {
		if (clave != null && valor != null) {
			url = url.replace("{" + clave + "}", valor);
			String valorEnmascarado = Util.enmascarar(servicio, clave, valor);
			urlEnmascarada = urlEnmascarada == null ? url : urlEnmascarada;
			urlEnmascarada = urlEnmascarada.replace("{" + clave + "}", valorEnmascarado);
		}
	}

	public void query(String clave, String valor) {
		if (urlEnmascarada == null) {
			urlEnmascarada = url;
		}
		url += (url.contains("?") ? "&" : "?") + Texto.escaparUrl(clave) + "=" + Texto.escaparUrl(valor);
		String valorEnmascarado = Util.enmascarar(servicio, clave, valor);
		urlEnmascarada = urlEnmascarada == null ? url : urlEnmascarada;
		urlEnmascarada += (urlEnmascarada.contains("?") ? "&" : "?") + Texto.escaparUrl(clave) + "=" + Texto.escaparUrl(valorEnmascarado);
	}

	public Objeto body(Objeto objeto) {
		body = objeto;
		return body;
	}

	public Objeto body(String clave) {
		return body.set(clave);
	}

	public Objeto body(String clave, Object valor) {
		return body.set(clave, valor);
	}

	public Objeto body(String clave, List<Objeto> valor) {
		if (valor == null) {
			return body.set(clave, valor);
		}

		Objeto objeto = new Objeto();
		for (Objeto item : valor) {
			objeto.add(item);
		}
		return body.set(clave, objeto);
	}

	public Objeto add(String clave, Object valor) {
		return body.add(clave, valor);
	}

	/* ========== BUILDER ========== */
	public Request build() {
		Request.Builder builder = new Request.Builder();
		builder.url(url);
		for (String clave : headers.keySet()) {
			if (headers.get(clave) != null) {
				builder.addHeader(clave, headers.get(clave));
			}
		}
		if ("GET".equals(method)) {
			builder.get();
		} else {
			builder.method(method, RequestBody.create(body.toJson(), JSON));
		}
		return builder.build();
	}

	/* ========== LOG ========== */
	public String log() {
		return log(null);
	}

	public String log(String origen) {
		Boolean kibana = ConfigHB.bool("kibana", false);
		StringBuilder log = new StringBuilder();

		if (!kibana) {
			log.append(Texto.hora()).append(" API REQUEST ");
			if (origen != null) {
				log.append("(").append(origen).append(")");
			}
			log.append(" [").append(servicio).append(", idCobis: ").append(idCobis()).append(", idProceso: ").append(idProceso()).append("]\n");
		}

		log.append(method).append(" ").append(urlEnmascarada != null ? urlEnmascarada : url).append("\n");
		for (String clave : headers.keySet()) {
			if (headers.get(clave) != null) {
				//log.append(clave).append(": ").append(headers.get(clave)).append("\n");
				if (clave.contains("clave") || clave.contains("pass") || clave.contains("pin") || clave.contains("token")) {
					log.append(clave).append(": ").append("***").append("\n");
				} else {
					log.append(clave).append(": ").append(headers.get(clave)).append("\n");
				}
			}
		}
		if (!"GET".equals(method)) {
			log.append("\n").append(Util.enmascarar(servicio, body)).append("\n");
		}

		if (kibana) {
			Objeto registro = new Objeto();
			registro.set("idCobis", idCobis());
			registro.set("tipo", "request");
			registro.set("servicio", servicio);
			registro.set("idProceso", idProceso());
			registro.set("origen", origen);
			registro.set("numeroSolicitud", headers.get("X-Handle"));
			registro.set("fecha", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			registro.set("request", log.toString());
			if (Objeto.setOf("UsuarioIDG", "ValidarUsuarioIDG", "ValidarClaveIDG", "Persona", "Cliente").contains(servicio)) {
				registro.set("userAgent", contexto.request.userAgent());
			}
			return registro.toString().replace("\n", "");
		}

		return log.toString();
	}

	public String rawLog(Boolean encriptar) {
		StringBuilder log = new StringBuilder();

		log.append(Texto.hora()).append(" API REQUEST ");
		log.append(" [").append(servicio).append(", idCobis: ").append(idCobis()).append(", idProceso: ").append(idProceso()).append("]\r\n");

		log.append(method).append(" ").append(url).append("\r\n");
		for (String clave : headers.keySet()) {
			if (headers.get(clave) != null) {
				log.append(clave).append(": ").append(headers.get(clave)).append("\r\n");
			}
		}
		if (!"GET".equals(method)) {
			log.append("\n").append(body).append("\r\n");
		}
		return log.toString();
	}
}
