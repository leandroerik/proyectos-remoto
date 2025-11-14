package ar.com.hipotecario.backend.base;

import java.util.LinkedHashMap;
import java.util.Map;

/** @author Gabriel Suarez */
public class HttpResponse {

	/* ========== ATRIBUTOS ========== */
	public Integer code;
	public Map<String, String> headers = new LinkedHashMap<>();
	public String body;

	/* ========== CONSTRUCTOR ========== */
	public HttpResponse() {
	}

	public HttpResponse(String body) {
		this.code = 200;
		this.body = body;
	}

	public HttpResponse(Integer code, String body) {
		this.code = code;
		this.body = body;
	}

	public HttpResponse(String code, String body) {
		this.code = Integer.parseInt(code);
		this.body = body;
	}

	/* ========== METODOS ========== */
		public Objeto jsonBody() {
		Objeto objeto = Objeto.fromJson(body);
		return objeto;
	}

	/* ========== LOG ========== */
	public String log() {
		StringBuilder log = new StringBuilder();
		try {
			log.append(Objeto.fromJson(body).toJson()).append("\n");
		} catch (Exception e) {
			log.append(body).append("\n");
		}
		return log.toString();
	}
}
