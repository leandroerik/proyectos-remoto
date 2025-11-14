package ar.com.hipotecario.canal.homebanking.lib;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

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

	public static void escribirBinario(String ruta, byte[] contenido) {
		try (FileOutputStream fos = new FileOutputStream(ruta)) {
			fos.write(contenido);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/* ========== PDF ========== */
	public static byte[] pdf() {
		List<PDDocument> documents = new ArrayList<>();
		try {
			PDDocument document = new PDDocument();

			PDPage page = new PDPage(new PDRectangle(1, 1));
			document.addPage(page);

			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			document.save(byteArrayOutputStream);
			document.close();
			for (PDDocument currentDocument : documents) {
				currentDocument.close();
			}
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] pdf(List<byte[]> binarios) {
		List<PDDocument> documents = new ArrayList<>();
		try {
			PDDocument document = new PDDocument();
			for (byte[] binario : binarios) {
				if (binario != null && binario.length > 3) {
					if (binario[0] == 0x25 && binario[1] == 0x50 && binario[2] == 0x44 && binario[3] == 0x46) { // Es PDF
						PDDocument currentDocument = PDDocument.load(binario);
						PDPageTree tree = currentDocument.getPages();
						Integer count = tree.getCount();
						for (Integer i = 0; i < count; ++i) {
							PDPage page = tree.get(i);
							document.importPage(page);
						}
						documents.add(currentDocument);
					} else { // Es Imagen
						ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(binario);
						BufferedImage bufferedImage = ImageIO.read(byteArrayInputStream);

						PDPage page = new PDPage(new PDRectangle(bufferedImage.getWidth(), bufferedImage.getHeight()));
						document.addPage(page);

//						PDImageXObject image = LosslessFactory.createFromImage(document, bufferedImage);
						PDImageXObject image = PDImageXObject.createFromByteArray(document, binario, "");
						try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
							contentStream.drawImage(image, 0, 0, bufferedImage.getWidth(), bufferedImage.getHeight());
						}
					}
				}
			}
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			document.save(byteArrayOutputStream);
			document.close();
			for (PDDocument currentDocument : documents) {
				currentDocument.close();
			}
			return byteArrayOutputStream.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
