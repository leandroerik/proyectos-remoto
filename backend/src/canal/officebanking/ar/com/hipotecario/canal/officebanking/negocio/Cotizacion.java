package ar.com.hipotecario.canal.officebanking.negocio;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

import ar.com.hipotecario.backend.servicio.api.inversiones.Cotizaciones;

public class Cotizacion {

	public static BigDecimal dolarCompra(ContextoOB contexto) {
		String idCobis = contexto.sesion().empresaOB.idCobis;
		Cotizaciones cotizacion;
		String canal = "HB_BE";
		try {
			cotizacion = Cotizaciones.getWithCanal(contexto, idCobis, "6", canal);	
			
			for (Cotizaciones.Cotizacion item : cotizacion) {
				if ("2".equals(item.idMoneda)) {
					return new BigDecimal(item.compra);
				}
			}
		} catch (ApiException e) {
			return null;
		}
		return null;
		
	}

	public static BigDecimal dolarVenta(ContextoOB contexto) {
		String idCobis = contexto.sesion().empresaOB.idCobis;
		Cotizaciones cotizacion;
		String canal = "HB_BE";
		try {
			cotizacion = Cotizaciones.getWithCanal(contexto, idCobis, "6", canal);	
			
			for (Cotizaciones.Cotizacion item : cotizacion) {
				if ("2".equals(item.idMoneda)) {
					return new BigDecimal(item.venta);
				}
			}
		} catch (ApiException e) {
			return null;
		}
		return null;
	}
}
