package ar.com.hipotecario.canal.tas.shared.modulos.apis.catalogo.servicios;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;

public class TASRestCatalogo {

    public static Objeto getFechaCalendario(ContextoTAS contexto, String fecha){
        try {
            ApiRequest request = new ApiRequest("API-Catalogo_ConsultaCalendario", "catalogo", "GET", "/v1/calendario/{fecha}", contexto);
            request.path("fecha", fecha);
            request.cache = true;
            ApiResponse response = request.ejecutar();
            ApiException.throwIf(!response.http(200, 204), request, response);
            Objeto resp = new Objeto();
            resp.set("estado", "OK");
            resp.set("respuesta", response);
            return resp;
        } catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    public static Objeto getTiposPFAhorro(ContextoTAS contexto,String idCliente,String operacion, String opcion){
        ApiRequest request = new ApiRequest("ConsultaParametriaPlanAhorroPlazoFijo", "catalogo", "GET", "/v1/plazoFijos/parametrias", contexto);
        request.query("opcion", opcion);
        request.query("operacion", operacion);
        request.query("codCliente", idCliente);
        request.cache = true;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response.codigoHttp == 204 ? null : response;
    }
}

