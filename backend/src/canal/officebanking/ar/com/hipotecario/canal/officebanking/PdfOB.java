package ar.com.hipotecario.canal.officebanking;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Resource;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;

public abstract class PdfOB {

	public static byte[] generarPdf(String plantilla, Map<String, Object> parametros) {
		InputStream stream = Resource.stream(plantilla);
		XWPFDocument documento;
		try {
			documento = new XWPFDocument(stream);

			for (Entry<String, Object> param : parametros.entrySet()) {
				if (param.getValue() instanceof Objeto) {
					XWPFTable tabla = tabla(documento, "fecha"); // TODO.: SACAR FECHA
					Objeto filas = (Objeto) param.getValue();

					for (int i = 1; i < filas.objetos().size(); i++) {
						duplicarFila(tabla, tabla.getRow(1));
					}

					int indice = 1;
					for (Objeto fila : filas.objetos()) {
						for (Entry<String, Object> m : fila.toMap().entrySet()) {
							modificarTextoFila(tabla.getRow(indice), m.getKey(), m.getValue().toString());
						}
						indice++;
					}

				} else {
					if (param.getKey().equals("TITULO")) {
						modificarTitulo(documento, "TITULO", param.getValue().toString());
					} else {
						XWPFTableRow f = fila(documento, param.getKey());
						modificarTextoFila(f, param.getKey(), param.getValue().toString());
					}

				}
			}
			return convertirAPDF(documento);

		} catch (IOException e) {
			return null;
		}

	}

	private static byte[] convertirAPDF(XWPFDocument documento) throws IOException {
		PdfOptions options = PdfOptions.create();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		PdfConverter.getInstance().convert(documento, out, options);
		return out.toByteArray();
	}

	private static XWPFTable tabla(XWPFDocument document, String key) {
		for (XWPFTable table : document.getTables()) {
			for (XWPFTableRow row : table.getRows()) {
				for (XWPFTableCell cell : row.getTableCells()) {
					for (XWPFParagraph para : cell.getParagraphs()) {
						if (para.getText().startsWith(key)) {
							return table;
						}
					}
				}
			}
		}
		return null;
	}

	private static XWPFTableRow fila(XWPFDocument document, String key) {
		for (XWPFTable table : document.getTables()) {
			for (XWPFTableRow row : table.getRows()) {
				for (XWPFTableCell cell : row.getTableCells()) {
					for (XWPFParagraph para : cell.getParagraphs()) {
						if (para.getText().startsWith(key)) {
							return row;
						}
					}
				}
			}
		}
		return null;
	}

	private static XWPFTable duplicarFila(XWPFTable table, XWPFTableRow sourceRow) {
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
		table.addRow(newRow);
		return table;
	}

	private static void modificarTextoFila(XWPFTableRow row, String valorAnterior, String nuevoValor) {
		try {
			for (XWPFTableCell cell : row.getTableCells()) {
				for (XWPFParagraph p : cell.getParagraphs()) {
					remplazarTexto(valorAnterior, nuevoValor, p);
				}
			}
		} catch (Exception e) {

		}

	}

	private static void modificarTitulo(XWPFDocument documento, String valorAnterior, String valor) {
		List<XWPFHeader> headers = documento.getHeaderList();
		for (XWPFHeader h : headers) {
			for (XWPFParagraph p : h.getParagraphs()) {
				remplazarTexto(valorAnterior, valor, p);
			}
		}
	}

	private static void remplazarTexto(String valorAnterior, String valor, XWPFParagraph p) {
		for (XWPFRun r : p.getRuns()) {
			String text = r.getText(0);
			if (text != null) {
				if (text != null && text.contains(valorAnterior)) {
					text = text.replace(valorAnterior, valor);
					r.setText(text, 0);
				}
			}
		}
	}

}
