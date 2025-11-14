package ar.com.hipotecario.canal.officebanking.util;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.net.URL;

/**
 * Utilidades para simplificar la creación y manipulación de tablas y celdas en PDF usando iText.
 */
public class PdfHelper {

    /**
     * Crea una tabla PDF con la cantidad de columnas y el ancho especificado.
     *
     * @param columnas        Número de columnas de la tabla.
     * @param widthPercentage Porcentaje de ancho de la tabla respecto a la página.
     * @return PdfPTable creada.
     */
    public static PdfPTable crearTabla(int columnas, float widthPercentage) {
        PdfPTable table = new PdfPTable(columnas);
        table.setWidthPercentage(widthPercentage);
        return table;
    }

    /**
     * Agrega una fila a la tabla con los valores indicados, aplicando el mismo estilo a todas las celdas.
     *
     * @param tabla     Tabla a la que se agregará la fila.
     * @param valores   Array de textos para cada celda.
     * @param fuente    Fuente a aplicar en las celdas.
     * @param alignment Alineación horizontal de las celdas.
     * @param border    Tipo de borde de las celdas.
     */

    public static void agregarFila(PdfPTable tabla, String[] valores, Font fuente, int alignment, int border) {
        for (String valor : valores) {
            PdfPCell celda = crearCeldaTexto(valor, fuente, 1, alignment, border);
            tabla.addCell(celda);
        }
    }
    public static void agregarFila(PdfPTable tabla, Paragraph[] valores, int alignment, int border) {
        for (Paragraph valor : valores) {
            PdfPCell celda = new PdfPCell();
            celda.addElement(valor);
            celda.setColspan(1);
            celda.setHorizontalAlignment(alignment);
            celda.setBorder(border);
            tabla.addCell(celda);
        }
    }
    /**
     * Agrega una fila avanzada a la tabla, permitiendo especificar el colspan de cada celda.
     *
     * @param tabla     Tabla a la que se agregará la fila.
     * @param valores   Array de textos para cada celda.
     * @param fuente    Fuente a aplicar en las celdas.
     * @param alignment Alineación horizontal de las celdas.
     * @param border    Tipo de borde de las celdas.
     * @param colspans  Array de colspans para cada celda (si es null o no se indica, se usa 1).
     */
    public static void agregarFilaAvanzada(PdfPTable tabla, String[] valores, Font fuente, int alignment, int border, int[] colspans, float[] paddings) {
        for (int i = 0; i < valores.length; i++) {
            int colspan = (colspans != null && i < colspans.length) ? colspans[i] : 1;
            PdfPCell celda = crearCeldaTexto(valores[i], fuente, colspan, alignment, border);
            if (paddings != null) {
                if (paddings.length == 4) {
                    celda.setPaddingLeft(paddings[0]);
                    celda.setPaddingRight(paddings[1]);
                    celda.setPaddingTop(paddings[2]);
                    celda.setPaddingBottom(paddings[3]);
                } else if (paddings.length == 2) {
                    celda.setPaddingLeft(paddings[0]);
                    celda.setPaddingRight(paddings[0]);
                    celda.setPaddingTop(paddings[1]);
                    celda.setPaddingBottom(paddings[1]);
                } else if (paddings.length == 1) {
                    celda.setPadding(paddings[0]);
                }
            }
            tabla.addCell(celda);
        }
    }
    public static void agregarFilaAvanzada(PdfPTable tabla, Paragraph[] valores, int[] alignments, int[] borders, int[] colspans, float[] paddings) {
        for (int i = 0; i < valores.length; i++) {
            int alignment = (alignments != null)
                    ? (alignments.length == 1 ? alignments[0] : (i < alignments.length ? alignments[i] : Element.ALIGN_LEFT))
                    : Element.ALIGN_LEFT;
            int border = (borders != null)
                    ? (borders.length == 1 ? borders[0] : (i < borders.length ? borders[i] : Rectangle.BOX))
                    : Rectangle.BOX;
            int colspan = (colspans != null && i < colspans.length) ? colspans[i] : 1;
            PdfPCell celda = new PdfPCell();
            celda.addElement(valores[i]);
            celda.setColspan(colspan);
            celda.setHorizontalAlignment(alignment);
            celda.setBorder(border);
            if (paddings != null) {
                if (paddings.length == 4) {
                    celda.setPaddingLeft(paddings[0]);
                    celda.setPaddingRight(paddings[1]);
                    celda.setPaddingTop(paddings[2]);
                    celda.setPaddingBottom(paddings[3]);
                } else if (paddings.length == 2) {
                    celda.setPaddingLeft(paddings[0]);
                    celda.setPaddingRight(paddings[0]);
                    celda.setPaddingTop(paddings[1]);
                    celda.setPaddingBottom(paddings[1]);
                } else if (paddings.length == 1) {
                    celda.setPadding(paddings[0]);
                }
            }
            tabla.addCell(celda);
        }
    }
    /**
     * Crea un párrafo con dos textos: el primero en negrita y el segundo en normal, ambos en la misma línea.
     * Utiliza la familia y tamaño de la fuente base pasada por parámetro.
     *
     * @param textoNegrita Texto que se mostrará en negrita.
     * @param textoNormal  Texto que se mostrará en estilo normal, a continuación del anterior.
     * @param fuenteBase   Fuente base para obtener familia y tamaño.
     * @return Paragraph con ambos textos en la misma línea.
     */
    public static Paragraph crearParrafoDobleTexto(String textoNegrita, String textoNormal, Font fuenteBase) {
        // Evitar valores null
        if (textoNegrita == null) textoNegrita = "";
        if (textoNormal == null) textoNormal = "";

        Font fontNegrita = new Font(fuenteBase.getFamily(), fuenteBase.getSize(), Font.BOLD);
        Font fontNormal = new Font(fuenteBase.getFamily(), fuenteBase.getSize(), Font.NORMAL);

        Paragraph parrafo = new Paragraph();
        parrafo.add(new Chunk(textoNegrita, fontNegrita));
        parrafo.add(new Chunk(textoNormal, fontNormal));
        return parrafo;
    }
    /**
     * Crea una celda de texto personalizada.
     *
     * @param texto     Texto de la celda.
     * @param fuente    Fuente a aplicar.
     * @param colspan   Cantidad de columnas que ocupa la celda.
     * @param alignment Alineación horizontal.
     * @param border    Tipo de borde.
     * @return PdfPCell configurada.
     */
    public static PdfPCell crearCeldaTexto(String texto, Font fuente, int colspan, int alignment, int border) {
        Phrase phrase = (fuente != null) ? new Phrase(texto, fuente) : new Phrase(texto);
        PdfPCell cell = new PdfPCell(phrase);
        cell.setColspan(colspan);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(border);
        return cell;
    }

