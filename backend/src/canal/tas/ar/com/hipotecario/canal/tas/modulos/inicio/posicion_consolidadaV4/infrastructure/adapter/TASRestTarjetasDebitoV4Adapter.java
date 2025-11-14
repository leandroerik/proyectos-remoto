package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.adapter;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.port.TASTarjetasDebitoV4Port;

public class TASRestTarjetasDebitoV4Adapter implements TASTarjetasDebitoV4Port {
    @Override
    public Objeto getDatosTD(ContextoTAS contexto, String idCliente, String tipoEstado) {
        ApiRequest request = new ApiRequest("ConsultaConsolidadaTarjetaDeDebito", "tarjetasdebito", "GET", "/v2/tarjetasdebito", contexto);
        request.query("idcliente", idCliente);
        request.query("tipoestado", tipoEstado);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response;
    }
}
