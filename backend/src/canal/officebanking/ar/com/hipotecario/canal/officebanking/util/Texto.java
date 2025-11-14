package ar.com.hipotecario.canal.officebanking.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public abstract class Texto {

	public static String extension(String nombreArchivo) {
		int lastIndexOf = nombreArchivo.lastIndexOf('.');
		if (lastIndexOf != -1) {
			return nombreArchivo.substring(lastIndexOf + 1).toLowerCase();
		}
		return "";
	}

	public static String mimeType(String nombreArchivo) {
		String mime = URLConnection.guessContentTypeFromName(nombreArchivo);
		return mime != null ? mime : "";
	}

	public static String hora() {
		return "[" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "]";
	}

	public static String primeraMayuscula(String texto) {
		String primeraMayuscula = "";
		primeraMayuscula += texto.length() > 0 ? texto.substring(0, 1).toUpperCase() : "";
		primeraMayuscula += texto.length() > 1 ? texto.substring(1).toLowerCase() : "";
		return primeraMayuscula;
	}

	public static String primerasMayuscula(String texto) {
		String primerasMayuscula = "";
		for (String parte : texto.split(" ")) {
			primerasMayuscula += primeraMayuscula(parte) + " ";
		}
		return primerasMayuscula.trim();
	}

	public static String unir(String separador, Object... textos) {
		String unido = "";
		for (Object texto : textos) {
			if (texto != null) {
				try {
					unido += unido.isEmpty() ? texto.toString() : separador + texto;
				} catch (Exception e) {
				}
			}
		}
		return unido;
	}

	public static String escaparUrl(String url) {
		try {
			return url != null ? URLEncoder.encode(url, "UTF-8") : "";
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String desescaparUrl(String url) {
		try {
			return url != null ? URLDecoder.decode(url, "UTF-8") : "";
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String desescaparUTF8(String texto) {
		try {
			Properties properties = new Properties();
			properties.load(new StringReader("clave=" + texto));
			return properties.getProperty("clave");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String stackTrace(Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String stackTrace = sw.toString();
		return stackTrace;
	}

	public static String toBase64(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}

	public static String substring(String texto, Integer cantidad) {
		if (texto == null || texto.isEmpty()) {
			return "";
		} else {
			return texto.substring(0, Math.min(cantidad, texto.length()));
		}
	}

	public static String htmlToText(String texto) {
		texto = texto.replace("&aacute;", "á");
		texto = texto.replace("&eacute;", "é");
		texto = texto.replace("&iacute;", "í");
		texto = texto.replace("&oacute;", "ó");
		texto = texto.replace("&uacute;", "ú");
		texto = texto.replace("&ntilde;", "ñ");
		texto = texto.replace("&Ntilde;", "Ñ");
		texto = texto.replace("&Aacute;", "Á");
		texto = texto.replace("&Eacute;", "É");
		texto = texto.replace("&Iacute;", "Í");
		texto = texto.replace("&Oacute;", "Ó");
		texto = texto.replace("&Uacute;", "Ú");
		texto = texto.replace("<br/>", "\n\r");
		texto = texto.replace("<br>", "\n\r");
		texto = texto.replace("<b>", "");
		texto = texto.replace("</b>", "");
		texto = texto.replace("&quot;", "'");
		texto = texto.replace("&deg;", "°");
		texto = texto.replace("<ul class=\"mb-0 list-unstyled pl-4\">", "");
		texto = texto.replace("</ul>", "\n\r");
		texto = texto.replace("<li>", "");
		texto = texto.replace("</li>", "");

		return texto;
	}

	public static String textToHtml(String texto) {
		texto = texto.replace("á", "&aacute;");
		texto = texto.replace("é", "&eacute;");
		texto = texto.replace("í", "&iacute;");
		texto = texto.replace("ó", "&oacute;");
		texto = texto.replace("ú", "&uacute;");
		texto = texto.replace("ñ", "&ntilde;");
		texto = texto.replace("Ñ", "&Ntilde;");
		texto = texto.replace("Á", "&Aacute;");
		texto = texto.replace("É", "&Eacute;");
		texto = texto.replace("Í", "&Iacute;");
		texto = texto.replace("Ó", "&Oacute;");
		texto = texto.replace("Ú", "&Uacute;");
		return texto;
	}

	public static String subCadenaDesdeRegex(String texto, String regex) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(texto);
		if (m.find()) {
			int position = m.start();
			return StringUtils.substring(texto, Math.addExact(position, 1), texto.length());
		}

		return "";
	}
}