    /**
     * Crea una celda que contiene una tabla anidada.
     *
     * @param tablaAnidada Tabla a insertar dentro de la celda.
     * @param colspan      Cantidad de columnas que ocupa la celda.
     * @param alignment    Alineación horizontal.
     * @param border       Tipo de borde.
     * @return PdfPCell configurada con la tabla anidada.
     */
    public static PdfPCell crearCeldaTabla(PdfPTable tablaAnidada, int colspan, int alignment, int border) {
        PdfPCell cell = new PdfPCell(tablaAnidada);
        cell.setColspan(colspan);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(border);
        return cell;
    }
    /**
     * Crea una celda que contiene una imagen.
     *
     * @param imagen    Imagen de iText a insertar.
     * @param colspan   Cantidad de columnas que ocupa la celda.
     * @param alignment Alineación horizontal.
     * @param border    Tipo de borde.
     * @return PdfPCell configurada con la imagen.
     */
    public static PdfPCell crearCeldaImagen(Image imagen, int colspan, int alignment, int border) {
        PdfPCell cell = new PdfPCell(imagen, true);
        cell.setColspan(colspan);
        cell.setHorizontalAlignment(alignment);
        cell.setBorder(border);
        return cell;
    }

    /**
     * Agrega un encabezado predeterminado al documento PDF con logo, título centrado y fecha alineada arriba a la derecha.
     *
     * @param document Documento PDF donde se agregará el encabezado.
     * @param nombreLogo     Imagen del logo a mostrar a la izquierda.
     * @param titulo   Título a mostrar centrado.
     * @param fecha    Fecha a mostrar a la derecha, en tamaño más pequeño.
     * @param fuente   Fuente a utilizar (opcional, puede ser null).
     * @param negrita  Si el texto debe estar en negrita.
     * @throws DocumentException Si ocurre un error al agregar el encabezado.
     */
    public static void agregarEncabezado(Document document, String nombreLogo, String titulo, String fecha, Font fuente, boolean negrita) throws DocumentException, java.io.IOException {
        Image logo = Image.getInstance(PdfHelper.class.getClassLoader().getResource("img/" + nombreLogo));

        float marginLeft = 56f;
        float marginTop = 56f;
        float logoHeight = 60.1f;

        logo.scaleToFit(134.4f, logoHeight);
        logo.setAbsolutePosition(marginLeft, document.getPageSize().getHeight() - marginTop - logoHeight);
        document.add(logo);

        // Fecha arriba a la derecha
        Font fontFecha = new Font(Font.FontFamily.TIMES_ROMAN, 8);
        if (negrita) fontFecha.setStyle(Font.BOLD);
        Paragraph fechaParrafo = new Paragraph(fecha, fontFecha);
        fechaParrafo.setAlignment(Element.ALIGN_RIGHT);
        fechaParrafo.setSpacingBefore(10f); // Opcional: espacio desde el tope
        document.add(fechaParrafo);

        Paragraph espacio = new Paragraph(" ");
        espacio.setSpacingBefore(40f); // Ajusta según el alto del logo
        document.add(espacio);

        PdfPTable headerTable = new PdfPTable(3);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{10f, 80f, 10f});

