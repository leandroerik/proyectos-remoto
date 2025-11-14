package ar.com.hipotecario.backend.base;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Properties {

	/* ========== ATRIBUTOS ESTATICOS ========== */
	private static Map<String, Map<String, String>> cache = new HashMap<>();

	/* ========== METODOS ESTATICOS ========== */
	public static Map<String, String> toMap(String ruta) {
		if (!cache.containsKey(ruta)) {
			try {
				java.util.Properties properties = new java.util.Properties();
				try (InputStream is = Properties.class.getResourceAsStream("/" + ruta)) {
					properties.load(is);
				}
				Map<String, String> mapa = new HashMap<>();
				for (Object clave : properties.keySet()) {
					mapa.put(clave.toString(), properties.getProperty(clave.toString()).trim());
				}
				cache.put(ruta, mapa);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return cache.get(ruta);
	}
}
