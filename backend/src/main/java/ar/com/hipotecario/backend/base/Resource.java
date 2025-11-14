package ar.com.hipotecario.backend.base;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/** @author Gabriel Suarez */
public class Resource {

	/* ========== ATRIBUTOS ========== */
	private static Map<String, byte[]> resources = new HashMap<>();

	/* ========== METODOS ESTATICOS ========== */
	public static Boolean isJar() {
		String protocol = Thread.currentThread().getContextClassLoader().getResource("").getProtocol();
		return "jar".equals(protocol);
	}

	public static URL url(String path) {
		URL url = Thread.currentThread().getContextClassLoader().getResource(path);
		if (url == null) {
			url = Thread.currentThread().getContextClassLoader().getResource("BOOT-INF/classes/" + path);
		}
		return url;
	}

	public static InputStream stream(String path) {
		InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		if (stream == null) {
			Thread.currentThread().getContextClassLoader().getResourceAsStream("BOOT-INF/classes/" + path);
		}
		return stream;
	}

	public static byte[] bytes(String path) {
		if (resources.get(path) == null) {
			byte[] buffer = new byte[8192];
			try (InputStream is = stream(path)) {
				if (is != null) {
					try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
						for (int length = is.read(buffer); length > 0; length = is.read(buffer)) {
							os.write(buffer);
						}
						resources.put(path, os.toByteArray());
					}
				}
			} catch (Exception e) {
				return null;
			}
		}
		return resources.get(path);
	}

	public static String base64(String path) {
		byte[] bytes = bytes(path);
		if (bytes != null) {
			return Base64.getEncoder().encodeToString(bytes);
		}
		return null;
	}

	public static Archivo archivo(String path) {
		return new Archivo(new File(path).getName(), Resource.bytes(path));
	}

	public static Archivo archivo(String path, String mime) {
		Archivo archivo = new Archivo(new File(path).getName(), Resource.bytes(path));
		archivo.mime = mime;
		return archivo;
	}
}
