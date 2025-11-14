package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.controller;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidada.controllers.TASPosicionConsolidadaController;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.useCase.TASPosicionConsolidadaV4UseCase;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.config.TASPosicionConsolidadaV4Initializer;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;

public class TASPosicionConsolidadaV4Controller {

    public static void init(){
    }
    static {
        TASPosicionConsolidadaV4Initializer.init();
    }

    public static Objeto getPosicionConsolidadaV4(ContextoTAS contexto) {
        try {
            String idCliente = contexto.parametros.string("idCliente");
            if (idCliente.isEmpty()) return RespuestaTAS.sinParametros(contexto, "parametro no ingresado");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String kioscoSesion = contexto.sesion().getIdTas();
            String idKiosco = contexto.config.string("tas_kiosco_id");
            if(idKiosco.equals(kioscoSesion)){
                LogTAS.evento(contexto, "TAS 125 - Posicion Consolidada v4");
                Objeto responseConsolidada = TASPosicionConsolidadaV4UseCase.getPosicionConsolidadaV4(contexto, idCliente);
                if(responseConsolidada.string("estado").equals("SIN_RESULTADOS")) return RespuestaTAS.sinResultados(contexto,"Sin resultados para el cliente seleccionado");
                if(responseConsolidada.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASPosicionConsolidadaV4Controller - Metodo getPosicionConsolidadaV4()",(Exception) responseConsolidada.get("error"));
                Objeto response = responseConsolidada.objeto("respuesta");
                return response;
            }else if( idKiosco.equals("TODOS")){
                Objeto responseConsolidada = TASPosicionConsolidadaV4UseCase.getPosicionConsolidadaV4(contexto, idCliente);
                if(responseConsolidada.string("estado").equals("SIN_RESULTADOS")) return RespuestaTAS.sinResultados(contexto,"Sin resultados para el cliente seleccionado");
                if(responseConsolidada.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASPosicionConsolidadaV4Controller - Metodo getPosicionConsolidadaV4()",(Exception) responseConsolidada.get("error"));
                Objeto response = responseConsolidada.objeto("respuesta");
                return response;
            } else {
                return TASPosicionConsolidadaController.getPosicionConsolidada(contexto);
            }
        } catch (Exception e) {
            return RespuestaTAS.error(contexto,"TASPosicionConsolidadaV4Controller - Metodo getPosicionConsolidadaV4()", e);
        }
    }
}
