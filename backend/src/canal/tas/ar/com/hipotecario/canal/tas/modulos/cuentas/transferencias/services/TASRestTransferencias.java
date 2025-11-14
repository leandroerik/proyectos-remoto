package ar.com.hipotecario.canal.tas.modulos.cuentas.transferencias.services;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.modulos.cuentas.transferencias.modelos.TASTransferenciasPropia;

public class TASRestTransferencias {



 
  public static Objeto postEjecutarTransferencia(ContextoTAS contexto, TASTransferenciasPropia datosTf){
    ApiRequest request = new ApiRequest("TransferenciaCuentaPropia", "cuentas", "POST",
					"/v2/cuentas/{idcuenta}/transferencias", contexto);

			request.path("idcuenta", datosTf.getIdCuenta());
			request.query("cuentapropia", datosTf.isCuentaPropia());
			request.query("inmediata", datosTf.isInmediata());
			request.query("especial", datosTf.isEspecial());
      request.body("cuentaOrigen", datosTf.getCuentaOrigen());
      request.body("cuentaDestino", datosTf.getCuentaDestino());
      request.body("idCliente", datosTf.getIdCliente());
      request.body("idMoneda", datosTf.getIdMoneda());
      request.body("idMonedaOrigen", datosTf.getIdMonedaOrigen());
			request.body("idMonedaDestino", datosTf.getIdMonedaDestino());
      request.body("importe", datosTf.getImporte());
      request.body("modoSimulacion", datosTf.getModoSimulacion());
      request.body("reverso", datosTf.getReverso());
      request.body("tipoCuentaOrigen", datosTf.getTipoCuentaOrigen());
			request.body("tipoCuentaDestino", datosTf.getTipoCuentaDestino());      
      request.body("transaccion", datosTf.getTransaccion());
			request.body("ddjjCompraventa", datosTf.getDdjjCompraventa());
      request.body("cotizacion", datosTf.getCotizacion());
      request.body("importePesos", datosTf.getImportePesos());
      request.body("importeDivisa", datosTf.getImporteDivisa());
      request.body("efectivo", datosTf.getEfectivo());
      request.cache = false;
			LogTAS.loguearRequest(contexto, request, "REQUEST_TF_CUENTA_PROPIA");
			ApiResponse response = request.ejecutar();
      LogTAS.loguearResponse(contexto, response, "RESPONSE_TF_CUENTA_PROPIA");
      ApiException.throwIf(!response.http(200, 202, 204), request, response);
      return response;
  }

    public static Objeto postSimularTransferenciaCV(ContextoTAS contexto, TASTransferenciasPropia datosTf){
        ApiRequest request = new ApiRequest("TransferenciaCuentaPropia", "cuentas", "POST",
                "/v2/cuentas/{idcuenta}/transferencias", contexto);

        request.path("idcuenta", datosTf.getIdCuenta());
        request.query("cuentapropia", datosTf.isCuentaPropia());
        request.query("inmediata", datosTf.isInmediata());
        request.query("especial", datosTf.isEspecial());
        request.body("cotizacion", "0");
        request.body("importe", datosTf.getImporte());
        request.body("importePesos", "0");
        request.body("importeDivisa", "0");
        request.body("modoSimulacion", "true");
        request.body("reverso", false);
        request.body("cuentaOrigen", datosTf.getCuentaOrigen());
        request.body("cuentaDestino", datosTf.getCuentaDestino());
        request.body("tipoCuentaOrigen", datosTf.getTipoCuentaOrigen());
        request.body("tipoCuentaDestino", datosTf.getTipoCuentaDestino());
        request.body("idMoneda", datosTf.getIdMoneda());
        request.body("idMonedaDestino", datosTf.getIdMonedaDestino());
        request.body("idCliente", datosTf.getIdCliente());
        request.cache = false;
        LogTAS.loguearRequest(contexto, request, "REQUEST_SIMULACION_CV_MONEDA_EXT");
        ApiResponse response = request.ejecutar();
        LogTAS.loguearResponse(contexto, response, "RESPONSE_SIMULACION_CV_MONEDA_EXT");
        ApiException.throwIf(!response.http(200, 202, 204), request, response);
        return response;
    }
    public static Objeto postEjecutarTransferenciaCV(ContextoTAS contexto, TASTransferenciasPropia datosTf){
        ApiRequest request = new ApiRequest("TransferenciaCuentaPropia", "cuentas", "POST",
                "/v2/cuentas/{idcuenta}/transferencias", contexto);

        request.path("idcuenta", datosTf.getIdCuenta());
        request.query("cuentapropia", datosTf.isCuentaPropia());
        request.query("inmediata", datosTf.isInmediata());
        request.query("especial", datosTf.isEspecial());
        request.body("cotizacion", datosTf.getCotizacion());//sale de la simulacion
        request.body("importe", datosTf.getImporte());
        request.body("importeDivisa", datosTf.getImporteDivisa());//sale de la simulacion
        request.body("importePesos", datosTf.getImportePesos());//sale de la simulacion
        request.body("modoSimulacion", false);
        request.body("reverso", false);
        request.body("cuentaOrigen", datosTf.getCuentaOrigen());
        request.body("cuentaDestino", datosTf.getCuentaDestino());
        request.body("tipoCuentaOrigen", datosTf.getTipoCuentaOrigen());
        request.body("tipoCuentaDestino", datosTf.getTipoCuentaDestino());
        request.body("idMoneda", datosTf.getIdMoneda().toString());
        request.body("idMonedaDestino", datosTf.getIdMonedaDestino().toString());
        request.body("transaccion", datosTf.getTransaccion());//sale de la simulacion
        request.body("idCliente", datosTf.getIdCliente());
        request.body("efectivo", false);
        request.body("identificacionPersona", datosTf.getIdentificacionPersona());
        request.body("paisDocumento", datosTf.getPaisDocumento());
        request.body("montoEnDivisa", datosTf.getMontoEnDivisa());//sale de simulacion
        request.body("ddjjCompraventa", datosTf.getDdjjCompraventa());

        request.cache = false;
        LogTAS.loguearRequest(contexto, request, "REQUEST_EJECUCION_CV_MONEDA_EXT");
        ApiResponse response = request.ejecutar();
        LogTAS.loguearResponse(contexto, response, "RESPONSE_EJECUCION_CV_MONEDA_EXT");
        ApiException.throwIf(!response.http(200, 202, 204), request, response);
        return response;
    }
}
