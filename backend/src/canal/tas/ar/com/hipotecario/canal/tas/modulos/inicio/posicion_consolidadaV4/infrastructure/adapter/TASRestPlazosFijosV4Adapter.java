package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.adapter;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.port.TASPlazosFijosV4Port;

public class TASRestPlazosFijosV4Adapter implements TASPlazosFijosV4Port {
    @Override
    public Objeto getDatosPF(ContextoTAS contexto, String idCliente, String tipoEstado, boolean inversiones) {
        ///v2/plazosfijos?idcliente=630777&inversiones=false&tipoestado=vigente
        ApiRequest request = new ApiRequest("ConsultaListadoPlazoFijo", "plazosfijos", "GET", "/v2/plazosfijos", contexto);
        request.query("idcliente", idCliente);
        request.query("tipoestado", tipoEstado);
        request.query("inversiones", inversiones);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response;
    }
}
