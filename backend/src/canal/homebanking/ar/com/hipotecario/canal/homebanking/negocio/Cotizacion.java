package ar.com.hipotecario.canal.homebanking.negocio;

import java.math.BigDecimal;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.servicio.InversionesService;

public class Cotizacion {

	public static BigDecimal dolarCompra(ContextoHB contexto, ApiResponse response) {
		if (!response.hayError()) {
			for (Objeto item : response.objetos()) {
				if ("2".equals(item.string("moneda"))) {
					return item.bigDecimal("compra");
				}
			}
		}
		return null;
	}

	public static BigDecimal dolarVenta(ContextoHB contexto, ApiResponse response) {
		if (!response.hayError()) {
			for (Objeto item : response.objetos()) {
				if ("2".equals(item.string("moneda"))) {
					return item.bigDecimal("venta");
				}
			}
		}
		return null;
	}

	public static BigDecimal euroCompra(ContextoHB contexto, ApiResponse response) {
		if (!response.hayError()) {
			for (Objeto item : response.objetos()) {
				if ("98".equals(item.string("moneda"))) {
					return item.bigDecimal("compra");
				}
			}
		}
		return null;
	}

	public static BigDecimal euroVenta(ContextoHB contexto, ApiResponse response) {
		if (!response.hayError()) {
			for (Objeto item : response.objetos()) {
				if ("98".equals(item.string("moneda"))) {
					return item.bigDecimal("venta");
				}
			}
		}
		return null;
	}

	public static BigDecimal uvaCompra(ContextoHB contexto, ApiResponse response) {
		if (!response.hayError()) {
			for (Objeto item : response.objetos()) {
				return item.bigDecimal("valorCotizacion");
			}
		}
		return null;
	}

	public static BigDecimal uvaVenta(ContextoHB contexto, ApiResponse response) {
		if (!response.hayError()) {
			for (Objeto item : response.objetos()) {
				return item.bigDecimal("valorCotizacion");
			}
		}
		return null;
	}
}
