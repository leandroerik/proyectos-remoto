package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.adapter;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.port.TASCajasSeguridadV4Port;

public class TASRestCajasSeguridadV4Adapter implements TASCajasSeguridadV4Port {
    @Override
    public Objeto getDatosCajas(ContextoTAS contexto, String idCliente, boolean cancelados) {
        ApiRequest request = new ApiRequest("ConsultaCajaSeguridadDeCliente", "cajasseguridad", "GET", "/v1/cajasseguridad", contexto);
        request.query("idcliente", idCliente);
        request.query("cancelados", String.valueOf(cancelados));
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response;
    }

}
