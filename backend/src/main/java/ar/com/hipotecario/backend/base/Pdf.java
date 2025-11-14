package ar.com.hipotecario.backend.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.canal.officebanking.util.PdfHelper;
import com.google.api.client.json.Json;
import com.itextpdf.text.*;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;

import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;

public abstract class Pdf {

	public static byte[] generar(String template, Map<String, String> parametros) {
		try {
			InputStream doc = Resource.stream(template);
			XWPFDocument document = new XWPFDocument(doc);
			for (XWPFParagraph p : document.getParagraphs()) {
				List<XWPFRun> runs = p.getRuns();
				if (runs != null) {
					for (XWPFRun r : runs) {
						String text = r.getText(0);
						for (String clave : parametros.keySet()) {
							if (text != null && text.contains(clave)) {
								text = text.replace(clave, parametros.get(clave));
								r.setText(text, 0);
							}
						}
					}
				}
			}
			for (XWPFTable tbl : document.getTables()) {
				for (XWPFTableRow row : tbl.getRows()) {
					for (XWPFTableCell cell : row.getTableCells()) {
						for (XWPFParagraph p : cell.getParagraphs()) {
							for (XWPFRun r : p.getRuns()) {
								String text = r.getText(0);
								for (String clave : parametros.keySet()) {
									if (text != null && text.contains(clave)) {
										text = text.replace(clave, parametros.get(clave));
										r.setText(text, 0);
									}
								}
							}
						}
					}
				}
			}
			PdfOptions options = PdfOptions.create();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PdfConverter.getInstance().convert(document, out, options);
			return out.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] generarPdf(Objeto parametros, List<Objeto> movimientos, String platilla) {
		try {
			InputStream doc = Resource.stream(platilla);
			XWPFDocument document = new XWPFDocument(doc);

			for (XWPFParagraph p : document.getParagraphs()) {
				List<XWPFRun> runs = p.getRuns();
				if (runs != null) {
					for (XWPFRun r : runs) {
						String text = r.getText(0);
						if (text != null && parametros.get(text) != null) {
							text = text.replace(text, parametros.get(text).toString().trim());
							r.setText(text, 0);
						}
					}
				}
			}

			for (XWPFTable tbl : document.getTables()) {
				XWPFTable returnTable = cargarFilasEnTabla(tbl, movimientos);
				tbl = returnTable != null ? returnTable : tbl;
				int indexRow = 0;
				for (XWPFTableRow row : tbl.getRows()) {
					for (XWPFTableCell cell : row.getTableCells()) {
						for (XWPFParagraph p : cell.getParagraphs()) {
							for (XWPFRun r : p.getRuns()) {
								if (returnTable != null && indexRow != 0) {
									String text = r.getText(0);
									if (text != null) {
										text = text.replace(text, "");
										r.setText(text, 0);
									}
								} else {
									String text = r.getText(0);
									if (text != null && parametros.get(text) != null) {
										text = text.replace(text, parametros.get(text).toString().trim());
										r.setText(text, 0);
									}
								}
							}
						}
					}
					indexRow++;
				}
			}
			PdfOptions options = PdfOptions.create();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			PdfConverter.getInstance().convert(document, out, options);
			return out.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static byte[] generarPdfPlazoFijo(String titulo, Map<String, String> parametros) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			Document document = new Document();
			PdfWriter.getInstance(document, out);
			document.open();

			Font fontNormal = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.NORMAL);
			Font fontBold = new Font(Font.FontFamily.TIMES_ROMAN, 8, Font.BOLD);
			Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 14, Font.NORMAL);
			Font fontTipoDeposito = new Font(Font.FontFamily.TIMES_ROMAN, 9, Font.NORMAL);

			// Sección: ENCABEZADO
			PdfHelper.agregarEncabezado(document, "bh-300x100-rgb-1.png", "DETALLE DE RECEPCION", "FECHA:"+parametros.get("FECHA_HOY"), titleFont, false);
			document.add(new LineSeparator(0.5f, 100, BaseColor.BLACK, Element.ALIGN_CENTER, 0));

			PdfPTable infoTable1 = PdfHelper.crearTabla(3, 100);
			PdfHelper.agregarFila(infoTable1, new Paragraph[]{
																PdfHelper.crearParrafoDobleTexto("TIPO DE DEPOSITO: ", parametros.get("TIPO_DEPOSITO"),fontTipoDeposito),
																PdfHelper.crearParrafoDobleTexto("    ", "    ",fontTipoDeposito),
																PdfHelper.crearParrafoDobleTexto("CATEGORIA: ", parametros.get("CATEGORIA_PF"), fontNormal)}, Element.ALIGN_LEFT, Rectangle.NO_BORDER);

			PdfHelper.agregarFila(infoTable1, new Paragraph[]{
																PdfHelper.crearParrafoDobleTexto("N° DEPOSITO: ", parametros.get("N_DEPOSITO"),fontNormal),
																PdfHelper.crearParrafoDobleTexto("    ", "    ",fontTipoDeposito),
																PdfHelper.crearParrafoDobleTexto("MONEDA: ", parametros.get("MONEDA_PF"), fontNormal)},
					Element.ALIGN_LEFT, Rectangle.NO_BORDER);
			PdfHelper.agregarFila(infoTable1, new Paragraph[]{
																PdfHelper.crearParrafoDobleTexto("TITULAR: ", parametros.get("TITULAR_PF"),fontNormal),
																PdfHelper.crearParrafoDobleTexto("    ", "    ",fontTipoDeposito),
																PdfHelper.crearParrafoDobleTexto("OFICINA: ", parametros.get("OFICINA_PF"), fontNormal)
				 }, Element.ALIGN_LEFT, Rectangle.NO_BORDER);
			document.add(infoTable1);
			document.add(Chunk.NEWLINE);

			// Tabla de datos principales usando agregarFila y agregarFilaAvanzada
			PdfPTable table = PdfHelper.crearTabla(3, 100);

			PdfHelper.agregarFila(table, new Paragraph[]{
					PdfHelper.crearParrafoDobleTexto("CAPITAL INICIAL: ", parametros.getOrDefault("CAPITAL_INICIAL", ""), fontNormal),
					PdfHelper.crearParrafoDobleTexto("PLAZO: ", parametros.getOrDefault("PLAZO_DIAS", ""), fontNormal),
					PdfHelper.crearParrafoDobleTexto("FECHA ACTIVACION: ", parametros.getOrDefault("FECHA_ACTIVACION", ""), fontNormal)
			}, Element.ALIGN_LEFT, Rectangle.NO_BORDER);


			PdfHelper.agregarFila(table, new Paragraph[]{
					PdfHelper.crearParrafoDobleTexto("IMP.SELLOS:  ", parametros.getOrDefault("IMP_SELLOS", ""), fontNormal),
					PdfHelper.crearParrafoDobleTexto("FORMA DE PAGO: ", parametros.getOrDefault("FORMA_DE_PAGO", ""), fontNormal),
					PdfHelper.crearParrafoDobleTexto("FECHA INGRESO: ", parametros.getOrDefault("FECHA_INGRESO", ""), fontNormal)
			}, Element.ALIGN_LEFT, Rectangle.NO_BORDER);


			PdfHelper.agregarFila(table, new Paragraph[]{
					PdfHelper.crearParrafoDobleTexto("INTERESES: ", parametros.getOrDefault("INTERESES_PF", ""), fontNormal),
					PdfHelper.crearParrafoDobleTexto("TNA: ", parametros.getOrDefault("TNA_PF", ""), fontNormal),
					PdfHelper.crearParrafoDobleTexto("FECHA VENCIMIENTO: ", parametros.getOrDefault("FECHA_VENCIMIENTO", ""), fontNormal)
			}, Element.ALIGN_LEFT, Rectangle.NO_BORDER);

			// Fila avanzada para IMP. GANANCIAS
			PdfHelper.agregarFila(
					table,
					new Paragraph[]{
							PdfHelper.crearParrafoDobleTexto("IMP. GANANCIAS: ", parametros.getOrDefault("IMP_GANANCIAS", ""),fontNormal),
							new Paragraph(""),
							new Paragraph("")
					},
					Element.ALIGN_LEFT,
					Rectangle.NO_BORDER
			);

			document.add(table);
			document.add(Chunk.NEWLINE);

			// Tabla de propietarios usando agregarFila y agregarFilaAvanzada
			PdfPTable ownersTable = PdfHelper.crearTabla(6, 100);

			PdfHelper.agregarFilaAvanzada(
					ownersTable,
					new String[]{"PROPIETARIOS", "RECEP. CAPITAL"},
					fontBold,
					Element.ALIGN_CENTER,
					Rectangle.BOX,
					new int[]{2, 4},
					new float[]{8f}
			);

			PdfHelper.agregarFilaAvanzada(
					ownersTable,
					new String[]{"ROL", "NOMBRE", "MONEDA", "F. RECEP", "VALOR"},
					fontBold,
					Element.ALIGN_CENTER,
					Rectangle.BOX,
					new int[]{1, 1, 1, 2, 1},
					new float[]{8f}
			);

			PdfHelper.agregarFilaAvanzada(
					ownersTable,
					new String[]{
							parametros.get("ROL_TIT"),
							parametros.get("NOMBRE_TIT"),
							parametros.get("MONEDA_PF"),
							parametros.get("TIPO_CUENTA"),
							parametros.get("N_CUENTA"),
							parametros.get("CAPITAL_INICIAL")
					},
					fontNormal,
					Element.ALIGN_CENTER,
					Rectangle.BOX,
					new int[]{1, 1, 1, 1, 1, 1},
					new float[]{4f}
			);

			Paragraph totalParrafo = PdfHelper.crearParrafoDobleTexto("TOTAL: ", "", fontNormal);
			totalParrafo.setAlignment(Element.ALIGN_RIGHT);

			Paragraph valorParrafo = PdfHelper.crearParrafoDobleTexto("", parametros.get("CAPITAL_FINAL"), fontNormal);
			valorParrafo.setAlignment(Element.ALIGN_CENTER);

			PdfHelper.agregarFilaAvanzada(
					ownersTable,
					new Paragraph[]{totalParrafo, valorParrafo},
					new int[]{Element.ALIGN_RIGHT, Element.ALIGN_LEFT},
					new int[]{Rectangle.TOP, Rectangle.BOX},
					new int[]{5, 1},
					new float[]{4f}
			);

			document.add(ownersTable);
			document.add(Chunk.NEWLINE);

			// Sección: FIRMA (sin cambios, por claridad)
			float[] widths = {20f, 2f, 20f, 2f, 20f, 2f, 34f};
			PdfPTable firmasTable = PdfHelper.crearTabla(7, 100);
			firmasTable.setWidths(widths);

			PdfHelper.agregarFilaAvanzada(
					firmasTable,
					new Paragraph[]{
							new Paragraph("FIRMA", fontNormal) {{
								setAlignment(Element.ALIGN_CENTER);
							}},
							new Paragraph("",fontNormal),
							new Paragraph("1RA FIRMA Y SELLO",fontNormal){{
								setAlignment(Element.ALIGN_CENTER);
							}},
							new Paragraph("",fontNormal),
							new Paragraph("2DA FIRMA Y SELLO",fontNormal){{
								setAlignment(Element.ALIGN_CENTER);
							}},
							new Paragraph("",fontNormal),
							new Paragraph("SELLO DE INVERSIONES/CAJA:",fontNormal){{
								setAlignment(Element.ALIGN_CENTER);
							}},
					},
					new int[]{Element.ALIGN_CENTER},
					new int[]{Rectangle.TOP,Rectangle.NO_BORDER,Rectangle.TOP,Rectangle.NO_BORDER,Rectangle.TOP,Rectangle.NO_BORDER,Rectangle.TOP},
					new int[]{1},
					new float[]{4f}
			);

			document.add(firmasTable);
			document.add(Chunk.NEWLINE);

			// Aclaración
			document.add(PdfHelper.crearParrafoTexto("ACLARACION:", fontBold, 8, true));
			document.add(PdfHelper.crearParrafoTexto("DNI:", fontBold, 8, true));
			document.add(PdfHelper.crearParrafoTexto("DOMICILIO:", fontBold, 8, true));
			for (int i = 0; i < 9; i++) {
				document.add(Chunk.NEWLINE);
			}

			// Texto legal final
			document.add(PdfHelper.crearParrafoTexto(
					"En mi carácter de titular, autorizo al BANCO HIPOTECARIO S.A. a que realice el presente débito en la/s cuenta/s aquí detalladas\n" +
							"En caso de renovación automática la tasa a aplicar será la de pizarra para el plazo, monto y moneda de la imposición, al día de la renovación. La renovación\n" +
							"automática podrá dejarse sin efecto hasta el día del respectivo vencimiento. Los depósitos cuentan con una garantía de $ 25.000.000. En las operaciones a\n" +
							"nombre de dos o más personas, la garantía se prorrateará entre sus Titulares. En ningún caso, el total de la garantía por persona podrá exceder de $\n" +
							"25.000.000, cualquiera sea el número depósitos (Ley 24.485 Decreto 540/95 y según normas del BCRA sobre Aplicación del Sistema de Seguro de Garantía\n" +
							"de los Depósitos). Se encuentran excluidos los depósitos captados a tasas superiores a la de la referencia y los que hayan contado con incentivos o estímulos\n" +
							"Especiales adicionales a la tasa de interés y los efectuados por personas vinculadas a la entidad financiera\n", fontNormal, 8, false));

			document.close();
			return out.toByteArray();
		} catch (DocumentException | IOException e) {
			throw new RuntimeException(e);
		}
	}
	public static XWPFTable cargarFilasEnTabla(XWPFTable table, List<Objeto> movimientos) {
		int indexRow = 1;
		XWPFTable returnTable = null;
		// the empty row
		XWPFTableRow sourceRow = table.getRow(indexRow);

		if (sourceRow != null && "FECHA_MOV".equalsIgnoreCase(sourceRow.getCell(0).getText())) {
			returnTable = copiarFilas(table, sourceRow, movimientos);
			returnTable.removeRow(indexRow); // removing the template row
		}

		return returnTable;
	}

	private static XWPFTable copiarFilas(XWPFTable table, XWPFTableRow sourceRow, List<Objeto> data) {
		for (Objeto objeto : data) {
			// create a new row from the template, which would copy the format of previous
			// row
			XWPFTableRow oldRow = sourceRow;
			CTRow ctrow = null;
			try {
				ctrow = CTRow.Factory.parse(oldRow.getCtRow().newInputStream());

			} catch (XmlException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			XWPFTableRow newRow = new XWPFTableRow(ctrow, table);
			cargarDatosEnFilas(newRow, objeto);

			// adding the newly created row top he table
			table.addRow(newRow);
		}
		return table;
	}

	private static void cargarDatosEnFilas(XWPFTableRow newRow, Objeto objeto) {
		int tamanoLetra = 7;
		for (XWPFTableCell cell : newRow.getTableCells()) {
			for (XWPFParagraph paragraph : cell.getParagraphs()) {
				for (XWPFRun run : paragraph.getRuns()) {
					String valueCell = run.getText(0);
					run.setText(objeto.string(valueCell).trim());
					run.setFontSize(tamanoLetra);
				}
			}
		}
	}

	/* TEST */
	public static void main(String[] args) {

//		Ejemplo estatico
//		Map<String, String> parametros = new HashMap<>();
//		parametros.put("NOMBRE_BENEFICIARIO", "Gabriel");
//		byte[] pdf = Pdf.generar("ode", parametros);
//		Archivo.escribir("C:\\Users\\C05302\\Desktop\\prueba.pdf", pdf);

//		Ejemplo dinamico
		Objeto parametros = new Objeto();
		parametros.set("TOTAL_IMPORTE", "123456");

		Objeto movimientos = new Objeto();

		Objeto item1 = movimientos.add();
		item1.set("FECHA_MOV", "A1");
		item1.set("DESCRIPCION_MOV", "A2");
		item1.set("IMPORTE_MOV", "A3");
		item1.set("SALDO_MOV", "A4");

		Objeto item2 = movimientos.add();
		item2.set("FECHA_MOV", "B1");
		item2.set("DESCRIPCION_MOV", "B2");
		item2.set("IMPORTE_MOV", "B3");
		item2.set("SALDO_MOV", "B4");

		byte[] pdf = Pdf.generarPdf(parametros, movimientos.objetos(), "/comprobantes/movimientos-cuenta.docx");
		Archivo.escribir("C:\\Users\\C05302\\Desktop\\prueba.pdf", pdf);
	}
}
