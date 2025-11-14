package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.adapter;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.port.TASPosicionConsolidadaV4Port;

public class TASRestPosicionConsolidadaV4Adapter implements TASPosicionConsolidadaV4Port {

    @Override
    public Objeto getPosicionConsolidadaV4(ContextoTAS contexto, String idCliente){
        ApiRequest request = new ApiRequest("PosicionConsolidada", "productos", "GET", "/v4/posicionconsolidada", contexto);
        request.query("idcliente", idCliente);
        request.query("tipoestado", "vigente");
        request.body("ip", contexto.ip());

        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf("CLIENTE_INVALIDO", response.contains("NOT_VALID_CLIENT"), request, response);
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response;
    }

    @Override
    public Objeto getConsolidadaTCV4(ContextoTAS contexto, String idCliente){
        try {
            ApiRequest request = new ApiRequest("ConsultaTarjetasCreditoCliente", "tarjetascredito", "GET", "/v1/tarjetascredito", contexto);
            request.query("idcliente", idCliente);
            request.query("adicionales", true);
            request.query("cancelados", false);
            request.cache = false;

            ApiResponse response = request.ejecutar();
            ApiException.throwIf(!response.http(200, 204), request, response);
            Objeto respuesta = new Objeto();
            respuesta.set("estado", "OK");
            respuesta.set("respuesta", response);
            return response.codigoHttp == 204 ? new Objeto().set("estado", "SIN_RESULTADOS") : respuesta;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }
}
