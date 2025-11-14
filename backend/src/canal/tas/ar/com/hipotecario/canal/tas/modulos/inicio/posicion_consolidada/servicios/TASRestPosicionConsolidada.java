package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidada.servicios;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;

public class TASRestPosicionConsolidada {

    public static Objeto getPosicionConsolidada(ContextoTAS contexto, String idCobis){
        ApiRequest request = new ApiRequest("PosicionConsolidada", "productos", "GET", "/v3/posicionconsolidada", contexto);
        request.query("idcliente", idCobis);
        request.query("tipoestado", "vigente");
        request.body("ip", contexto.ip());
		
        request.cache = false;
        
        ApiResponse response = request.ejecutar();
        ApiException.throwIf("CLIENTE_INVALIDO", response.contains("NOT_VALID_CLIENT"), request, response);
        ApiException.throwIf(!response.http(200, 204), request, response);
        
        return Objeto.fromJson(response.toJson());
    }
}
