package ar.com.hipotecario.canal.homebanking.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;

public class Excel {

	private static final String REGEX = "['<>^]*";

	public static byte[] generarExcel(XSSFWorkbook workbook, ContextoHB contexto, String nombre) {
		byte[] contentReturn = null;
		String nombreComprobante = nombre + ".xls";
		try {

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			workbook.write(baos);

			contentReturn = baos.toByteArray();

			contexto.responseHeader("Content-Disposition", "attachment; filename=" + nombreComprobante);
			contexto.contentType("application/vnd.ms-excel");

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

	public static XSSFWorkbook contruyeExcel(XSSFWorkbook file, ContextoHB contexto, List<Objeto> movimientos, Objeto datosExportar, Boolean clonar, int indicePagina, String plantilla) {
		XSSFWorkbook workbook = null;
		int contFila = 8;
		int movimientoProcesado = 0;

		try {
			InputStream doc = ConfigHB.class.getResourceAsStream(plantilla);
			workbook = file == null ? new XSSFWorkbook(doc) : file;
			XSSFSheet sheet = workbook.getSheetAt(indicePagina);
			if (clonar) {
				sheet.getWorkbook().cloneSheet(sheet.getWorkbook().getSheetIndex(sheet));
			}

			cargarFilasEnTabla(sheet, contFila, movimientos.size());

			Iterator<Row> rowIterator = sheet.iterator();
			Row fila = null;
			boolean encontroTabla = false;

			while (rowIterator.hasNext()) {
				fila = rowIterator.next();
				Iterator<Cell> cellIterator = fila.cellIterator();
				Cell celda;
				while (cellIterator.hasNext()) {
					celda = cellIterator.next();

					switch (celda.getCellType()) {

					case NUMERIC:
						break;
					case STRING:
						String item = celda.getStringCellValue().trim();
						item = item.toUpperCase();
						item = item.replaceAll(REGEX, "");
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
				if (item.equals("TOTAL_IMPORTE")) {
					cell.setCellValue(valor);
				} else {
					cell.setCellValue(Formateador.importe(valor));
				}

			} else {
				cell.setCellValue(valor);
			}
		} else {
//			cell.setCellType(CellType.STRING);
			cell.setCellValue("");
		}
	}

	private static void cargarFilasEnTabla(XSSFSheet sheet, int contFila, int movimientos) {
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

	private static void copiarFila(XSSFSheet worksheet, int sourceRowNum, int destinationRowNum) {

		XSSFRow newRow = worksheet.getRow(destinationRowNum);
		XSSFRow sourceRow = worksheet.getRow(sourceRowNum);
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
			XSSFCell oldCell = sourceRow.getCell(i);
			XSSFCell newCell = newRow.createCell(i);

			if (oldCell == null) {
				newCell = null;
				continue;
			}

			newCell.setCellStyle(oldCell.getCellStyle());

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

}
