package ar.com.hipotecario.canal.tas.modulos.tarjetas.services;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;

public class TASRestTarjetas {

    public static Objeto getTarjetaCredito(ContextoTAS contexto, String idTarjeta){
        ApiRequest request = new ApiRequest("ConsultaTarjetasPorId", "tarjetascredito", "GET","/v1/tarjetascredito/{idtarjeta}", contexto);
        request.path("idtarjeta", idTarjeta);
        request.cache = true;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 202, 204), request, response);
        Objeto tarjeta = !response.isEmpty() ? response.objetos().get(0) : null;
        return tarjeta;
    }

    public static Objeto getResumenCuentaTC(ContextoTAS contexto, String idCuenta, String idTarjeta){
        ApiRequest request = new ApiRequest("ResumenCuentaTarjeta", "tarjetascredito", "GET","/v1/cuentas/{idcuenta}/resumencuenta", contexto);
        request.path("idcuenta", idCuenta);
        request.query("idtarjeta", idTarjeta);
        request.cache = true;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 202, 204), request, response);
        return response;
    }

    public static Objeto patchCambioFormaPago(ContextoTAS contexto, Objeto datosParaCambio){
        try {
            ApiRequest request = new ApiRequest("CambioFormaPagoTarjeta", "tarjetascredito", "PATCH", "/v1/cuentas/{idcuenta}/formapago", contexto);
            request.path("idcuenta", datosParaCambio.string("cuenta"));
            request.body("formaPago", datosParaCambio.string("formaPago"));
            request.body("sucursal", datosParaCambio.string("sucursal"));
            request.body("cuenta", datosParaCambio.string("cuenta"));
            request.body("numeroCuenta", datosParaCambio.string("numeroCuenta"));
            request.body("tipoCuenta", datosParaCambio.string("tipoCuenta"));
            request.body("idMoneda", datosParaCambio.string("idMoneda"));
            request.body("ticket", datosParaCambio.string("ticket"));
            request.body("codigoCliente", datosParaCambio.string("codigoCliente"));
            request.body("nombreCliente", datosParaCambio.string("nombreCliente"));
            request.body("apellidoCliente", datosParaCambio.string("apellidoCliente"));
            request.cache = true;
            LogTAS.loguearRequest(contexto, request, "REQUEST_CAMBIO_FORMA_PAGO");
            ApiResponse response = request.ejecutar();
            LogTAS.loguearResponse(contexto, response, "RESPONSE_CAMBIO_FORMA_PAGO");
            ApiException.throwIf(!response.http(200, 202), request, response);
            Objeto resp = new Objeto();
            resp.set("estado", "OK");
            resp.set("response", response);
            return resp;
        }catch (ApiException e){
            int codigo = e.response.codigoHttp;
            Objeto errorApi = new Objeto();
            errorApi.set("estado", "ERROR");
            errorApi.set("codigoHTTP", codigo);
            errorApi.set("codigoMW", e.response.string("codigo"));
            errorApi.set("error", e);
            return errorApi;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    public static Objeto getUltimaLiquidacion(ContextoTAS contexto, String idCuenta){
        try {
            ApiRequest request = new ApiRequest("UltimaLiquidacionPrismaMP", "servicios", "GET", "/api/tarjeta/ultimaLiquidacionv2", contexto);
            request.header("x-usuariocanal", contexto.config.string("tas_api_usuario"));
            request.query("cuenta", idCuenta);
            request.cache = true;
            ApiResponse response = request.ejecutar();
            ApiException.throwIf(!response.http(200, 202, 204), request, response);
            Objeto resp = new Objeto();
            resp.set("estado", "OK");
            resp.set("response", response);
            return resp;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    public static  Objeto postPagoTarjetaDebitoCuenta(ContextoTAS contexto, Objeto datosParaPgo){
        ApiRequest request = new ApiRequest("PagoTarjeta", "servicios", "POST", "/api/tarjeta/pagoTarjeta", contexto);
        request.body("cuenta", datosParaPgo.string("cuenta"));
        request.body("cuentaTarjeta",datosParaPgo.string("cuentaTarjeta"));
        request.body("moneda",datosParaPgo.string("moneda"));
        request.body("importe",datosParaPgo.bigDecimal("importe"));
        request.body("tipoCuenta",datosParaPgo.string("tipoCuenta"));
        request.body("tipoTarjeta",datosParaPgo.string("tipoTarjeta"));
        request.body("numeroTarjeta",datosParaPgo.string("numeroTarjeta"));
        //request.cache = true;
        LogTAS.loguearRequest(contexto, request, "REQUEST_PAGO_TARJETA_DEBITO_CUENTA");
        ApiResponse response = request.ejecutar();
        LogTAS.loguearResponse(contexto, response, "RESPONSE_PAGO_TARJETA_DEBITO_CUENTA");

        ApiException.throwIf(!response.http(200, 202, 204), request, response);
        return response;
    }

    public static Objeto getEstadosTD(ContextoTAS contexto, String numeroTD){
        try {
            ApiRequest request = new ApiRequest("ConsultaEstadoTarjetaDebitoLink", "tarjetasdebito", "GET", "/v1/tarjetasdebito/{numeroTarjeta}/estado", contexto);
            request.path("numeroTarjeta", numeroTD);
            request.cache = true;
            ApiResponse response = request.ejecutar();
            ApiException.throwIf(!response.http(200, 202, 204), request, response);
            Objeto resp = new Objeto();
            resp.set("estado", "OK");
            resp.set("estadoTD", response);
            return resp;
        }catch (Exception e){
            LogTAS.error(contexto, e);
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    public static Objeto deleteBlanquearPIL(ContextoTAS contexto, String numeroTD, String pil, String digitoVerificador, String numeroMiembro, String numeroVersion){
        try {
            ApiRequest request = new ApiRequest("BlanqueoPilLink", "tarjetasdebito", "DELETE",
                    "/v1/tarjetasdebito/{nrotarjeta}/{pil}", contexto);
            request.path("nrotarjeta", numeroTD);
            request.path("pil", pil);
            request.query("digitoverificador", digitoVerificador);
            request.query("numeromiembro", numeroMiembro);
            request.query("numeroversion", numeroVersion);
            request.cache = true;

            LogTAS.loguearRequest(contexto, request, "REQUEST_BLANQUEO_PIL");
            ApiResponse response = request.ejecutar();
            LogTAS.loguearResponse(contexto, response, "RESPONSE_BLANQUEO_PIL");
            ApiException.throwIf(!response.http(200, 204), request, response);
            Objeto resp = new Objeto();
            resp.set("estado", "OK");
            resp.set("codigo",response.codigoHttp);
            resp.set("respuesta", response);
            return resp;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }
    public static Objeto deleteBlanquearPIN(ContextoTAS contexto, String numeroTD, String pin, String digitoVerificador, String numeroMiembro, String numeroVersion){
        try {
            ApiRequest request = new ApiRequest("BlanqueoPinLink", "tarjetasdebito", "DELETE",
                    "/v1/tarjetasdebito/{nrotarjeta}/{pin}", contexto);
            request.path("nrotarjeta", numeroTD);
            request.path("pin", pin);
            request.query("digitoverificador", digitoVerificador);
            request.query("numeromiembro", numeroMiembro);
            request.query("numeroversion", numeroVersion);
            request.cache = true;

            LogTAS.loguearRequest(contexto, request, "REQUEST_BLANQUEO_PIN");
            ApiResponse response = request.ejecutar();
            LogTAS.loguearResponse(contexto, response, "RESPONSE_BLANQUEO_PIN");
            ApiException.throwIf(!response.http(200, 204), request, response);
            Objeto resp = new Objeto();
            resp.set("estado", "OK");
            resp.set("codigo",response.codigoHttp);
            resp.set("respuesta", response);
            return resp;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    public static Objeto patchHabilitarTD(ContextoTAS contexto, String numeroTD){
        try{
            ApiRequest request = new ApiRequest("HabilitarTarjetaDebitoLink", "tarjetasdebito", "PATCH", "/v1/tarjetasdebito/{nrotarjeta}/habilitar", contexto);
            request.path("nrotarjeta", numeroTD);
            request.cache = true;

            LogTAS.loguearRequest(contexto, request, "REQUEST_HABILITAR_TD");
            ApiResponse response = request.ejecutar();
            LogTAS.loguearResponse(contexto, response, "RESPONSE_HABILITAR_TD");
            ApiException.throwIf(!response.http(200, 204), request, response);
            Objeto resp = new Objeto();
            resp.set("estado", "OK");
            resp.set("codigo",response.codigoHttp);
            resp.set("respuesta", response);
            return resp;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    public static Objeto getDetalleTDById(ContextoTAS contexto, String numeroTD){
        try {
            ApiRequest request = new ApiRequest("ConsultaTarjetaDebitoDetalle", "tarjetasdebito", "GET", "/v1/tarjetasdebito/{nrotarjeta}", contexto);
            request.path("nrotarjeta", numeroTD);
            request.cache = true;
            ApiResponse response = request.ejecutar();
            ApiException.throwIf(!response.http(200), request, response);
            Objeto resp = new Objeto();
            resp.set("estado", "OK");
            resp.set("respuesta", response);
            return resp;
        }catch (Exception e){
            Objeto error = new Objeto();
            LogTAS.error(contexto, e);
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

	public static Objeto consultaTitularidadCuentaPrincipal(ContextoTAS contexto, String num) {
        ApiRequest request = new ApiRequest("ConsultaTitularidadCuentaPrincipalTarjetaCredito", "tarjetascredito", "GET", "/v1/cuentas/{nrotarjeta}/titular", contexto);
        request.path("nrotarjeta", num);
        request.query("tipotarjeta", "N");
        request.cache = true;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response;
	}
	
	 public static Objeto getTarjetasCredito(ContextoTAS contexto, String idCliente){
	        ApiRequest request = new ApiRequest("ConsultaTarjetasCreditoCliente", "tarjetascredito", "GET","/v1/tarjetascredito", contexto);
	        request.query("adicionales", "false");
	        request.query("idcliente", idCliente);
	        request.cache = true;
	        ApiResponse response = request.ejecutar();
	        ApiException.throwIf(!response.http(200, 202, 204), request, response);
	        Objeto tarjeta = !response.isEmpty() ? response : null;
	        return tarjeta;
	    }
}
