package ar.com.hipotecario.canal.tas.modulos.cuentas.transferencias.utils;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.modulos.cuentas.transferencias.controllers.TASTransferenciasController;
import ar.com.hipotecario.canal.tas.modulos.cuentas.transferencias.modelos.TASTransferenciasPropia;
import ar.com.hipotecario.canal.tas.modulos.cuentas.transferencias.services.TASRestTransferencias;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.inversiones.servicios.TASRestInversiones;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.scheduler.servicios.TASRestScheduler;

import java.math.BigDecimal;
import java.util.Date;

public class UtilesTransferencias {

    public static Objeto comprobarHorario(ContextoTAS contexto){
        try{
            String idMonedaOrigen = contexto.parametros.string("idMonedaOrigen","80");
            String idMonedaDestino = contexto.parametros.string("idMonedaDestino","80");
            String tipoIdentificador = idMonedaOrigen.equalsIgnoreCase(idMonedaDestino) ?
                    "TRANSFERENCIAINTRABANCARIA" : "BIMONETARY";
            Objeto horariosBancarios = TASRestScheduler.getHorarioBancarioByidentificador(contexto, tipoIdentificador);
            Objeto procesarHorario = procesarHorarioBancario(horariosBancarios, tipoIdentificador);
            if(procesarHorario.string("validacion").equals("false")) {
                Objeto fueraHorario = new Objeto();
                fueraHorario.set("estado", "FUERA_HORARIO");
                fueraHorario.set("mensaje", "Esta operación está disponible todos los días de "
                        + procesarHorario.string("horaInicio") + " a " + procesarHorario.string("horaFin") + " hs.");
                return fueraHorario;
            }
            return new Objeto().set("estado", "false");
        }catch(Exception e){
            Objeto error = new Objeto().set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    private static Objeto procesarHorarioBancario(Objeto horariosBancarios, String identificador){
        Objeto horario = horariosBancarios.objetos().size() > 1 ?
             horariosBancarios.objetos()
                    .stream().filter(horarios ->
                            horarios.string("identificador").equals(identificador))
                    .findFirst().orElse(null) :
        horariosBancarios.objetos().get(0);
        Fecha fechaHoy = new Fecha(new Date());
        Integer horaHoy = fechaHoy.hora();
        Integer minHoy = fechaHoy.minuto();
        Integer bancoHoraInicio = Integer.valueOf(horario.string("horaInicio"));
        Integer bancoMinInicio = Integer.valueOf(horario.string("minutoInicio"));
        Integer bancoHoraFin = Integer.valueOf(horario.string("horaFin"));
        Integer bancoMinFin = Integer.valueOf(horario.string("minutoFin"));
        Objeto response = new Objeto();
        if ((horaHoy == bancoHoraInicio && minHoy >= bancoMinInicio || horaHoy > bancoHoraInicio) && (horaHoy == bancoHoraFin && minHoy <= bancoMinFin || horaHoy < bancoHoraFin)) {
            response.set("validacion","ok");
        } else {
            response.set("validacion","false");
            response.set("horaInicio" , horario.string("horaInicio")+":"+horario.string("minutoInicio"));
            response.set("horaFin" , horario.string("horaFin")+":"+horario.string("minutoFin"));
        }
        return response;
    }

    public static Objeto simularCV(ContextoTAS contexto, TASTransferenciasPropia datoscv){
        try{
            Objeto responseSimulador = TASRestTransferencias.postSimularTransferenciaCV(contexto, datoscv);
            return responseSimulador;
        }catch (Exception e){
            // capturo tipo error si es "121013", genero mock
//            if(e instanceof ApiException){
//                Objeto error = new Objeto();
//                Objeto response = new Objeto();
//                ApiException apiEx = (ApiException) e;
//                String codigo = apiEx.response.string("codigo");error.set("estado", "ERROR");
//                error.set("error", apiEx);
//                response = codigo.equals("121013") ? TASTransferenciasController.generarMock() : error;
//                return response;
//            }
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    public static TASTransferenciasPropia modificarParametrosCompraVenta(ContextoTAS contexto, TASTransferenciasPropia datosCV, BigDecimal cotDolar){
        TASTransferenciasPropia datosModificados = new TASTransferenciasPropia();
        try{
            datosModificados = datosCV;
            //si la cotizacion no vino por parametro hago las cuentas con la obtenida de la api
            if(datosCV.getCotizacion().equals("0")) {
                datosModificados.setCotizacion(cotDolar);
                BigDecimal importeDivisa = datosCV.getIdMonedaOrigen() == 80 ? datosCV.getImporteDestino() : datosCV.getImporte();
                datosModificados.setImporteDivisa(importeDivisa);
                BigDecimal importePesos = datosCV.getIdMonedaOrigen() == 80 ? datosCV.getImporte() : datosCV.getImporteDestino();
                datosModificados.setImportePesos(importePesos.multiply(cotDolar));
                datosModificados.setMontoEnDivisa(datosModificados.getImporteDivisa());
                return datosModificados;
            }
            //esto solo sucede si no vienen los datos por parametro del front
            if(datosCV.getImporteDivisa().equals("0") || datosCV.getImportePesos().equals("0") || datosCV.getMontoEnDivisa().equals("0")){
                Objeto responseSimulador = simularCV(contexto,datosCV);
                datosModificados.setImporteDivisa(responseSimulador.bigDecimal("importeDivisa"));
                datosModificados.setImportePesos(responseSimulador.bigDecimal("importePesos"));
                datosModificados.setTransaccion(Integer.valueOf(responseSimulador.string("transaccion")));
                datosModificados.setMontoEnDivisa(responseSimulador.bigDecimal("importeDivisa"));

            }
            return datosModificados;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            datosCV.setError(error);
            datosModificados.setError(error);
            return datosCV;
        }
    }
}
