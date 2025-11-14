package ar.com.hipotecario.canal.tas.shared.modulos.apis.scheduler.utiles;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.tarjetas.services.TASRestTarjetas;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.scheduler.servicios.TASRestScheduler;

public class UtilesScheduler {

    /*
    * schedTarReq.setEnviaEmail(false);
		schedTarReq.setFrecuencia(1);
		schedTarReq.setIdentificadorHorarioEjecucion(Mensajes.GENERAL);
		schedTarReq.setIdentificadorPeriodicidad(Mensajes.UNICA);
		schedTarReq.setMetodo(IDENTIFICADOR_PAGOTARJETA);
		schedTarReq.setNumeroMaxIntento(1);
		schedTarReq.setProtocolo(HttpMethod.POST.toString());
		schedTarReq.setRequest(ptr.toString());
		schedTarReq.setServicio(Mensajes.API_SERVICIOS);
		schedTarReq.setTecnologia("api-rest");
		schedTarReq.setTipoRequest("json");
		schedTarReq.setUrl
    * */
    public static Objeto armarDatosParaAgendarPagoTarjeta(ContextoTAS contexto,Objeto datosPago){
        try{
        Objeto datosAgendar = new Objeto();
        datosAgendar.set("enviaEmail",false);
        datosAgendar.set("frecuencia",1);
        datosAgendar.set("identificadorHorarioEjecucion","GENERAL");
        datosAgendar.set("identificadorPeriodicidad","UNICA");
        datosAgendar.set("metodo","PAGOTARJETA");
        datosAgendar.set("numeroMaxIntento",1);
        datosAgendar.set("protocolo","POST");
        datosAgendar.set("request",datosPago.toString());//! ojo acaaaaaaaa
        datosAgendar.set("servicio","Api-servicios");
        datosAgendar.set("tecnologia","api-rest");
        datosAgendar.set("tipoRequest","json");
        datosAgendar.set("url",contexto.config.string("backend_api_servicios") + "/api/tarjeta/pagoTarjeta");
        datosAgendar.set("estado", "OK");
        return datosAgendar;
        } catch (Exception e){
            return new Objeto().set("estado", "ERROR");
        }
    }

    //todo: terminar servicio
    public static Objeto obtenerFechaEjecucion(ContextoTAS contexto, String identificador){
        Objeto fechas = TASRestScheduler.getHorarioBancarioByidentificador(contexto, identificador);
        return fechas;
    }
}
