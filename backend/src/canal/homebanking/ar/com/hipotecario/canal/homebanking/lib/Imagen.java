package ar.com.hipotecario.canal.homebanking.lib;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import ar.com.hipotecario.canal.homebanking.base.Objeto;

public abstract class Imagen {

	public static void main(String[] args) {
		List<String> nombres = Objeto.listOf("DNI Dorso", "DNI Frente", "Neutra", "Ojos cerrados", "Sonriente");
		for (String nombre : nombres) {
			String carpeta = "C:\\Users\\C05302\\Desktop\\fotos2\\";
			String extension = ".jpg";
			String path = carpeta + nombre + extension;
			byte[] archivo = Archivo.leerBinario(path);
			byte[] archivoComprimido30 = Imagen.comprimir(archivo, path, 0.30f);
			byte[] archivoComprimido50 = Imagen.comprimir(archivo, path, 0.50f);
			Archivo.escribirBinario(carpeta + nombre + "_30-porciento-calidad" + extension, archivoComprimido30);
			Archivo.escribirBinario(carpeta + nombre + "_50-porciento-calidad" + extension, archivoComprimido50);
			Archivo.escribirBinario(carpeta + nombre + "_original" + extension, archivo);
		}
	}

	public static byte[] comprimir(byte[] bytes, String nombreArchivo, Float calidad) {
		try {
			BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
			ByteArrayOutputStream os = new ByteArrayOutputStream();

			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(extension(nombreArchivo).toLowerCase());
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
			return imagenComprimida;
		} catch (Exception e) {
			return bytes;
		}
	}

	public static String extension(String nombreArchivo) {
		int lastIndexOf = nombreArchivo.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return "";
		}
		return nombreArchivo.substring(lastIndexOf + 1);
	}
}
