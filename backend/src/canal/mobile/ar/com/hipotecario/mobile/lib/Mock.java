package ar.com.hipotecario.mobile.lib;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Set;

import ar.com.hipotecario.mobile.conector.ApiResponseMB;

public class Mock {

	public static ApiResponseMB resumenCuentaTCMock(ApiResponseMB responseResumen, String cobis) {
		Set<String> cobisMock = Objeto.setOf("6090851_4478435_730_762_6041602_803_850433_5182519_3763892_5498733".split("_"));
		if (cobisMock.contains(cobis)) {
			switch (cobis) {
			case "6090851":
				// Estamos preparando tu resumen
				return replaceResumenCuentaTC(responseResumen, 34, 30, 9, -1);
			case "6041602":
				// Tu resumen esta listo
				return replaceResumenCuentaTC(responseResumen, 34, 30, 9, -3);
			case "4478435":
				// Faltan 3 dias para tu vencimiento
				return replaceResumenCuentaTC(responseResumen, 34, 30, 3, -8);
			case "730":
				// Faltan 2 dias para tu vencimiento
				return replaceResumenCuentaTC(responseResumen, 34, 30, 2, -8);
			case "762":
				// Mañana vence tu tarjeta
				return replaceResumenCuentaTC(responseResumen, 34, 30, 1, -8);
			case "803":
				// Hoy vence tu tarjeta
				return replaceResumenCuentaTC(responseResumen, 34, 30, 0, -8);
			case "850433":
				// verificando el pago
				return replaceResumenCuentaTC(responseResumen, 34, 30, -1, -8);
			case "3763892":
				// pago parcial
				return replaceResumenCuentaTC(responseResumen, 34, 30, 6, -8);
			case "5182519":
				// verificando total
				return replaceResumenCuentaTC(responseResumen, 34, 30, 6, -8);
			case "5498733":
				// pasaron 7 dias del pago
				return replaceResumenCuentaTC(responseResumen, 34, 30, -8, -16);
			default:
				return replaceResumenCuentaTC(responseResumen, 34, 30, 9, -3);
			}
		}
		return responseResumen;
	}

	public static ApiResponseMB replaceResumenCuentaTC(ApiResponseMB response, Integer proximoLiqVenc, Integer proximoLiqcierre, Integer ultimaLiqVenc, Integer ultimaLiqcierre) {
		DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		String fechaActualString = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		String json = "";
		try {
			json = response.json.replace("proximoLiqVenc", LocalDate.parse(fechaActualString, formateador).plusDays(proximoLiqVenc).format(formateador)).replace("proximoLiqcierre", LocalDate.parse(fechaActualString, formateador).plusDays(proximoLiqcierre).format(formateador)).replace("ultimaLiqVenc", LocalDate.parse(fechaActualString, formateador).plusDays(ultimaLiqVenc).format(formateador)).replace("ultimaLiqcierre", LocalDate.parse(fechaActualString, formateador).plusDays(ultimaLiqcierre).format(formateador));
			return new ApiResponseMB(null, response.codigo, json);
		} catch (Exception e) {
			return new ApiResponseMB(null, 204, json);
		}
	}

	public static String aumentarFecha(String fechaString, int cantidadDias) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDate fecha = LocalDate.parse(fechaString, formatter);
		return fecha.plusDays(cantidadDias).format(formatter);
	}

}
