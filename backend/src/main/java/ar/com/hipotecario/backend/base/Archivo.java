package ar.com.hipotecario.backend.base;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class Archivo {

	/* ========== ATRIBUTOS ESTATICOS ========== */
	private static Map<String, String> mapMimeType = mapMimeType();

	/* ========== ATRIBUTOS ========== */
	public String nombre;
	public byte[] bytes;
	public String mime;

	/* ========== CONSTRUCTORES ========== */
	public Archivo(String ruta) {
		Path path = Paths.get(ruta);
		try {
			nombre = path.getFileName().toString();
			bytes = Files.readAllBytes(path);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Archivo(String nombre, byte[] bytes) {
		this.nombre = nombre;
		this.bytes = bytes;
	}

	public Archivo(String nombre, String base64) {
		this.nombre = nombre;
		this.bytes = Base64.getDecoder().decode(base64);
	}

	/* ========== METODOS ========== */
	public String string() {
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public static byte[] leerBinario(String ruta) {
		Path path = Paths.get(ruta);
		try {
			return Files.readAllBytes(path);
		} catch (Exception e) {
			return null;
		}
	}

	public String extension() {
		return extension(nombre);
	}

	public static String extension(String nombreArchivo) {
		return extension(nombreArchivo, false);
	}

	public static String extension(String nombreArchivo, Boolean mantenerPunto) {
		if (nombreArchivo != null) {
			int i = nombreArchivo.lastIndexOf('.');
			if (i >= 0) {
				String extension = nombreArchivo.substring(i + (mantenerPunto ? 0 : 1)).trim();
				return extension;
			}
		}
		return "";
	}

	public String mime() {
		return mime == null ? mime(nombre) : mime;
	}

	public static String mime(String nombreArchivo) {
		if (nombreArchivo != null) {
			String extension = extension(nombreArchivo);
			String mimeType = mapMimeType.get(extension);
			if (mimeType == null) {
				mimeType = "application/octet-stream";
			}
			return mimeType;
		}
		return "";
	}

	public String base64() {
		String base64 = Base64.getEncoder().encodeToString(bytes);
		return base64;
	}

	public static Boolean guardar(String base64, String path) {
		File outputFile = new File(path);
		try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
			outputStream.write(Base64.getDecoder().decode(base64));
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static void escribir(String ruta, String contenido) {
		new File(ruta).getParentFile().mkdirs();
		try (PrintWriter archivo = new PrintWriter(ruta)) {
			archivo.write(contenido);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static void escribir(String ruta, byte[] contenido) {
		try (FileOutputStream fos = new FileOutputStream(ruta)) {
			fos.write(contenido);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Archivo comprimirImagen(Integer calidad) {
		try {
			BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(extension().toLowerCase());
			ImageWriter writer = (ImageWriter) writers.next();

			ImageOutputStream ios = ImageIO.createImageOutputStream(os);
			writer.setOutput(ios);

			ImageWriteParam parametros = writer.getDefaultWriteParam();
			if (parametros.canWriteCompressed()) {
				parametros.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
				parametros.setCompressionQuality(calidad);
			}
			writer.write(null, new IIOImage(bufferedImage, null, null), parametros);

			byte[] imagenComprimida = os.toByteArray();
			return new Archivo(nombre, imagenComprimida);
		} catch (Exception e) {
			return this;
		}
	}

	/* ========== INICIALIZAR ========== */
	private static Map<String, String> mapMimeType() {
		Map<String, String> mapa = new HashMap<>();
		mapa.put("aac", "audio/aac");
		mapa.put("abw", "application/x-abiword");
		mapa.put("arc", "application/octet-stream");
		mapa.put("avi", "video/x-msvideo");
		mapa.put("azw", "application/vnd.amazon.ebook");
		mapa.put("bin", "application/octet-stream");
		mapa.put("bz", "application/x-bzip");
		mapa.put("bz2", "application/x-bzip2");
		mapa.put("csh", "application/x-csh");
		mapa.put("css", "text/css");
		mapa.put("csv", "text/csv");
		mapa.put("doc", "application/msword");
		mapa.put("epub", "application/epub+zip");
		mapa.put("gif", "image/gif");
		mapa.put("htm", "text/html");
		mapa.put("html", "text/html");
		mapa.put("ico", "image/x-icon");
		mapa.put("ics", "text/calendar");
		mapa.put("jar", "application/java-archive");
		mapa.put("jpeg", "image/jpeg");
		mapa.put("jpg", "image/jpeg");
		mapa.put("js", "application/javascript");
		mapa.put("json", "application/json");
		mapa.put("mid", "audio/midi");
		mapa.put("midi", "audio/midi");
		mapa.put("mpeg", "video/mpeg");
		mapa.put("mpkg", "application/vnd.apple.installer+xml");
		mapa.put("odp", "application/vnd.oasis.opendocument.presentation");
		mapa.put("ods", "application/vnd.oasis.opendocument.spreadsheet");
		mapa.put("odt", "application/vnd.oasis.opendocument.text");
		mapa.put("oga", "audio/ogg");
		mapa.put("ogv", "video/ogg");
		mapa.put("ogx", "application/ogg");
		mapa.put("pdf", "application/pdf");
		mapa.put("ppt", "application/vnd.ms-powerpoint");
		mapa.put("png", "image/png");
		mapa.put("rar", "application/x-rar-compressed");
		mapa.put("rtf", "application/rtf");
		mapa.put("sh", "application/x-sh");
		mapa.put("svg", "image/svg+xml");
		mapa.put("swf", "application/x-shockwave-flash");
		mapa.put("tar", "application/x-tar");
		mapa.put("tif", "image/tiff");
		mapa.put("tiff", "image/tiff");
		mapa.put("ttf", "font/ttf");
		mapa.put("vsd", "application/vnd.visio");
		mapa.put("wav", "audio/x-wav");
		mapa.put("weba", "audio/webm");
		mapa.put("webm", "video/webm");
		mapa.put("webp", "image/webp");
		mapa.put("woff", "font/woff");
		mapa.put("woff2", "font/woff2");
		mapa.put("xhtml", "application/xhtml+xml");
		mapa.put("xls", "application/vnd.ms-excel");
		mapa.put("xml", "application/xml");
		mapa.put("xul", "application/vnd.mozilla.xul+xml");
		mapa.put("zip", "application/zip");
		mapa.put("3gp", "video/3gpp");
		mapa.put("3g2", "video/3gpp2");
		mapa.put("7z", "application/x-7z-compressed");
		return mapa;
	}
}
