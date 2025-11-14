package ar.com.hipotecario.canal.tas.shared.modulos.apis.scheduler.controller;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.scheduler.utiles.UtilesScheduler;

public class TASSchedulerController {

    public static Objeto postProgramarTarea(ContextoTAS contexto){
        return null;
    }
    //TODO: terminar servicio de programar pago....
    public static Objeto programarPagoTarjeta(ContextoTAS contexto, Objeto datosPago){
        try{
            Objeto datosParaAgendar = UtilesScheduler.armarDatosParaAgendarPagoTarjeta(contexto, datosPago);
            if(!datosParaAgendar.string("estado").equals("OK")) return new Objeto().set("estado", "ERROR_PARAMETROS");
            Objeto fechaEjecucion = UtilesScheduler.obtenerFechaEjecucion(contexto, "PAGOTARJETA");
            return null;
        }catch (Exception e){
            return null;
        }
    }
}
