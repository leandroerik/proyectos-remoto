package ar.com.hipotecario.canal.tas.modulos.prestamos.services;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;

public class TASRestPrestamos {

    public static Objeto getPrestamoByNro(ContextoTAS contexto, String nroPrestamo, boolean detalle){
        ApiRequest request = new ApiRequest("ConsultaPrestamo", "prestamos", "GET", "/v1/prestamos/{nroPrestamo}", contexto);
        request.path("nroPrestamo", nroPrestamo);
        request.query("detalle", detalle);
        request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response;
    }

    public static Objeto getCuotasPrestamo(ContextoTAS contexto, String nroPrestamo, String fechaDesde, String fechaHasta){
        try{
        ApiRequest request = new ApiRequest("ConsultaCuotasPrestamo", "prestamos", "GET", "/v1/prestamos/{nroPrestamo}/cuotas", contexto);
        request.path("nroPrestamo", nroPrestamo);
        request.query("fechadesde", fechaDesde);
        request.query("fechahasta", fechaHasta);
        request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        Objeto resp = new Objeto();
        resp.set("estado", "OK");
        resp.set("cuota", response);
        return resp;
        }catch (Exception e){
            return new Objeto().set("estado", "ERROR");
        }
    }

    public static Objeto getResumenPrestamo(ContextoTAS contexto, String nroPrestamo, boolean leyenda){
        ApiRequest request = new ApiRequest("ConsultaResumenPrestamo", "prestamos", "GET", "/v1/prestamos/{nroPrestamo}/resumen", contexto);
        request.path("nroPrestamo", nroPrestamo);
        request.query("leyenda", leyenda);
        request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response;
    }

    public static Objeto postPagoPrestamoDebitoCuenta(ContextoTAS contexto, Objeto datosPago){
        ApiRequest request = new ApiRequest("PagoPrestamo", "servicios", "POST", "/v1/prestamos/{nroPrestamo}/electronicos", contexto);
        request.path("nroPrestamo", datosPago.string("numeroPrestamo"));
        request.body("numeroPrestamo", datosPago.string("numeroPrestamo"));
        request.body("tipoPrestamo",datosPago.string("tipoPrestamo"));
        request.body("importe",datosPago.string("importe"));
        request.body("cuenta",datosPago.bigDecimal("cuenta"));
        request.body("idMoneda",datosPago.string("moneda"));
        request.body("tipoCuenta",datosPago.string("tipoCuenta"));
        request.body("reverso",datosPago.string("reverso"));
        request.body("idProducto", datosPago.string("idProducto"));
        //request.cache = true;
        LogTAS.loguearRequest(contexto, request, "REQUEST_PAGO_PRESTAMO_DEBITO_CUENTA");
        ApiResponse response = request.ejecutar();
        LogTAS.loguearResponse(contexto, response, "RESPONSE_PAGO_PRESTAMO_DEBITO_CUENTA");
        ApiException.throwIf(!response.http(200, 202, 204), request, response);
        return response;
    }
}
