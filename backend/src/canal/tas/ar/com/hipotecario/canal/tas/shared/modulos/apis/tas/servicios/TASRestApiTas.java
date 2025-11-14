package ar.com.hipotecario.canal.tas.shared.modulos.apis.tas.servicios;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;

public class TASRestApiTas {

    public static Objeto healthApiTas(ContextoTAS contexto){
        try {
            ApiRequest request = new ApiRequest("API-Tas_status", "tas", "GET", "/health", contexto);
            request.cache = true;
            ApiResponse response = request.ejecutar();
            return response;
        } catch (Exception e){
            RespuestaTAS.error(contexto, "TASAplicacion - datosAPI()", e);
            return new Objeto().set("status", "false");
        }
    }
}
