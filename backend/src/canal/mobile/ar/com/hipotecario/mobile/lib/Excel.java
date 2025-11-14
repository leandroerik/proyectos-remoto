package ar.com.hipotecario.mobile.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;

public class Excel {

	private static final String REGEX = "['<>^]*";

	public static byte[] generarExcel(HSSFWorkbook workbook, ContextoMB contexto, String nombre) {
		byte[] contentReturn = null;
		String nombreComprobante = nombre + ".xls";
		try {

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			workbook.write(baos);
			contentReturn = baos.toByteArray();

			contexto.setHeader("Content-Disposition", "attachment; filename=" + nombreComprobante);
			contexto.setContentType("application/vnd.ms-excel");

		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (null != workbook) {
				try {
					workbook.close();
				} catch (IOException eio) {
					throw new RuntimeException(eio);
				}
			}
		}

		return contentReturn;
	}

	public static HSSFWorkbook contruyeExcel(HSSFWorkbook file, ContextoMB contexto, List<Objeto> movimientos, Objeto datosExportar, Boolean clonar, int indicePagina, String plantilla) {
		HSSFWorkbook workbook = null;
		int contFila = 8;
		int movimientoProcesado = 0;

		try {
			InputStream doc = ConfigMB.class.getResourceAsStream(plantilla);
			workbook = file == null ? new HSSFWorkbook(doc) : file;
			HSSFSheet sheet = workbook.getSheetAt(indicePagina);
			if (clonar) {
				sheet.getWorkbook().cloneSheet(sheet.getWorkbook().getSheetIndex(sheet));
			}

			cargarFilasEnTabla(sheet, contFila, movimientos.size());

			Iterator<Row> rowIterator = sheet.iterator();
			Row fila = null;
			boolean encontroTabla = false;
			CellStyle origStyle = null;

			while (rowIterator.hasNext()) {
				fila = rowIterator.next();
				Iterator<Cell> cellIterator = fila.cellIterator();
				Cell celda;
				while (cellIterator.hasNext()) {
					celda = cellIterator.next();
					origStyle = celda.getCellStyle();

					switch (celda.getCellType()) {

					case NUMERIC:
						break;
					case STRING:
						String item = celda.getStringCellValue().trim();
						item = item.toUpperCase();
						item = item.replaceAll(REGEX, "");
						celda.setCellStyle(origStyle);
						if (validarIteraccionTabla(item)) {
							encontroTabla = true;
							reemplazarItemTabla(celda, item, movimientos, movimientoProcesado);
						} else {
							reemplazarItem(celda, item, datosExportar);
						}
						break;
					case BOOLEAN:
						break;
					default:
						break;
					}
				}

				if (encontroTabla) {
					movimientoProcesado++;
					ajustarColumna(sheet, fila);
				}
			}

		} catch (IOException eio) {
			throw new RuntimeException(eio);
		}

		return workbook;
	}

	private static void reemplazarItem(Cell cell, String item, Objeto parametros) {
		if (parametros != null && parametros.existe(item)) {
			String valor = parametros.get(item).toString().trim();
			reemplazarItemValor(cell, item, valor);
		}
	}

	private static void reemplazarItemTabla(Cell cell, String item, List<Objeto> listaMovimientos, int index) {

		if (listaMovimientos != null && !listaMovimientos.isEmpty() && index < listaMovimientos.size()) {
			Objeto movimiento = listaMovimientos.get(index);
			String valor = movimiento.get(item).toString().trim();
			reemplazarItemValor(cell, item, valor);
		} else {
//			cell.setCellType(CellType.STRING);
			cell.setCellValue("");
		}
	}

	private static void reemplazarItemValor(Cell cell, String item, String valor) {
		if (valor != null && !valor.isEmpty()) {
			if (verificarItemMonto(item)) {
				cell.setCellValue(Formateador.importe(valor));
			} else {
				cell.setCellValue(valor);
			}
		} else {
//			cell.setCellType(CellType.STRING);
			cell.setCellValue("");
		}
	}

	private static void cargarFilasEnTabla(HSSFSheet sheet, int contFila, int movimientos) {
		for (int i = 0; i < (movimientos - 1); i++) {
			copiarFila(sheet, contFila, contFila + 1);
			contFila++;
		}
	}

