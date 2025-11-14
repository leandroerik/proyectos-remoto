package ar.com.hipotecario.canal.officebanking;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Resource;

public class ExcelOB {

	public static byte[] descargaMovimientos(String plantilla, Map<String, Object> parametros) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream stream = Resource.stream(plantilla);
		try (XSSFWorkbook documento = new XSSFWorkbook(stream)) {
			XSSFSheet hoja = documento.getSheet("Ultimos Movimientos");

			set(hoja, 3, 1, parametros.get("CUENTA").toString());
			set(hoja, 1, 9, parametros.get("TITULO").toString());
			set(hoja, 6, 14, parametros.get("SUCURSAL").toString());
			set(hoja, 6, 16, parametros.get("REFERENCIA").toString());
			set(hoja, 6, 22, parametros.get("DEBITO_MONEDA").toString());
			set(hoja, 6, 25, parametros.get("CREDITO_MONEDA").toString());
			set(hoja, 6, 28, parametros.get("SALDO_MONEDA").toString());

			Objeto movimientos = (Objeto) parametros.get("MOVIMIENTOS");
			List<Objeto> lstMovimientos = movimientos.objetos();

			DecimalFormatSymbols symbols = new DecimalFormatSymbols();
			symbols.setGroupingSeparator('.');
			symbols.setDecimalSeparator(',');
			DecimalFormat df = new DecimalFormat ("#,##0.00", symbols);

			Integer filaActual = 7;
			for (Objeto movimiento : lstMovimientos) {
				String debitoFormateado = df.format(new BigDecimal(movimiento.get("debito").toString()));
				String creditoFormateado = df.format(new BigDecimal(movimiento.get("credito").toString()));
				String saldoFormateado = "";
				if(movimiento.get("saldo") != null){
					saldoFormateado = df.format(new BigDecimal(movimiento.get("saldo").toString()));
				}
				duplicarFila(hoja, filaActual);
				set(hoja, filaActual, 1, movimiento.get("fecha").toString());
				set(hoja, filaActual, 3, movimiento.get("descripcion").toString());
				set(hoja, filaActual, 14, movimiento.get("sucursal").toString());
				set(hoja, filaActual, 16, movimiento.get("referencia").toString());
				set(hoja, filaActual, 22, debitoFormateado.equals("0,00") ? "" : debitoFormateado);
				set(hoja, filaActual, 25, creditoFormateado.equals("0,00") ? "" : creditoFormateado);
				set(hoja, filaActual, 28, saldoFormateado);
				++filaActual;
			}
			eliminarFila(hoja, filaActual++);

			String totalDebitoFormateado = df.format(new BigDecimal(parametros.get("TOTAL_DEBITO").toString()));
			String totalCreditoFormateado = df.format(new BigDecimal(parametros.get("TOTAL_CREDITO").toString()));

			set(hoja, filaActual, 22, totalDebitoFormateado);
			set(hoja, filaActual, 25, totalCreditoFormateado);

			documento.write(baos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return baos.toByteArray();
	}

	private static void set(Sheet hoja, Integer fila, Integer columna, String valor) {
		Cell celda = hoja.getRow(fila).getCell(columna);
		if (celda == null) {
			celda = hoja.getRow(fila).createCell(columna);
		}
		celda.setCellValue(valor);
	}

	private static void duplicarFila(XSSFSheet hoja, Integer fila) {
		hoja.shiftRows(fila, hoja.getLastRowNum(), 1);
		hoja.copyRows(fila + 1, fila + 1, fila, new CellCopyPolicy());
	}

	private static void eliminarFila(XSSFSheet hoja, Integer fila) {
		hoja.removeRow(hoja.getRow(fila));
	}

}
