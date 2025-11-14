package ar.com.hipotecario.canal.tas.modulos.tarjetas.utils;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.modulos.tarjetas.services.TASRestTarjetas;

public class UtilesTarjetas {
    public static Objeto consultarEstadosTD(ContextoTAS contexto, Objeto tarjetasDebito){
        try{
            Objeto response = new Objeto();
            for(Object tarjeta : tarjetasDebito.toList()){
                Objeto estadoTD = new Objeto();
                estadoTD.set("numeroTarjeta", tarjeta.toString());
                Objeto responseEstadoTD = TASRestTarjetas.getEstadosTD(contexto, tarjeta.toString());
                if(responseEstadoTD.string("estado").equals("ERROR")) {
                   estadoTD.set("estadoTarjeta", "ERROR");
                }else{
                    estadoTD.set("estadoTarjeta", responseEstadoTD.objeto("estadoTD").string("estadoTarjeta"));
                }
                response.add(estadoTD);
            }
            return response;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    public static Objeto armarResponseEstadoErrorTD(Objeto td){
        Objeto response = new Objeto();
        response.set("tarjetaNumero", td.string("numeroTarjeta"));
        response.set("flagPIL", false);
        response.set("flagPIN", false);
        response.set("flagHAB", false);
        return response;
    }
    public static Objeto armarResponseEstadosTD(Objeto td){
        Objeto response = new Objeto();
        response.set("tarjetaNumero", td.string("numeroTarjeta"));
        String estado = td.string("estadoTarjeta");
        switch (estado){
            case "INACTIVA", "BAJA_POR_EXTRAVIO", "BAJA_POR_ROBO", "BLOQUEADA", "RESTRINGIDA_A_DEPOSITO":
                response.set("flagPIL", true);
                response.set("flagPIN", false);
                response.set("flagHAB", true);
                break;
            case "BAJA_FISICA":
                response.set("flagPIL", false);
                response.set("flagPIN", false);
                response.set("flagHAB", false);
                break;
            case "HABILITADA":
                response.set("flagPIL", true);
                response.set("flagPIN", true);
                response.set("flagHAB", false);
                break;
            default:
                response.set("flagPIL", true);
                response.set("flagPIN", false);
                response.set("flagHAB", false);
                break;
        }
        return response;
    }

    public static String getTipoTarjetaFromDetalle(ContextoTAS contexto, String numeroTD){
        Objeto responseDetalle = TASRestTarjetas.getDetalleTDById(contexto, numeroTD);
        String tipoTD = responseDetalle.string("estado").equals("OK") ?
                responseDetalle.objeto("respuesta").string("tipoTarjeta")
                : "SIN_RESULTADOS";
        return tipoTD;
    }
}
