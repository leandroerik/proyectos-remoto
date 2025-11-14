package ar.com.hipotecario.canal.officebanking.util;

import ar.com.hipotecario.backend.servicio.api.inversiones.ApiInversiones;
import ar.com.hipotecario.backend.servicio.api.inversiones.Cotizaciones;
import ar.com.hipotecario.backend.servicio.api.inversiones.CotizacionesOb;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

import java.math.BigDecimal;

public class CotizacionesDivisas {

    static final String DOLAR="2";
    static final String EURO="98";
    static final String COMPRA="compra";
    static final String VENTA="venta";

    public static BigDecimal obtenerCotizacion(ContextoOB contexto, String moneda, String operacion){
        BigDecimal cotizacion = BigDecimal.ZERO;
        try{
            switch (moneda.toUpperCase()) {
                case DOLAR:
                    if (COMPRA.equalsIgnoreCase(operacion)) {
                        cotizacion = dolarCompra(contexto);
                    } else if (VENTA.equalsIgnoreCase(operacion)) {
                        cotizacion = dolarVenta(contexto);
                    }
                    break;

                case EURO:
                    if (COMPRA.equalsIgnoreCase(operacion)) {
                        cotizacion = euroCompra(contexto);
                    } else if (VENTA.equalsIgnoreCase(operacion)) {
                        cotizacion = euroVenta(contexto);
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Moneda no soportada: " + moneda);
            }
        } catch (Exception e){
            return BigDecimal.valueOf(1);
        }

        return cotizacion;
    }

    public static BigDecimal dolarCompra(ContextoOB contexto) {
        BigDecimal compra=BigDecimal.ZERO;
        CotizacionesOb cotizaciones = ApiInversiones.cotizacionesOb(contexto, contexto.sesion().idCobis, "6").get();
        if(cotizaciones!=null){
            compra = cotizaciones.stream().filter(m ->m.idMoneda.equals(DOLAR)).findFirst().get().compra;
        }
        return compra;
    }

    public static BigDecimal dolarVenta(ContextoOB contexto) {
        BigDecimal compra=BigDecimal.ZERO;
        CotizacionesOb cotizaciones = ApiInversiones.cotizacionesOb(contexto, contexto.sesion().idCobis, "6").get();
        if(cotizaciones!=null){
            compra = cotizaciones.stream().filter(m ->m.idMoneda.equals(DOLAR)).findFirst().get().venta;
        }
        return compra;
    }

    public static BigDecimal euroCompra(ContextoOB contexto) {
        BigDecimal compra=BigDecimal.ZERO;
        CotizacionesOb cotizaciones = ApiInversiones.cotizacionesOb(contexto, contexto.sesion().idCobis, "6").get();
        if(cotizaciones!=null){
            compra = cotizaciones.stream().filter(m ->m.idMoneda.equals(EURO)).findFirst().get().compra;
        }
        return compra;
    }

    public static BigDecimal euroVenta(ContextoOB contexto) {
        BigDecimal compra=BigDecimal.ZERO;
        CotizacionesOb cotizaciones = ApiInversiones.cotizacionesOb(contexto, contexto.sesion().idCobis, "6").get();
        if(cotizaciones!=null){
            compra = cotizaciones.stream().filter(m ->m.idMoneda.equals(EURO)).findFirst().get().venta;
        }
        return compra;
    }
}
