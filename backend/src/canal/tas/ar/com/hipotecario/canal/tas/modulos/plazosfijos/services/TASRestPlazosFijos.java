package ar.com.hipotecario.canal.tas.modulos.plazosfijos.services;


import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;

public class TASRestPlazosFijos {

    public static Objeto getTasasByIdCliente(ContextoTAS contexto, String idCliente, String secuencial){
        try {
            ApiRequest request = new ApiRequest("ConsultaTasaPlazoFijo", "plazosfijos", "GET", "/v1/tasas", contexto);
            request.query("idcliente", idCliente);
            request.query("secuencial", secuencial);
            ApiResponse response = request.ejecutar();
            ApiException.throwIf(!response.http(200, 204), request, response);
            Objeto resp = new Objeto();
            resp.set("estado", "OK");
            resp.set("codigo", response.codigoHttp);
            resp.set("respuesta", response);
            return resp;
        }catch (Exception e){
            LogTAS.error(contexto, e);
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }
    
    public static Objeto getTasasPreferencialesByIdCliente(ContextoTAS contexto, String idCliente){
        try {
            ApiRequest request = new ApiRequest("ConsultaTasaPreferencial", "plazosfijos", "GET", "/v1/tasasPreferenciales", contexto);
            request.query("idcliente", idCliente);
            request.cache = true;
            ApiResponse response = request.ejecutar();
            ApiException.throwIf(!response.http(200, 204), request, response);
            return response;
        }catch (Exception e){
            LogTAS.error(contexto, e);
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    public static Objeto postSimularPF(ContextoTAS contexto, Objeto parametrosSimulador){
        ApiRequest request = new ApiRequest("ConsultaAltaPlazoFijo", "simuladores", "GET", "/v1/plazoFijos", contexto);
        request.query("tipoOperacion", parametrosSimulador.string("tipoPlazoFijoCodigo"));
        request.query("periodo", parametrosSimulador.integer("periodo"));
        request.query("plazo", parametrosSimulador.integer("plazo"));
        request.query("idcliente", parametrosSimulador.string("idCliente"));
        request.query("monto", parametrosSimulador.string("monto"));
        request.query("renova", parametrosSimulador.string("renova"));
        request.query("cuenta", parametrosSimulador.string("cuenta"));
        request.query("moneda", parametrosSimulador.string("moneda"));
        request.query("tipoCuenta", parametrosSimulador.string("tipoCuenta"));
        request.query("canal", parametrosSimulador.string("canal"));
        //request.cache = true;
        LogTAS.loguearRequest(contexto, request, "REQUEST_SIMULADOR_PF");
        ApiResponse response = request.ejecutar();
        LogTAS.loguearResponse(contexto, response, "RESPONSE_SIMULADOR_PF");
        ApiException.throwIf(!response.http(200), request, response);        
        return response;
    }

    public static Objeto postAltaPF(ContextoTAS contexto, Objeto parametrosAlta){
        try {
            ApiRequest request = new ApiRequest("AltaPlazoFijo", "plazosfijos", "POST", "/v1/plazoFijos", contexto);
            request.body("canal", parametrosAlta.string("canal"));
            request.body("capInteres", parametrosAlta.string("acredita"));
            request.body("cuenta", parametrosAlta.string("cuenta"));
            request.body("idPlanAhorro", null);
            request.body("idcliente", parametrosAlta.string("idCliente"));
            request.body("moneda", parametrosAlta.string("moneda"));
            request.body("monto", parametrosAlta.string("monto"));
            request.body("nroOperacion", null);
            request.body("periodo", parametrosAlta.string("periodo"));
            request.body("plazo", parametrosAlta.string("plazo"));
            request.body("renova", parametrosAlta.string("renova"));
            request.body("reverso", null);
            request.body("tipoCuenta", parametrosAlta.string("tipoCuenta"));
            request.body("tipoOperacion", parametrosAlta.string("tipoPlazoFijoCodigo"));
            request.body("usuarioAlta", parametrosAlta.string("idCliente"));
            // request.cache = true;
            LogTAS.loguearRequest(contexto, request, "REQUEST_ALTA_PF");
            ApiResponse response = request.ejecutar();
            LogTAS.loguearResponse(contexto, response, "RESPONSE_ALTA_PF");
            ApiException.throwIf(!response.http(200), request, response);            
            return response;
        } catch (Exception e){
         Objeto error = new Objeto();
         error.set("estado", "ERROR");
         error.set("error" , e);
         return error;
        }
    }

    public static Objeto getDetallePF(ContextoTAS contexto, String codigoPF){
        try {
            ApiRequest request = new ApiRequest("ConsultaPlazosFijos", "plazosfijos", "GET", "/v1/plazosfijos/{idPlazoFijo}", contexto);
            request.path("idPlazoFijo", codigoPF);
            request.cache = true;
            ApiResponse response = request.ejecutar();
            ApiException.throwIf(!response.http(200, 204), request, response);
            if (response.codigoHttp == 204) return new Objeto().set("estado", "SIN_RESULTADOS");
            Objeto resp = new Objeto();
            resp.set("estado", "OK");
            resp.set("respuesta", response);
            return resp;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error" , e);
            return error;
        }
    }

    public static Objeto getCancelacionAnticipada(ContextoTAS contexto, String idPF){
        ApiRequest request = new ApiRequest("CancelacionAnticipadaInfoSolicitud", "plazosfijos",
                "GET", "/v1/plazosfijos/{idPlazoFijo}/cancelacionanticipada/solicitud", contexto);
        request.path("idPlazoFijo", idPF);
        request.cache = true;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response;
    }

    public static Objeto getEstadoCancelacionAnticipada(ContextoTAS contexto, String idPF){
        ApiRequest request = new ApiRequest("CancelacionAnticipadaInfoSolicitudEstado", "plazosfijos",
                "GET", "/v1/plazosfijos/{idPlazoFijo}/cancelacionanticipada/solicitud/estado", contexto);
        request.path("idPlazoFijo", idPF);
        request.cache = true;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response;
    }

    public static Objeto postSolicitarCancelacionAnt(ContextoTAS contexto, String idPF){
        ApiRequest request = new ApiRequest("CancelacionAnticipadaSolicitud", "plazosfijos",
                "POST", "/v1/plazosfijos/cancelacionanticipada/solicitud", contexto);
        request.body("numeroOperacion", idPF);
        //request.cache = true;
        LogTAS.loguearRequest(contexto, request, "REQUEST_SOLICITAR_CANCELACION_ANT");
        ApiResponse response = request.ejecutar();
        LogTAS.loguearResponse(contexto, response, "RESPONSE_SOLICITAR_CANCELACION_ANT");
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response;
    } 
    
    public static Objeto getPlazoFijoDetalle(ContextoTAS contexto, String idCobis, Fecha fechaDesde, Fecha fechaHasta, String num){
        ApiRequest request = new ApiRequest("ConsultaPlazoFijoDetalle", "plazosfijos",
                "GET", "/v1/{idCobis}", contexto);
        request.path("idCobis", idCobis);
        request.query("fechaInicio", fechaDesde.string("dd/MM/yyyy"));
        request.query("fechaFin", fechaHasta.string("dd/MM/yyyy"));
        request.query("certificado", num);

        request.cache = true;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response;
    }

    public static Objeto getCabeceraPFLogros(ContextoTAS contexto, String idCliente, int idPlanAhorro, String opcion, String operacion){
        ApiRequest request = new ApiRequest("ConsultaPlanAhorro", "plazosfijos", "GET", "/v1/planAhorro/cabecera", contexto);
        request.query("opcion", opcion);
        request.query("idPlanAhorro", idPlanAhorro);
        request.query("operacion", operacion);
        request.query("codCliente", idCliente);        
        request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        
        return response;
    }

    public static Objeto postSimularPFLogros(ContextoTAS contexto, Objeto params){
        ApiRequest request = new ApiRequest("ConsultaPlazoFijoMeta", "simuladores", "GET", "/v1/plazoFijos/meta", contexto);
        request.query("codCliente", params.string("codCliente"));
        request.query("cuenta", params.string("cuenta"));
        request.query("cuota", params.string("cuota"));
        request.query("dia", params.string("dia"));        
        request.query("moneda", params.string("moneda"));
        request.query("monto", params.string("monto"));
        request.query("nombre", params.string("nombre"));
        request.query("opcion", params.string("opcion"));    
        request.query("planContratado", params.string("planContratado"));
        request.query("tipoCuenta", params.string("tipoCuenta"));  
        //request.cache = true;
        LogTAS.loguearRequest(contexto, request, "REQUEST_SIMULADOR_PF_META");
        ApiResponse response = request.ejecutar();
        
        LogTAS.loguearResponse(contexto, response, "RESPONSE_SIMULADOR_PF_META");
        ApiException.throwIf(!response.http(200, 204), request, response);        
        return response;
    }

    //? este metodo deberia ser postSimularPF hay que hacerlo generico
    public static Objeto postSimuladorPFLogros(ContextoTAS contexto,Objeto parametrosSimuladorPF){
        ApiRequest request = new ApiRequest("ConsultaAltaPlazoFijo", "simuladores", "GET", "/v1/plazoFijos", contexto);
        request.query("tipoOperacion", parametrosSimuladorPF.string("tipoOperacion"));        
        request.query("plazo", parametrosSimuladorPF.integer("plazo"));
        request.query("idcliente", parametrosSimuladorPF.string("idCliente"));
        request.query("monto", parametrosSimuladorPF.string("monto"));        
        request.query("cuenta", parametrosSimuladorPF.string("cuenta"));
        request.query("moneda", parametrosSimuladorPF.string("moneda"));
        request.query("tipoCuenta", parametrosSimuladorPF.string("tipoCuenta"));
        request.query("canal", parametrosSimuladorPF.string("canal"));
        //request.cache = true;
        LogTAS.loguearRequest(contexto, request, "REQUEST_SIMULADOR_PF_LOGROS");
        ApiResponse response = request.ejecutar();
        LogTAS.loguearResponse(contexto, response, "RESPONSE_SIMULADOR_PF_LOGROS");
        ApiException.throwIf(!response.http(200), request, response);
        return response;
    }

    public static Objeto postAltaPFLogros(ContextoTAS contexto,Objeto parametrosAltaPfLogros){
        try{
            ApiRequest request = new ApiRequest("AltaPlazoFijoMETA", "plazosfijos", "POST", "/v1/plazoFijosMETA", contexto);
            request.body("codCliente", parametrosAltaPfLogros.string("codCliente"));
            request.body("cuenta", parametrosAltaPfLogros.string("cuenta"));
            request.body("cuota", parametrosAltaPfLogros.string("cuota"));
            request.body("dia", Integer.valueOf(parametrosAltaPfLogros.string("dia")));
            request.body("moneda", Integer.valueOf(parametrosAltaPfLogros.string("moneda")));
            request.body("monto", parametrosAltaPfLogros.string("monto"));
            request.body("nombre", parametrosAltaPfLogros.string("nombre"));
            request.body("opcion", parametrosAltaPfLogros.string("opcion"));
            request.body("operacion", parametrosAltaPfLogros.string("operacion"));
            request.body("planContratado", Integer.valueOf(parametrosAltaPfLogros.string("planContratado")));
            request.body("tipoCuenta", parametrosAltaPfLogros.string("tipoCuenta"));
            //request.cache = true;
            LogTAS.loguearRequest(contexto, request,"REQUEST_ALTA_PF_META");
            ApiResponse response = request.ejecutar();
            LogTAS.loguearResponse(contexto, response, "RESPONSE_ALTA_PF_META");
            ApiException.throwIf(!response.http(200), request, response);
            Objeto responseApi = new Objeto();
            responseApi.set("estado", "OK");
            responseApi.set("respuesta", response);             
            return responseApi;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error" , e);
            return error;
        }
    }

    //? este metodo deberia ser postAltaPF hay que hacerlo generico!!
    public static Objeto postConstitucionPF(ContextoTAS contexto, Objeto parametrosAlta){
        try{
            ApiRequest request = new ApiRequest("AltaPlazoFijo", "plazosfijos", "POST", "/v1/plazoFijos", contexto);
            request.body("canal", parametrosAlta.integer("canal"));
            request.body("capInteres", parametrosAlta.string("capInteres"));
            request.body("cuenta", parametrosAlta.string("cuenta"));
            request.body("idPlanAhorro", String.valueOf(parametrosAlta.integer("idPlanAhorro")));
            request.body("idcliente", parametrosAlta.string("idCliente"));
            request.body("moneda", parametrosAlta.integer("moneda"));
            request.body("monto", parametrosAlta.string("monto"));
            request.body("nroOperacion", null);
            request.body("periodo", parametrosAlta.string("periodo"));
            request.body("plazo", String.valueOf(parametrosAlta.integer("plazo")));
            request.body("renova", parametrosAlta.string("renova"));
            request.body("tipoCuenta", parametrosAlta.string("tipoCuenta"));
            request.body("tipoOperacion", parametrosAlta.string("tipoOperacion"));
            request.body("usuarioAlta", parametrosAlta.string("idCliente"));
            request.cache = true;
            LogTAS.loguearRequest(contexto, request, "REQUEST_ALTA_PF_LOGROS");
            ApiResponse response = request.ejecutar();
            LogTAS.loguearResponse(contexto, response, "RESPONSE_ALTA_PF_LOGROS");
            ApiException.throwIf(!response.http(200), request, response);
            Objeto responseApi = new Objeto();
            responseApi.set("estado", "OK");
            responseApi.set("respuesta", response);    
            return responseApi;
        }catch (Exception e){
         Objeto error = new Objeto();
         error.set("estado", "ERROR");
         error.set("error" , e);
         return error;
        }
    }

    public static Objeto getDetalleCuotasPFLogros(ContextoTAS contexto,String idCliente,int planContratado){
        ApiRequest request = new ApiRequest("ConsultaPlanAhorroDetalle", "plazosfijos", "GET", "/v1/planAhorro/detalle", contexto);
        request.query("codCliente", idCliente);
        request.query("planContratado", planContratado);
        request.query("opcion", "Q");
        request.query("operacion", "2");
        request.query("secuencial", 0);
        request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response;
    }

    public static Objeto getForzadoCuotaPFLogros(ContextoTAS contexto,String idCliente,int planContratado,int secuencial){
        ApiRequest request = new ApiRequest("ForzadoPlazoFijo", "plazosfijos", "GET", "/v1/{idcobis}/forzadoPlazoFijo", contexto);
        request.path("idcobis", idCliente);
        request.query("planContratado", planContratado);
        request.query("secuencialPlazoFijo", secuencial);
        request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response;
    }

    public static Objeto getBajaPFLogros(ContextoTAS contexto, String idCliente, int planContratado){
        ApiRequest request = new ApiRequest("BajaPlazoFijoAhorro", "plazosfijos", "GET", "/v1/{idCobis}/bajaPlazoFijoAhorro", contexto);
        request.path("idCobis", idCliente);
        request.query("planContratado", planContratado);
        request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response;
    }

    public static Objeto getModificarPFLogros(ContextoTAS contexto, Objeto params){
        ApiRequest request = new ApiRequest("ModificacionPlazoFijoAhorro", "plazosfijos", "GET", "/v1/{idCobis}/modificacionPlazoFijoAhorro", contexto);
        request.path("idCobis", params.string("idCobis"));
        request.query("cuenta", params.string("cuenta"));
        request.query("moneda", params.string("moneda"));
        request.query("monto", params.string("monto"));
        request.query("nombre", params.string("nombre"));
        request.query("planContratado", params.string("planContratado"));
        request.query("tipoCuenta", params.string("tipoCuenta"));
        request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response;
    }
}



