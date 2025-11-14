package ar.com.hipotecario.canal.homebanking.insomnia;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ar.gabrielsuarez.glib.G;

public class InsomniaHB {

	public static void main(String[] args) {
		String carpeta = carpeta("/hb/api/campanas");
		System.out.println(carpeta);
	}

	public static Map<String, String> carpetas() {
		String ruta = new File(G.sourcePath(), "/ar/com/hipotecario/homebanking/ApiHomebanking.java").getAbsolutePath();
		String contenido = G.readFile(ruta);
		List<String> lineas = Arrays.asList(contenido.split("\\R"));

		Map<String, String> comentarios = new LinkedHashMap<>();
		for (String linea : lineas) {
			if (linea.trim().startsWith("// ")) {
				String comentario = linea.substring(4).trim();
				comentarios.put(comentario, "fld_" + G.md5(UUID.randomUUID().toString()).toLowerCase());
			}
		}
		return comentarios;
	}

	public static String carpeta(String endpoint) {
		String ruta = new File(G.sourcePath(), "/ar/com/hipotecario/homebanking/ApiHomebanking.java").getAbsolutePath();
		String contenido = G.readFile(ruta);
		List<String> lineas = Arrays.asList(contenido.split("\\R"));

		String comentario = "";
		for (String linea : lineas) {
			if (linea.trim().startsWith("// ")) {
				comentario = linea.substring(4).trim();
			}
			if (linea.contains("\"" + endpoint + "\"")) {
				return comentario;
			}
		}
		return "";
	}
}
