package ar.com.hipotecario.mobile.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public abstract class Archivo {

	/* ========== UTIL ========== */
	public static Boolean existe(String ruta) {
		Boolean existe = new File(ruta).exists();
		return existe;
	}

	/* ========== LECTURA ========== */
	public static String leer(String ruta) {
		File archivo = new File(ruta);
		try (Scanner scanner = new Scanner(archivo)) {
			scanner.useDelimiter("\\Z");
			String contenido = scanner.next();
			return contenido;
		} catch (NoSuchElementException e) {
			return "";
		} catch (FileNotFoundException e) {
			return null;
		}
	}

	public static byte[] leerBinario(String ruta) {
		Path path = Paths.get(ruta);
		try {
			return Files.readAllBytes(path);
		} catch (Exception e) {
			return null;
		}
	}

	/* ========== ESCRITURA ========== */
	public static void escribir(String ruta, String contenido) {
		new File(ruta).getParentFile().mkdirs();
		try (PrintWriter archivo = new PrintWriter(ruta)) {
			archivo.write(contenido);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static void agregar(String ruta, String linea) {
		try {
			List<String> lineas = new ArrayList<>();
			lineas.add(linea);
			Files.write(Paths.get(ruta), lineas, StandardCharsets.UTF_8, StandardOpenOption.APPEND, StandardOpenOption.CREATE);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
