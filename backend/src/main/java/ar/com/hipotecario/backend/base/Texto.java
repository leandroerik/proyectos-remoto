package ar.com.hipotecario.backend.base;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Texto {

	/* ========== URL ========== */
	public static String urlEncode(String url) {
		try {
			return url != null ? URLEncoder.encode(url, "UTF-8") : "";
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	/* ========== EXCEPTION ========== */
	public static String stackTrace(Throwable e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String stackTrace = sw.toString();
		return stackTrace;
	}

	public static String primeraMayuscula(String texto) {
		String primeraMayuscula = "";
		primeraMayuscula += texto.length() > 0 ? texto.substring(0, 1).toUpperCase() : "";
		primeraMayuscula += texto.length() > 1 ? texto.substring(1).toLowerCase() : "";
		return primeraMayuscula;
	}
}
