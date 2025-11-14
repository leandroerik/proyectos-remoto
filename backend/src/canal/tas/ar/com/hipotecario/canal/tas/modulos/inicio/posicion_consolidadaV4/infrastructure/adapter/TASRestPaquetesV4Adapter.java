package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.adapter;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.port.TASPaquetesV4Port;

public class TASRestPaquetesV4Adapter implements TASPaquetesV4Port {
    @Override
    public Objeto getDatosPaquetes(ContextoTAS contexto, String idCliente, String historico) {
        ///v1/infoPaquetes?historico=N&idCliente=630777
        ApiRequest request = new ApiRequest("ConsultaPaquete", "paquetes", "GET", "/v1/infoPaquetes", contexto);
        request.query("idCliente", idCliente);
        request.query("historico", historico);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response;
    }
}
