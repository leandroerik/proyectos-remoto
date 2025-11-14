package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.adapter;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.port.TASPrestamosV4Port;

public class TASRestPrestamosV4Adapter implements TASPrestamosV4Port {
    @Override
    public Objeto getDatosPrestamo(ContextoTAS contexto, String idCliente, String tipoEstado, boolean buscaNsp) {
        //"/v2/prestamos?idcliente=135706&tipoestado=vigente&buscansp=false"
        ApiRequest request = new ApiRequest("PosicionConsolidadaPrestamos", "prestamos", "GET", "/v2/prestamos", contexto);
        request.query("idcliente", idCliente);
        request.query("tipoestado", tipoEstado);
        request.query("buscansp", buscaNsp);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response;
    }
}
