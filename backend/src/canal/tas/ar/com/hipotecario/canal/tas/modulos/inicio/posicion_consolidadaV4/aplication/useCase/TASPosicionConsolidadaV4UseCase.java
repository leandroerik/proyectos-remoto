package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.useCase;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.assembler.TASPosicionConsolidadaV4Assembler;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.mapper.TASTarjetasCreditoV4Mapper;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.port.TASPosicionConsolidadaV4Port;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.dto.TASPosicionConsolidadaV4DTO;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASProductosGenericosV4;

import java.util.ArrayList;
import java.util.List;

public class TASPosicionConsolidadaV4UseCase {
    private static TASPosicionConsolidadaV4Port port;
    public static void init(TASPosicionConsolidadaV4Port injectedPort) {
        port = injectedPort;
    }
    public static Objeto getPosicionConsolidadaV4(ContextoTAS contexto, String idCliente){
        try {
            Objeto objResponse = new Objeto();
            Objeto responsePCons = port.getPosicionConsolidadaV4(contexto, idCliente);
            int codigoResponse = 0;
            if(responsePCons instanceof ApiResponse){
                ApiResponse response = (ApiResponse) responsePCons;
                codigoResponse = response.codigoHttp;
            }
            if(codigoResponse == 204) return objResponse.set("estado", "SIN_RESULTADOS");
            TASPosicionConsolidadaV4DTO productosResponse = TASPosicionConsolidadaV4Assembler.crearModeloProductos(contexto, responsePCons);
            Objeto consolidadaTC = port.getConsolidadaTCV4(contexto, idCliente);
            productosResponse.setTarjetasCredito(consolidadaTC.string("estado").equals("ERROR")
                    || consolidadaTC.string("estado").equals("SIN_RESULTADOS") ? null
                    : TASTarjetasCreditoV4Mapper.mapearTC(consolidadaTC.objeto("respuesta")));
            objResponse.set("estado", "OK");
            objResponse.set("respuesta", TASPosicionConsolidadaV4Assembler.recuperarDatos(contexto, idCliente, productosResponse));
            return objResponse;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }
}