	private static boolean validarIteraccionTabla(String item) {
		return ("FECHA_MOV".equalsIgnoreCase(item) || "DESCRIPCION_MOV".equalsIgnoreCase(item) || "IMPORTE_MOV".equalsIgnoreCase(item) || "SALDO_MOV".equalsIgnoreCase(item) || "IMPORTE_FORMAT_PESOS".equalsIgnoreCase(item) || "IMPORTE_FORMAT_USD".equalsIgnoreCase(item));

	}

	private static boolean verificarItemMonto(String item) {
		return ("IMPORTE_MOV".equalsIgnoreCase(item) || "SALDO_MOV".equalsIgnoreCase(item) || "TOTAL_IMPORTE".equalsIgnoreCase(item) || "IMPORTE_FORMAT_PESOS".equalsIgnoreCase(item) || "IMPORTE_FORMAT_USD".equalsIgnoreCase(item) || "TOTAL_IMPORTE_USD".equalsIgnoreCase(item));
	}

	private static void copiarFila(HSSFSheet worksheet, int sourceRowNum, int destinationRowNum) {

		HSSFRow newRow = worksheet.getRow(destinationRowNum);
		HSSFRow sourceRow = worksheet.getRow(sourceRowNum);
		sourceRow.setHeightInPoints((2 * worksheet.getDefaultRowHeightInPoints()));

		// considera si existen formulas y las respalda antes de hacer el shiftRows, ya
		// que luego no se copian correctamente.
		String[] formulasArray = new String[sourceRow.getLastCellNum()];
		for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
			if (sourceRow.getCell(i) != null && sourceRow.getCell(i).getCellType() == CellType.FORMULA)
				formulasArray[i] = sourceRow.getCell(i).getCellFormula();
		}

		if (newRow != null) {
			worksheet.shiftRows(destinationRowNum, worksheet.getLastRowNum(), 1, true, false);
		}

		newRow = worksheet.createRow(destinationRowNum);
		newRow.setHeightInPoints((2 * worksheet.getDefaultRowHeightInPoints()));

		for (int i = 0; i < sourceRow.getLastCellNum() + 1; i++) {
			HSSFCell oldCell = sourceRow.getCell(i);
			HSSFCell newCell = newRow.createCell(i);

			if (oldCell == null) {
				newCell = null;
				continue;
			}

			HSSFCellStyle newCellStyle = worksheet.getWorkbook().createCellStyle();
			newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
			newCell.setCellStyle(newCellStyle);

			// If there is a cell comment, copy
			if (newCell.getCellComment() != null)
				newCell.setCellComment(oldCell.getCellComment());

			// If there is a cell hyperlink, copy
			if (oldCell.getHyperlink() != null)
				newCell.setHyperlink(oldCell.getHyperlink());

			// Set the cell data type
			newCell.setCellType(oldCell.getCellType());

			// Set the cell data value
			switch (oldCell.getCellType()) {
			case BLANK:
				break;
			case BOOLEAN:
				newCell.setCellValue(oldCell.getBooleanCellValue());
				break;
			case FORMULA:
				newCell.setCellFormula(formulasArray[i]);
				break;
			case NUMERIC:
				newCell.setCellValue(oldCell.getNumericCellValue());
				break;
			case STRING:
				newCell.setCellValue(oldCell.getRichStringCellValue());
				break;
			default:
				break;
			}
		}

		// If there are any merged regions in the source row, copy to new row
		for (int i = 0; i < worksheet.getNumMergedRegions(); i++) {
			CellRangeAddress cellRangeAddress = worksheet.getMergedRegion(i);
			if (cellRangeAddress.getFirstRow() == sourceRow.getRowNum()) {
				CellRangeAddress newCellRangeAddress = new CellRangeAddress(newRow.getRowNum(), (newRow.getRowNum() + (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow())), cellRangeAddress.getFirstColumn(), cellRangeAddress.getLastColumn());
				worksheet.addMergedRegion(newCellRangeAddress);
			}
		}

	}

	private static void ajustarColumna(HSSFSheet sheet, Row sourceRow) {
		for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
			sheet.autoSizeColumn(i);
		}
	}

}
