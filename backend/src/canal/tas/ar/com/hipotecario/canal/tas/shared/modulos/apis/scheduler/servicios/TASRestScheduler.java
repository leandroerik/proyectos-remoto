package ar.com.hipotecario.canal.tas.shared.modulos.apis.scheduler.servicios;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;

public class TASRestScheduler {



    public static Objeto getHorarioBancarioByidentificador (ContextoTAS contexto, String identificador){
        ApiRequest request = new ApiRequest("HorarioBancarioIdentificador","scheduler", "GET", "/v1/horariobancario/identificador/{identificador}", contexto);
        request.path("identificador", identificador);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200) || response.isEmpty(), request, response);
        return response;
    }
}
