package ar.com.hipotecario.backend.base;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Servidor;

public class Version {

	public static Objeto get() {
		File path1 = new File("/app/.git/");
		File path2 = new File(System.getProperty("user.dir"), "../.git/");
		File path = path1.exists() ? path1 : path2.exists() ? path2 : null;
		Objeto respuesta = new Objeto();

		String tag = "";
		String hash = "";
		String shortHash = "";
		for (String linea : lineas(path, "HEAD")) {
			if (linea.contains("ref")) {
				tag = linea.substring(linea.lastIndexOf('/') + 1);
			} else {
				hash = linea;
			}
		}
		for (String linea : lineas(path, "packed-refs")) {
			if (!hash.isEmpty() && linea.contains(hash)) {
				tag = linea.substring(linea.lastIndexOf('/') + 1);
//				break;
			}
			if (!tag.isEmpty() && linea.contains("/" + tag)) {
				hash = linea.split(" ")[0];
//				break;
			}
		}
		if (hash.length() > 7) {
			shortHash = hash.substring(0, 7);
		}

		String buildTime = "";
		for (String linea : lineas(path, "BUILD_TIME")) {
			buildTime = linea;
		}

		respuesta.set("version", tag);
		respuesta.set("hash", shortHash);
		respuesta.set("full-hash", hash);
		respuesta.set("build", buildTime);
		respuesta.set("inicio-pod", Servidor.tiempoInicio);
		return respuesta;
	}

	private static List<String> lineas(File base, String value) {
		try {
			File file = new File(base.getAbsolutePath(), value);
			if (file.exists()) {
				return Files.readAllLines(file.toPath());
			}
		} catch (Exception e) {
		}
		return new ArrayList<>();
	}
}
