package ar.com.hipotecario.backend.servicio.api.tarjetasCredito;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.tarjetaDebito.Cuentas;

public class DetalleTarjetasCredito extends ApiObjetos<DetalleTarjetasCredito.DetalleTarjetaCredito> {
    public static class DetalleTarjetaCredito extends ApiObjeto {
        public Fecha vigenciaHasta;
    }

    public static DetalleTarjetasCredito get(Contexto contexto, String numeroTarjeta) {
        ApiRequest request = new ApiRequest("TarjetaCredito", "tarjetasCredito", "GET", "/v1/tarjetascredito/" + numeroTarjeta, contexto);
        request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response.crear(DetalleTarjetasCredito.class);
    }
}
