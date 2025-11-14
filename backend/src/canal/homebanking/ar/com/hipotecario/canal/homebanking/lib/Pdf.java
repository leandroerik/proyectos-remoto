package ar.com.hipotecario.canal.homebanking.lib;

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
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;

import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;

public abstract class Pdf {

    public static byte[] generar(String template, Map<String, String> parametros) {
        try {
            InputStream doc = ConfigHB.class.getResourceAsStream("/comprobantes/" + template + ".docx");
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

    public static byte[] generarPdf(Objeto parametros, List<Objeto> movimientos, String platilla) {
        try {
            InputStream doc = ConfigHB.class.getResourceAsStream(platilla);
            XWPFDocument document = new XWPFDocument(doc);

            for (XWPFParagraph p : document.getParagraphs()) {
                List<XWPFRun> runs = p.getRuns();
                if (runs != null) {
                    for (XWPFRun r : runs) {
                        String text = r.getText(0);
                        if (text != null && parametros.existe(text)) {
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
                                    if (text != null && parametros.existe(text)) {
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
}
