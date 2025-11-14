package ar.com.hipotecario.canal.tas.modulos.cajasseguridad.servicios;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;

public class TASRestCajasSeguridad {
    public static Objeto getCajasSeguridad(ContextoTAS contexto, String idCaja){
        try{
            ApiRequest request = new ApiRequest("ConsultaCajaSeguridad", "cajasseguridad", "GET", "/v1/cajasseguridad/{idcajaseguridad}", contexto);
            request.path("idcajaseguridad", idCaja);
            request.cache = false;

            ApiResponse response = request.ejecutar();
            ApiException.throwIf(!response.http(200, 204), request, response);
            if(response.codigoHttp == 204) new Objeto().set("estado", "SIN_RESULTADOS");
            Objeto resp = new Objeto();
            resp.set("estado", "OK");
            resp.set("detalleCS", response);
            return resp;
        }catch (Exception e){
            Objeto error = new Objeto();
            LogTAS.error(contexto, e);
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }
}
