package ar.com.hipotecario.canal.tas.shared.modulos.depositos.service;

import java.util.Date;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.models.TasDepositoEfectivo;
import ar.com.hipotecario.canal.tas.modulos.prestamos.pagos.models.TasPagoPrestamoEfectivo;
import ar.com.hipotecario.canal.tas.modulos.tarjetas.pagos.models.TasPagoTarjetaEfectivo;

public class TasRestDepositos {
    public static Objeto postDepositosEfectivo(ContextoTAS contexto, TasDepositoEfectivo deposito) {
        ApiRequest request = new ApiRequest("DepositoEfectivo", "tas", "POST", "/v1/depositosefectivo", contexto);

        String fechaApi = deposito.getFecha().string(
                "yyyy-MM-dd'T'HH:mm:ss");

        request.body("cuenta", deposito.getCuenta());
        request.body("fecha", fechaApi); // deposito.getFecha());
        request.body("lote", deposito.getLote());
        request.body("moneda", deposito.getMoneda());
        request.body("monto", deposito.getMonto());
        request.body("oficina", deposito.getOficina());
        request.body("precinto", deposito.getPrecinto());
        request.body("producto", deposito.getProducto());
        request.body("tas", deposito.getTas());

        request.cache = true;
        ApiResponse response = null;
        LogTAS.loguearRequest(contexto, request, "REQUEST_DEPOSITO_EFECTIVO");
        response = request.ejecutar();
        LogTAS.loguearResponse(contexto, response, "RESPONSE_DEPOSITO_EFECTIVO");
        ApiException.throwIf(!response.http(200, 202), request, response);

        return response;
    }

    public static Objeto patchDepositosEfectivoReversa(ContextoTAS contexto, TasDepositoEfectivo deposito,
            String idReversa) {

        ApiRequest request = new ApiRequest("DepositoEfectivoReversa", "tas", "PATCH",
                "/v1/depositosefectivo/{idReversa}",
                contexto);
        
        request.path("idReversa", idReversa);
        
        String fechaApi = deposito.getFecha().string(
                "yyyy-MM-dd'T'HH:mm:ss");

        request.body("cuenta", deposito.getCuenta());
        request.body("fecha", fechaApi);
        request.body("lote", deposito.getLote());
        request.body("moneda", deposito.getMoneda());
        request.body("monto", deposito.getMonto());
        request.body("oficina", deposito.getOficina());
        request.body("precinto", deposito.getPrecinto());
        request.body("producto", deposito.getProducto());
        request.body("tas", deposito.getTas());

        request.cache = true;
        ApiResponse response = null;
        LogTAS.loguearRequest(contexto, request, "REQUEST_DEPOSITO_EFECTIVO_REVERSA");
        response = request.ejecutar();
        LogTAS.loguearResponse(contexto, response, "RESPONSE_DEPOSITO_EFECTIVO_REVERSA");
        ApiException.throwIf(!response.http(200, 202), request, response);

        return response;
    }
    
    public static Objeto postPagoTarjetaEfectivo(ContextoTAS contexto, TasPagoTarjetaEfectivo deposito) {
        ApiRequest request = new ApiRequest("PagoTarjetaEfectivo", "tas", "POST", "/v1/tarjetas/{cuentaTarjeta}/pagos", contexto);

        
        request.path("cuentaTarjeta", deposito.getCuentaTarjeta());

        String fechaApi = deposito.getFecha().string(
                "yyyy-MM-dd'T'HH:mm:ss");

        request.body("codigoPrecinto", deposito.getCodigoPrecinto());
        request.body("cuentaTarjeta", deposito.getCuentaTarjeta());
        request.body("fecha", fechaApi); // deposito.getFecha());
        request.body("idMoneda", deposito.getIdMoneda());
        request.body("importe", deposito.getImporte());
        request.body("lote", deposito.getLote());
        request.body("oficina", deposito.getOficina());
        request.body("tas", deposito.getTas());
        request.body("tipoProducto", deposito.getTipoProducto());
        request.body("tipoTarjeta", deposito.getTipoTarjeta());



        request.cache = true;
        ApiResponse response = null;
        LogTAS.loguearRequest(contexto, request, "REQUEST_PAGO_TARJETA_EFECTIVO");
        response = request.ejecutar();
        LogTAS.loguearResponse(contexto, response, "RESPONSE_PAGO_TARJETA_EFECTIVO");
        ApiException.throwIf(!response.http(200, 202), request, response);

        return response;
    }

