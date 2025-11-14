package ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.services;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.modelos.TASCajasAhorro;
import ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.modelos.TASCuentasCorriente;


public class TASRestCuenta {

    public static Objeto getCuentas(ContextoTAS contexto, String idCliente, boolean cancelados, boolean firmaConjunta, boolean firmantes){
        ApiRequest request = new ApiRequest("Cuentas", "cuentas", "GET",
                //"/v1/cuentas?idcliente={idCliente}&cancelados={cancelados}&firmaconjunta={firmaconjunta}"
                "/v1/cuentas?idcliente={idCliente}&cancelados={cancelados}&firmaconjunta={firmaconjunta}&firmantes={firmantes}", contexto);
        request.path("idCliente", idCliente);
        request.path("cancelados", String.valueOf(cancelados));
        request.path("firmaconjunta", String.valueOf(firmaConjunta));
        request.path("firmantes", String.valueOf(firmantes));
        request.cache = true;
        ApiResponse response = null;
        response = request.ejecutar();
        ApiException.throwIf(!response.http(200,202, 204), request, response);
        Objeto respFinal = new Objeto();
        for(Objeto obj :  response.objetos()){
            if(!obj.string("tipoTitularidad").equals("F")) respFinal.add(obj);
        }
        return respFinal;
    }
    
    public static List<TASCajasAhorro> getCajaAhorroById(ContextoTAS contexto, String idCuenta, Fecha fechaDesde, boolean historico, boolean validaCuentaEmpleado){
        ApiRequest request = new ApiRequest("CajasAhorroByIdCuenta", "cuentas", "GET","/v1/cajasahorros/{idCuenta}", contexto);
        request.path("idCuenta", idCuenta);
        request.query("fechadesde", String.valueOf(fechaDesde));
        request.query("historico", String.valueOf(historico));
        request.query("validacuentaempleado", String.valueOf(validaCuentaEmpleado));
        request.cache = true;

        ApiResponse response = null;
        response = request.ejecutar();
        ApiException.throwIf("NO_EXISTE_CUENTA", response.contains("NO EXISTE CUENTA"), request, response);
        ApiException.throwIf(!response.http(200,202), request, response);
        List<TASCajasAhorro> cajasAhorros = new ArrayList<>();
        for(Objeto obj : response.objetos()){
            TASCajasAhorro ca = obj.toClass(TASCajasAhorro.class);
            cajasAhorros.add(ca);
        }
        return cajasAhorros;
    }

