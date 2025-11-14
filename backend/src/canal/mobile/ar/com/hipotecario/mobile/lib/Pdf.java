package ar.com.hipotecario.mobile.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import ar.com.hipotecario.mobile.ConfigMB;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;

public abstract class Pdf {

	public static byte[] generar(String template, Map<String, String> parametros) {
		try {
			InputStream doc = ConfigMB.class.getResourceAsStream("/comprobantes/" + template + ".docx");
			XWPFDocument document = new XWPFDocument(doc);
			for (XWPFParagraph p : document.getParagraphs()) {
				List<XWPFRun> runs = p.getRuns();
				if (runs != null) {
					for (XWPFRun r : runs) {
						String text = r.getText(0);
						for (String clave : parametros.keySet()) {
							if (text != null && text.contains(clave)) {
								text = text.replace(clave, parametros.get(clave) != null ? parametros.get(clave) : "");
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
										text = text.replace(clave, parametros.get(clave) != null ? parametros.get(clave) : "");
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
}