        // Celda logo vacía
        PdfPCell logoCell = new PdfPCell();
        logoCell.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(logoCell);

        // Celda título
        Font fontTitulo = fuente != null ? new Font(fuente) : new Font(Font.FontFamily.HELVETICA, 14);
        if (negrita) fontTitulo.setStyle(Font.BOLD);
        PdfPCell tituloCell = crearCeldaTexto(titulo, fontTitulo, 1, Element.ALIGN_CENTER, Rectangle.NO_BORDER);
        tituloCell.setVerticalAlignment(Element.ALIGN_BOTTOM);
        headerTable.addCell(tituloCell);

        // Celda vacía (ya que la fecha está arriba)
        PdfPCell vacia = new PdfPCell();
        vacia.setBorder(Rectangle.NO_BORDER);
        headerTable.addCell(vacia);

        document.add(headerTable);
    }
    /**
     * Crea un párrafo de texto personalizado para agregar al documento PDF.
     *
     * @param texto   Texto a mostrar.
     * @param fuente  Fuente base a utilizar (puede ser null).
     * @param tamano  Tamaño de la fuente.
     * @param negrita Si el texto debe estar en negrita.
     * @return Paragraph configurado.
     */
    public static Paragraph crearParrafoTexto(String texto, Font fuente, float tamano, boolean negrita) {
        Font font;
        if (fuente != null) {
            font = new Font(fuente);
            font.setSize(tamano);
        } else {
            font = new Font(Font.FontFamily.HELVETICA, tamano);
        }
        if (negrita) {
            font.setStyle(Font.BOLD);
        }
        return new Paragraph(texto, font);
    }
    // Ejemplo de uso:
    // PdfPTable tablaPadre = PdfHelper.crearTabla(2, 100);
    // PdfPTable tablaHija = PdfHelper.crearTabla(1, 100);
    // tablaHija.addCell(PdfHelper.crearCeldaTexto("Celda anidada", font, 1, Element.ALIGN_CENTER, Rectangle.BOX));
    // tablaPadre.addCell(PdfHelper.crearCeldaTabla(tablaHija, 2, Element.ALIGN_CENTER, Rectangle.NO_BORDER));
}