    public static Objeto patchPagoTarjetaEfectivoReversa(ContextoTAS contexto, TasPagoTarjetaEfectivo deposito,
            String idReversa) {

    	 ApiRequest request = new ApiRequest("PagoTarjetaEfectivo", "tas", "PATCH", "/v1/tarjetas/{cuentaTarjeta}/pagos/{idReversa}", contexto);

         
         request.path("cuentaTarjeta", deposito.getCuentaTarjeta());
         request.path("idReversa", idReversa);


         String fechaApi = deposito.getFecha().string(
                 "yyyy-MM-dd'T'HH:mm:ss");

         request.body("codigoPrecinto", deposito.getCodigoPrecinto());
         request.body("cuentaTarjeta", deposito.getCuentaTarjeta());
         request.body("fecha", fechaApi); // deposito.getFecha());
         request.body("idMoneda", deposito.getIdMoneda());
         request.body("importe", deposito.getImporte());
         request.body("lote", deposito.getLote());
         request.body("oficina", deposito.getOficina());
         request.body("tas", deposito.getTas());
         request.body("tipoProducto", deposito.getTipoProducto());
         request.body("tipoTarjeta", deposito.getTipoTarjeta());

        request.cache = true;
        ApiResponse response = null;
        LogTAS.loguearRequest(contexto, request, "REQUEST_PAGO_TARJETA_EFECTIVO_REVERSA");
        response = request.ejecutar();
        LogTAS.loguearResponse(contexto, response, "RESPONSE_PAGO_TARJETA_EFECTIVO_REVERSA");
        ApiException.throwIf(!response.http(200, 202), request, response);

        return response;
    }
    
    public static Objeto postPagoPrestamoEfectivo(ContextoTAS contexto, TasPagoPrestamoEfectivo deposito) {
        ApiRequest request = new ApiRequest("PagoPrestamoEfectivo", "tas", "POST", "/v1/prestamos/{nroPrestamo}/pagos", contexto);

        
        request.path("nroPrestamo", deposito.getNroPrestamo());

        String fechaApi = deposito.getFecha().string(
                "yyyy-MM-dd'T'HH:mm:ss");

        request.body("precinto", deposito.getPrecinto());
        request.body("fecha", fechaApi); // deposito.getFecha());
        request.body("hora", deposito.getHora());
        request.body("importe", deposito.getImporte());
        request.body("nroPrestamo", deposito.getNroPrestamo());
        request.body("lote", deposito.getLote());
        request.body("sucursal", deposito.getSucursal());
        request.body("tas", deposito.getTas());
        


        request.cache = true;
        ApiResponse response = null;
        LogTAS.loguearRequest(contexto, request, "REQUEST_PAGO_PRESTAMO_EFECTIVO");
        response = request.ejecutar();
        LogTAS.loguearResponse(contexto, response, "RESPONSE_PAGO_PRESTAMO_EFECTIVO");
        ApiException.throwIf(!response.http(200, 202), request, response);

        return response;
    }
    
    public static Objeto patchPagoPrestamoEfectivoReversa(ContextoTAS contexto, TasPagoPrestamoEfectivo deposito,
            String idReversa) {

    	 ApiRequest request = new ApiRequest("PagoPrestamoEfectivo", "tas", "PATCH", "/v1/prestamos/{nroPrestamo}/pagos/{idReversa}", contexto);

         
         request.path("nroPrestamo", deposito.getNroPrestamo());
         request.path("idReversa", idReversa);


         String fechaApi = deposito.getFecha().string(
                 "yyyy-MM-dd'T'HH:mm:ss");

         request.body("precinto", deposito.getPrecinto());
         request.body("fecha", fechaApi); // deposito.getFecha());
         request.body("hora", deposito.getHora());
         request.body("importe", deposito.getImporte());
         request.body("nroPrestamo", deposito.getNroPrestamo());
         request.body("lote", deposito.getLote());
         request.body("sucursal", deposito.getSucursal());
         request.body("tas", deposito.getTas());
         

        request.cache = true;
        ApiResponse response = null;
        LogTAS.loguearRequest(contexto, request, "REQUEST_PAGO_PRESTAMO_EFECTIVO_REVERSA");
        response = request.ejecutar();
        LogTAS.loguearResponse(contexto, response, "RESPONSE_PAGO_PRESTAMO_EFECTIVO_REVERSA");
        ApiException.throwIf(!response.http(200, 202), request, response);

        return response;
    }

}
