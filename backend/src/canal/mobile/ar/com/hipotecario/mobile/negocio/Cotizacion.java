package ar.com.hipotecario.mobile.negocio;

import java.math.BigDecimal;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.servicio.InversionesService;

public class Cotizacion {

	public static BigDecimal dolarCompra(ContextoMB contexto) {
		ApiResponseMB response = InversionesService.inversionesGetCotizaciones(contexto);
		if (!response.hayError()) {
			for (Objeto item : response.objetos()) {
				if ("2".equals(item.string("moneda"))) {
					return item.bigDecimal("compra");
				}
			}
		}
		return null;
	}

	public static BigDecimal dolarVenta(ContextoMB contexto) {
		ApiResponseMB response = InversionesService.inversionesGetCotizaciones(contexto);
		if (!response.hayError()) {
			for (Objeto item : response.objetos()) {
				if ("2".equals(item.string("moneda"))) {
					return item.bigDecimal("venta");
				}
			}
		}
		return null;
	}

	public static BigDecimal euroCompra(ContextoMB contexto) {
		ApiResponseMB response = InversionesService.inversionesGetCotizaciones(contexto);
		if (!response.hayError()) {
			for (Objeto item : response.objetos()) {
				if ("98".equals(item.string("moneda"))) {
					return item.bigDecimal("compra");
				}
			}
		}
		return null;
	}

	public static BigDecimal euroVenta(ContextoMB contexto) {
		ApiResponseMB response = InversionesService.inversionesGetCotizaciones(contexto);
		if (!response.hayError()) {
			for (Objeto item : response.objetos()) {
				if ("98".equals(item.string("moneda"))) {
					return item.bigDecimal("venta");
				}
			}
		}
		return null;
	}

	public static BigDecimal uvaCompra(ContextoMB contexto) {
		ApiResponseMB response = InversionesService.inversionesGetCotizaciones(contexto, "88");
		if (!response.hayError()) {
			for (Objeto item : response.objetos()) {
				return item.bigDecimal("valorCotizacion");
			}
		}
		return null;
	}

	public static BigDecimal uvaVenta(ContextoMB contexto) {
		ApiResponseMB response = InversionesService.inversionesGetCotizaciones(contexto, "88");
		if (!response.hayError()) {
			for (Objeto item : response.objetos()) {
				return item.bigDecimal("valorCotizacion");
			}
		}
		return null;
	}
}
