package ar.com.hipotecario.canal.tas.shared.modulos.apis.inversiones.servicios;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.Cotizaciones;
import ar.com.hipotecario.canal.tas.ContextoTAS;

public class TASRestInversiones {

    public static Objeto getCotizacionUVA(ContextoTAS contexto, String idMoneda, Fecha fechaDesde, Fecha fechaHasta){
        ApiRequest request = new ApiRequest("CotizacionesMoneda", "inversiones", "GET", "/v1/cotizacionesmoneda", contexto);
        request.query("moneda", idMoneda);
        request.query("fechadesde", fechaDesde.string("yyyy-MM-dd"));
        request.query("fechahasta", fechaHasta.string("yyyy-MM-dd"));
        //request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response;
    }

    public static Objeto getCotizacionByMercado(ContextoTAS contexto, String idCliente, String idMercado){
        ApiRequest request = new ApiRequest("Cotizaciones", "inversiones", "GET", "/v1/cotizaciones/{idCliente}", contexto);
        request.path("idCliente", idCliente);
        request.query("mercado", idMercado);
        //request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response;
    }
}
