package ar.com.hipotecario.canal.officebanking.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.xmlbeans.XmlException;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Objeto;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;

public abstract class Pdf {
    private static final Map<String, String> campoADocx = Map.ofEntries(
            Map.entry("TIPO_OPERACION", "Tipo operación"),
            Map.entry("MONEDA_OP", "Moneda"),
            Map.entry("NRO_CLIENTE", "Número Cliente"),
            Map.entry("CUIT_CTE", "CUIT / CUIL"),
            Map.entry("IVA_CTE", "Categoría de IVA"),
            Map.entry("RAZON_SOCIAL", "Cliente"),
            Map.entry("DIRECCION_CTE", "Dirección"),
            Map.entry("CP_CTE", "Código Postal"),
            Map.entry("MONTO_OP", "Monto Oper."),
            Map.entry("CFT_TNA", "C.F.T. en T.N.A."),
            Map.entry("SALDO_CAPITAL", "Saldo Capital"),
            Map.entry("FECHA_VCIA", "Fecha de vigencia"),
            Map.entry("FECHA_VTO", "Fecha de vencimiento"),
            Map.entry("PLAZO_OP", "Plazo"),
            Map.entry("TIPO_PLAZO", "Tipo de Plazo"),
            Map.entry("TIPO_CUOTA", "Tipo de Cuota"),
            Map.entry("FECHA_PG", "Fecha pago"),
            Map.entry("FORMA_PG", "Forma"),
            Map.entry("DETALLE_PG", "Detalle"),
            Map.entry("REF_PF", "Referencia"),
            Map.entry("BEN_PF", "Beneficiario"),
            Map.entry("MON_PG", "Moneda"),
            Map.entry("MONTO_PG", "Monto"),
            Map.entry("MONTODESCR_PG", "Total") // este es especial, puede estar junto a MON_PG
    );

    public static byte[] generar(String template, Map<String, String> parametros) {
        try {
            InputStream doc = Config.class.getResourceAsStream("/comprobantes/" + template + ".docx");
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
            InputStream doc = Config.class.getResourceAsStream(platilla);
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

        if (sourceRow != null && "FECHA_PG".equalsIgnoreCase(sourceRow.getCell(0).getText())) {
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
                    String marcador = run.getText(0);

                    if (marcador != null && !marcador.trim().isEmpty()) {
                        // Buscar en el objeto un campo con ese nombre
                        if (objeto.existe(marcador)) {
                            String nuevoValor = objeto.string(marcador).trim();
                            run.setText(nuevoValor, 0);
                            run.setFontSize(tamanoLetra);
                        } else {
                            // Si el marcador no se encuentra, lo dejamos en blanco
                            run.setText("", 0);
                        }
                    }
                }
            }
        }
    }

    public static byte[] generarPDFPrestamos(String template, Map<String, String> parametros, List<Map<String, String>> conceptos) {
        try {
            InputStream doc = Config.class.getResourceAsStream("/comprobantes/" + template + ".docx");
            assert doc != null;
            XWPFDocument document = new XWPFDocument(doc);

            for (XWPFParagraph p : document.getParagraphs()) {
                for (XWPFRun r : p.getRuns()) {
                    String text = r.getText(0);
                    if (text != null) {
                        for (String clave : parametros.keySet()) {
                            if (text.contains(clave)) {
                                text = text.replace(clave, parametros.get(clave));
                                r.setText(text, 0);
                            }
                        }
                    }
                }
            }

            // Reemplazo en tablas + filas de conceptos
            for (XWPFTable tbl : document.getTables()) {
                List<XWPFTableRow> originalRows = new ArrayList<>(tbl.getRows());
                XWPFTableRow rowPlantilla = null;

                // Reemplazo de parámetros simples y detección de la fila plantilla
                for (XWPFTableRow row : originalRows) {
                    boolean contieneConceptos = false;

                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph p : cell.getParagraphs()) {
                            for (XWPFRun r : p.getRuns()) {
                                String text = r.getText(0);
                                if (text != null) {
                                    for (String clave : parametros.keySet()) {
                                        if (text.contains(clave)) {
                                            text = text.replace(clave, Optional.ofNullable(parametros.get(clave)).orElse(""));
                                            r.setText(text, 0);
                                        }
                                    }

                                    // Detección de si esta fila es plantilla
                                    if (!contieneConceptos && contieneAlgunaClave(text, conceptos)) {
                                        contieneConceptos = true;
                                        rowPlantilla = row;
                                    }
                                }
                            }
                        }
                    }
                }

                // Si encontramos la fila plantilla, insertamos los conceptos
                if (rowPlantilla != null) {
                    int indexPlantilla = tbl.getRows().indexOf(rowPlantilla);

                    for (Map<String, String> item : conceptos) {
                        CTRow ctrow = CTRow.Factory.parse(rowPlantilla.getCtRow().newInputStream());
                        XWPFTableRow newRow = new XWPFTableRow(ctrow, tbl);

                        for (XWPFTableCell cellNew : newRow.getTableCells()) {
                            for (XWPFParagraph paragraph : cellNew.getParagraphs()) {
                                for (XWPFRun run : paragraph.getRuns()) {
                                    String textNew = run.getText(0);
                                    if (textNew != null) {
                                        for (String key : item.keySet()) {
                                            if (textNew.contains(key)) {
                                                String valor = item.get(key);
                                                textNew = textNew.replace(key, valor != null ? valor : "");
                                                run.setText(textNew, 0);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Insertar después de la plantilla
                        tbl.addRow(newRow);
                    }

                    // Eliminar la fila plantilla
                    tbl.removeRow(indexPlantilla);
                }
            }

            // Convertir a PDF
            PdfOptions options = PdfOptions.create();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfConverter.getInstance().convert(document, out, options);



            byte[] pdfBase = out.toByteArray();

            // === 🆕 AGREGADO: cargar el PDF con PDFBox para agregar la numeración de páginas ===
            PDDocument pdfDoc = PDDocument.load(pdfBase);
            int totalPages = pdfDoc.getNumberOfPages();

            for (int i = 0; i < totalPages; i++) {
                PDPage page = pdfDoc.getPage(i);

                // Abre un flujo para agregar texto a cada página
                PDPageContentStream cs = new PDPageContentStream(
                        pdfDoc,
                        page,
                        PDPageContentStream.AppendMode.APPEND,
                        true,
                        true
                );

                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 7.5f);
                cs.newLineAtOffset(500, 823); // Coordenadas arriba a la derecha
                cs.showText("Página " + (i + 1) + " de " + totalPages);
                cs.endText();
                cs.close();
            }

            ByteArrayOutputStream finalOut = new ByteArrayOutputStream();
            pdfDoc.save(finalOut);
            pdfDoc.close();

            return finalOut.toByteArray();

        } catch (IOException | XmlException e) {
            throw new RuntimeException("Error al generar el PDF con numeración de páginas", e);
        }
    }
    private static boolean contieneAlgunaClave(String texto, List<Map<String, String>> conceptos) {
        for (Map<String, String> map : conceptos) {
            for (String key : map.keySet()) {
                if (texto.contains(key)) {
                    return true;
                }
            }
        }
        return false;
    }
}