    public static List<TASCuentasCorriente> getCuentaCorrienteById(ContextoTAS contexto, String idCuenta, Fecha fechaDesde, boolean historico, boolean validaCuentaEmpleado){
        ApiRequest request = new ApiRequest("CuentasCorrientesByIdCuenta", "cuentas", "GET", "/v1/cuentascorrientes/{idCuenta}", contexto);
        request.path("idCuenta", idCuenta);
        request.query("fechadesde", fechaDesde.string("yyyy-MM-dd"));
        request.query("validacuentaempleado", validaCuentaEmpleado);
        request.query("historico", historico);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200,202), request, response);
        List<TASCuentasCorriente> cuentasCorrientes = new ArrayList<>();
        for(Objeto obj : response.objetos()){
            TASCuentasCorriente cc = obj.toClass(TASCuentasCorriente.class);
            cuentasCorrientes.add(cc);
        }
        return cuentasCorrientes;
    }

    public static Objeto getAlias(ContextoTAS contexto, String cbu, boolean consultaAlias){
        ApiRequest request = new ApiRequest("Cuentas", "cuentas", "GET",
                "/v1/cuentas?consultaalias={consultaAlias}&cbu={cbu}", contexto);
        request.path("cbu", cbu);
        request.path("consultaAlias", String.valueOf(consultaAlias));
        request.cache = true;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200,202), request, response);
        return response;
    }

    public static Objeto getConsultaCheques(ContextoTAS contexto, String datoCuenta, String operacion){
        String tipoCuenta = operacion.equals("Pendientes") ? "idCuenta" : "numeroCuenta";
        ApiRequest request = new ApiRequest("ConsultaChequesCTE", "cheques", "GET",
                "/v1/cuentascorriente?"+tipoCuenta+"={datoCuenta}&operacion={operacion}", contexto);
        request.path("datoCuenta", datoCuenta);
        request.path("operacion", operacion);
        request.cache = true;
        ApiResponse response = request.ejecutar();
        String mensaje = "codigo"+ ": "+ "204";
        if(response.http(404) && response.body.contains(mensaje)) response.codigoHttp = 204;
        LogTAS.evento(contexto, "CONSULTA_CHEQUES_DETALLE_CC", response);
        ApiException.throwIf(!response.http(200,202,204), request, response);
        Objeto respFinal = new Objeto();
        respFinal.set("estado", "OK");
        respFinal.set("respuesta", response);
        return respFinal;
    }

    public static Objeto getUltimosMovimientosCa(ContextoTAS contexto, String idCuenta, Objeto parametros){
        ApiRequest request = new ApiRequest("CuentasGetMovimientos", "cuentas", "GET", "/v1/cajasahorros/{idCuenta}/movimientos", contexto);
        request.path("idCuenta", idCuenta);
        request.query("fechadesde", parametros.string("fechadesde"));
        request.query("fechahasta", parametros.string("fechahasta"));
        request.query("numeropagina", parametros.integer("numeropagina"));
        request.query("orden", parametros.string("orden"));
        request.query("pendientes", parametros.integer("pendientes"));
        request.query("tipomovimiento", parametros.string("tipomovimiento"));
        request.query("validactaempleado", parametros.string("validactaempleado"));
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 202, 204), request, response);
        return response;
    }
    public static Objeto getUltimosMovimientosCc(ContextoTAS contexto, String idCuenta, Objeto parametros){
        ApiRequest request = new ApiRequest("CuentasGetMovimientos", "cuentas", "GET", "/v1/cuentascorrientes/{idCuenta}/movimientos", contexto);
        request.path("idCuenta", idCuenta);
        request.query("fechadesde", parametros.string("fechadesde"));
        request.query("fechahasta", parametros.string("fechahasta"));
        request.query("numeropagina", parametros.integer("numeropagina"));
        request.query("orden", parametros.string("orden"));
        request.query("pendientes", parametros.integer("pendientes"));
        request.query("tipomovimiento", parametros.string("tipomovimiento"));
        request.query("validactaempleado", parametros.string("validactaempleado"));
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 202, 204), request, response);
        return response;
    }

    public static Objeto getBloqueosCuentaCA(ContextoTAS contexto, String idCuentaCobis){
        ApiRequest request = new ApiRequest("CuentasCajaAhorroBloqueos", "cuentas", "GET", "/v2/cajasahorros/{idcuenta}/bloqueos", contexto);
        request.path("idcuenta", idCuentaCobis);
        request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response;
    }

    public static Objeto getCuentasComitentesPorId(ContextoTAS contexto, String idCuentaComitente){
            ApiRequest request = new ApiRequest("CuentasPorComitente", "inversiones", "GET", "/v1/cuentas/{cuentacomitente}/comitente", contexto);
            request.path("cuentacomitente", idCuentaComitente);
            request.cache = true;

            ApiResponse response = request.ejecutar();
            ApiException.throwIf(!response.http(200, 204), request, response);
            return response;
    }

    public static Objeto validarCuentaDeposito(ContextoTAS contexto, String idCuenta){
        ApiRequest request = new ApiRequest("ValidarCuentaDeposito", "cuentas", "GET", "/v1/cuentas/{idCuenta}/depositos", contexto);
        request.path("idCuenta", idCuenta);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200,202), request, response);
        return response;
    }

    public static Objeto solicitarBajaCa(ContextoTAS contexto, Objeto datosBajaCa){
        ApiResponse response = null;
        try {
            ApiRequest request = new ApiRequest("CrearCaso", "postventa", "POST", "/crearCaso", contexto);
            request.query("codigotipificacion", datosBajaCa.string("codigo_tipificacion", "BAJACA_PEDIDO"));
            request.body("Canal", "TAS");
            request.body("CodigoTipificacion", datosBajaCa.string("codigo_tipificacion", "BAJACA_PEDIDO"));
            request.body("CodigoTributarioCliente", datosBajaCa.string("CodigoTributarioCliente"));

            List<Objeto> atrList = new ArrayList<Objeto>();
            Objeto ca = new Objeto();
            ca.set("nombreAtributo", "numeroproducto");
            Objeto valor = new Objeto();
            valor.set("Titulo", datosBajaCa.string("titulo"));
            valor.set("Valor", datosBajaCa.string("titulo"));
            valor.set("Padre", datosBajaCa.string("padre", null));
            valor.set("PorDefecto", false);
            valor.set("Valor2", datosBajaCa.string("fechaAlta"));
            valor.set("Valor3", datosBajaCa.string("cuenta"));
            valor.set("Valor4", datosBajaCa.string("usoFirma"));
            valor.set("Valor5", datosBajaCa.string("tipoTitularidad"));
            valor.set("Valor6", null);
            valor.set("Valor7", datosBajaCa.string("categoria"));
            valor.set("Valor8", datosBajaCa.string("saldo"));
            valor.set("Valor9", null);
            valor.set("Valor10", null);
            valor.set("Valor11", null);
            valor.set("Valor12", null);
            valor.set("Valor13", null);
            valor.set("Valor14", null);
            valor.set("Valor15", null);

            ca.set("Valor", valor);
            atrList.add(ca);

            Objeto saldo = new Objeto();
            saldo.set("nombreAtributo", "saldo");
            saldo.set("Valor", datosBajaCa.string("saldo"));
            atrList.add(saldo);

            request.body("Atributos", atrList);
            LogTAS.loguearRequest(contexto, request, "REQUEST_SOLICITAR_BAJA_CA");
            response = request.ejecutar();
            LogTAS.loguearResponse(contexto, response, "RESPONSE_SOLICITAR_BAJA_CA");
            ApiException.throwIf(!response.http(200, 202), request, response);
            return response;
        }catch (Exception e){
            Objeto error = new Objeto();
            if(e instanceof ApiException){
                ApiException ex = (ApiException) e;
                error.set("estado", "ERROR");
                error.set("error", ex);
                return error;
            }
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    public static Objeto getSaldosHistoricos(ContextoTAS contexto, String nroProducto, String nroCuenta){
        try{
            ApiRequest request = new ApiRequest("ConsultaSaldos", "cuentas", "GET", "/v2/cajasahorros/{nroCuenta}", contexto);
            request.path("nroCuenta", nroCuenta);
            Fecha fecha = Fecha.ayer();
            String fechaDesde = fecha.string("yyyy-MM-dd");
            request.query("fechadesde", fechaDesde);
            request.query("idproducto", nroProducto);        
            request.cache = true;

            ApiResponse response = request.ejecutar();
            ApiException.throwIf(!response.http(200, 202), request, response);
            return response;
        }catch(Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }
    

    public static Objeto getSeguimientoPaquetes(ContextoTAS contexto, String idCliente){
        ApiRequest request = new ApiRequest("ConsultaSeguimientoPaquetes", "paquetes", "GET", "/v1/paquetes/seguimientopaquete", contexto);
        request.query("idCobis", idCliente);
        request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 202, 204), request, response);
        return response;
    }

    public static Objeto postEnvioSolicitudCA(ContextoTAS contexto, Objeto solicitudParams){
        try{
            ApiRequest request = new ApiRequest("solicitudesPOST", "ventas", "POST", "/solicitudes", contexto);
            request.body("id", solicitudParams.string("id", null));
            request.body("tipoOperacion", solicitudParams.string("tipoOperacion"));
            request.body("canalOriginacion1", solicitudParams.integer("canalOriginacion1"));
            request.body("canalOriginacion2", solicitudParams.integer("canalOriginacion2"));
            request.body("canalOriginacion3", solicitudParams.string("canalOriginacion3"));
            request.body("canalVenta1", solicitudParams.string("canalVenta1"));
            request.body("canalVenta2", solicitudParams.string("canalVenta2"));
            request.body("canalVenta3", solicitudParams.string("canalVenta3"));
            request.body("canalVenta4", solicitudParams.integer("canalVenta4"));
            request.body("oficina", solicitudParams.string("oficina"));
            request.body("idSolicitud", solicitudParams.string("idSolicitud", null));
            //! ver si es necesario este cambio para MIGRACION DE URL API-VENTAS
            /*
             request.header("X-Usuario", contexto.config.string("tas_api_usuario"));
            request.header("X-Canal", contexto.config.string("tas_api_canaloriginacion1"));
            request.header("X-Subcanal", contexto.sesion().idTas);
             */
            request.cache = false;
            LogTAS.loguearRequest(contexto, request, "REQUEST_ENVIO_SOLICITUD_CA");
            ApiResponse response = request.ejecutar();
            LogTAS.loguearResponse(contexto, response, "RESPONSE_ENVIO_SOLICITUD_CA");
            ApiException.throwIf(!response.http(200, 202), request, response);
            return response;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }        
    }

    public static Objeto postEnviarIntegrantes(ContextoTAS contexto, Objeto paramsIntegrantes){
        try {
            ApiRequest request = new ApiRequest("integrantesPOST", "ventas", "POST", "/solicitudes/{nroSolicitud}/integrantes", contexto);
            request.path("nroSolicitud", paramsIntegrantes.string("nroSolicitud"));
            request.body("id", paramsIntegrantes.string("id", null));
            request.body("tipoOperacion", paramsIntegrantes.string("tipoOperacion"));
            request.body("numeroTributario", paramsIntegrantes.string("numeroTributario"));
            request.body("secuencia", paramsIntegrantes.integer("secuencia"));
            request.cache = false;
            //! ver si es necesario este cambio para MIGRACION DE URL API-VENTAS
            /*
             request.header("X-Usuario", contexto.config.string("tas_api_usuario"));
            request.header("X-Canal", contexto.config.string("tas_api_canaloriginacion1"));
            request.header("X-Subcanal", contexto.sesion().idTas);
             */
            request.header("X-Usuario", contexto.config.string("tas_api_usuario"));
            request.header("X-Canal", contexto.sesion().idTas);
            request.header("X-Subcanal", contexto.sesion().idTas);
            LogTAS.loguearRequest(contexto, request, "REQUEST_ENVIO_INTEGRANTES");
            ApiResponse response = request.ejecutar();
            LogTAS.loguearResponse(contexto, response, "RESPONSE_ENVIO_INTEGRANTES");
            ApiException.throwIf(!response.http(200, 202), request, response);
            return response;
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    public static Objeto putEnviarResolucion(ContextoTAS contexto, String nroSolicitud, String tipoOperacion){
        try{
        ApiRequest request = new ApiRequest("resolucionesPUT", "ventas", "PUT", "/solicitudes/{nroSolicitud}/resoluciones", contexto);
        request.path("nroSolicitud", nroSolicitud);
        request.body("TipoOperacion", tipoOperacion);
        request.cache = false;
        request.header("X-Usuario", contexto.config.string("tas_api_usuario"));
        request.header("X-Canal", contexto.sesion().idTas);
        request.header("X-Subcanal", contexto.sesion().idTas);
          //! ver si es necesario este cambio para MIGRACION DE URL API-VENTAS
            /*
             request.header("X-Usuario", contexto.config.string("tas_api_usuario"));
            request.header("X-Canal", contexto.config.string("tas_api_canaloriginacion1"));
            request.header("X-Subcanal", contexto.sesion().idTas);
             */
        LogTAS.loguearRequest(contexto, request, "REQUEST_ENVIO_RESOLUCION");
        ApiResponse response = request.ejecutar();
        LogTAS.loguearResponse(contexto, response, "RESPONSE_ENVIO_RESOLUCION");
        ApiException.throwIf(!response.http(200, 202), request, response);
        Objeto respuesta = new Objeto();
        respuesta.set("estado", "OK");
        respuesta.set("respuesta", response);
        return respuesta;
    }catch(Exception e){
        Objeto error = new Objeto();
        error.set("estado", "ERROR");
        error.set("error", e);
        return error;
    }        
    }

    public static Objeto postFinalizarSolicitudCA(ContextoTAS contexto,String nroSolicitud){
        try{
            ApiRequest request = new ApiRequest("solicitudesGET", "ventas", "GET", "/solicitudes/{nroSolicitud}?estado=finalizar", contexto);
            request.path("nroSolicitud", nroSolicitud);
            request.cache = true;
            request.header("X-Usuario", contexto.config.string("tas_api_usuario"));
            request.header("X-Canal", contexto.sesion().idTas);
            request.header("X-Subcanal", contexto.sesion().idTas);
              //! ver si es necesario este cambio para MIGRACION DE URL API-VENTAS
            /*
             request.header("X-Usuario", contexto.config.string("tas_api_usuario"));
            request.header("X-Canal", contexto.config.string("tas_api_canaloriginacion1"));
            request.header("X-Subcanal", contexto.sesion().idTas);
             */
            LogTAS.loguearRequest(contexto, request, "REQUEST_FINALIZAR_SOLICITUD_CA");
            ApiResponse response = request.ejecutar();
            LogTAS.loguearResponse(contexto, response, "RESPONSE_FINALIZAR_SOLICITUD_CA");
            ApiException.throwIf(!response.http(200, 202), request, response);
            Objeto respuesta = new Objeto();
            respuesta.set("estado", "OK");
            respuesta.set("respuesta", response);
            return respuesta;
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
        
    }

    public static Objeto postEnviarCA(ContextoTAS contexto, Objeto params){
        try {
            ApiRequest request = new ApiRequest("cajaAhorroPOST", "ventas", "POST", "/solicitudes/{nroSolicitud}/cajaAhorro", contexto);
            request.path("nroSolicitud", params.string("nroSolicitud"));
            request.body("categoria", params.string("categoria"));
            request.body("productoBancario", params.string("productoBancario"));
            
            Objeto domicilio = params.objeto("domicilioResumen");
            request.body("domicilioResumen", domicilio);

            request.body("oficial", params.string("oficial"));
            request.body("cobroPrimerMantenimiento", params.bool("cobroPrimerMantenimiento"));
            request.body("moneda", params.string("moneda"));
            request.body("origen", params.string("origen"));
            request.body("usoFirma", params.string("usoFirma"));
            request.body("ciclo", params.string("ciclo"));
            request.body("resumenMagnetico", params.bool("resumenMagnetico"));
            request.body("transfiereAcredHab", params.bool("transfiereAcredHab"));
            
            List<Objeto> integrantes = new ArrayList<>();
            integrantes.add(params.objeto("integrantes"));
            request.body("integrantes", integrantes);
            
            request.body("cuentaLegales", params.objeto("cuentasLegales"));

            request.body("tipoOperacion", params.string("tipoOperacion"));
            request.cache = false;
              //! ver si es necesario este cambio para MIGRACION DE URL API-VENTAS
            /*
             request.header("X-Usuario", contexto.config.string("tas_api_usuario"));
            request.header("X-Canal", contexto.config.string("tas_api_canaloriginacion1"));
            request.header("X-Subcanal", contexto.sesion().idTas);
             */
            LogTAS.loguearRequest(contexto, request, "REQUEST_ENVIO_CA");
            ApiResponse response = request.ejecutar();
            LogTAS.loguearResponse(contexto, response, "RESPONSE_ENVIO_CA");
            ApiException.throwIf(!response.http(200, 202), request, response);
            Objeto respuesta = new Objeto();
            respuesta.set("estado", "OK");
            respuesta.set("respuesta", response);
            return respuesta;
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    public static Objeto postEnviarTD(ContextoTAS contexto,Objeto parametrosEnviarTD){
        try{
            ApiRequest request = new ApiRequest("tarjetaDebitoPOST", "ventas", "POST", "/solicitudes/{nroSolicitud}/tarjetaDebito", contexto);
            request.path("nroSolicitud", parametrosEnviarTD.string("nroSolicitud"));
            request.body("tipo", parametrosEnviarTD.string("tipo"));
            request.body("domicilio", parametrosEnviarTD.objeto("domicilio"));
            request.body("grupo", parametrosEnviarTD.string("grupo"));
            request.body("tipoCuentaComision", parametrosEnviarTD.string("tipoCuentaComision"));
            request.body("numeroCtaComision", parametrosEnviarTD.string("numeroCuentaComision"));
            
            List<Objeto> integrantes = new ArrayList<>();
            integrantes.add(parametrosEnviarTD.objeto("integrantes"));
            request.body("integrantes", integrantes);

            List<Objeto> tarjetaDebitoCuentasOperativas = new ArrayList<>();
            tarjetaDebitoCuentasOperativas.add(parametrosEnviarTD.objeto("tarjetasDebitoCuentasOperativas"));
            request.body("tarjetaDebitoCuentasOperativas", tarjetaDebitoCuentasOperativas);
            request.body("tipoOperacion", parametrosEnviarTD.string("tipoOperacion"));
            request.body("EsVirtual", parametrosEnviarTD.bool("EsVirtual"));
            request.cache = false;
              //! ver si es necesario este cambio para MIGRACION DE URL API-VENTAS
            /*
             request.header("X-Usuario", contexto.config.string("tas_api_usuario"));
            request.header("X-Canal", contexto.config.string("tas_api_canaloriginacion1"));
            request.header("X-Subcanal", contexto.sesion().idTas);
             */
            LogTAS.loguearRequest(contexto, request, "REQUEST_ENVIO_TD");
            ApiResponse response = request.ejecutar();
            LogTAS.loguearResponse(contexto, response, "RESPONSE_ENVIO_TD");
            ApiException.throwIf(!response.http(200, 202), request, response);

            Objeto respuesta = new Objeto();
            respuesta.set("estado", "OK");
            respuesta.set("respuesta", response);
            return respuesta;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

}

