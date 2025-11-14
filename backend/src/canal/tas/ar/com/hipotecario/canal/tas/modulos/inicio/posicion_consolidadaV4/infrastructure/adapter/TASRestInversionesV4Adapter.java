package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.adapter;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.port.TASInversionesV4Port;

public class TASRestInversionesV4Adapter implements TASInversionesV4Port {
    @Override
    public Objeto getDatosInversiones(ContextoTAS contexto, String idCliente, String estado, boolean firmantes) {
        //"/v2/cuentascomitentes?idcliente=135706&tipoestado=vigente&firmantes=false"
        ApiRequest request = new ApiRequest("CuentasComitentesDetalle", "inversiones", "GET", "/v2/cuentascomitentes", contexto);
        request.query("idcliente", idCliente);
        request.query("tipoestado", estado);
        request.query("firmantes", firmantes);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response;
    }
}
