package ar.com.hipotecario.canal.tas.modulos.cuentas.transferencias.controllers;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.cuentas.transferencias.modelos.TASTransferenciasPropia;
import ar.com.hipotecario.canal.tas.modulos.cuentas.transferencias.services.TASRestTransferencias;
import ar.com.hipotecario.canal.tas.modulos.cuentas.transferencias.utils.UtilesTransferencias;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.inversiones.servicios.TASRestInversiones;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;
import ar.com.hipotecario.canal.tas.shared.utils.models.enums.TASCodigoMonedaEnum;
import ar.com.hipotecario.canal.tas.shared.utils.models.strings.TASMensajesString;

import java.math.BigDecimal;

public class TASTransferenciasController {

    
    public static Objeto ejecutarTransferencia(ContextoTAS contexto){
        String tipoCta = "";
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idCuenta = contexto.parametros.string("idCuenta");
            if (idCuenta.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Cuenta sin especificar");
            Objeto validacionHorario = UtilesTransferencias.comprobarHorario(contexto);
            if(!validacionHorario.string("estado").equals("false")){
                String estado = validacionHorario.string("estado");
                return estado.equals("ERROR") ?
                        RespuestaTAS.error(contexto, "TASTransferenciasController - ejecutarTransferencia()" ,(Exception) validacionHorario.get("error"))
                        : validacionHorario;
            }
            TASTransferenciasPropia datosTf = validarDatosTf(contexto, idCliente, idCuenta);
            if(datosTf.getError() != null) return RespuestaTAS.error(contexto, "TASTransferenciasController - ejecutarTransferencia()", (Exception) datosTf.getError().get("error"));
            tipoCta = datosTf.getTipoCuentaOrigen();
            LogTAS.evento(contexto,"INICIO_TRANSFERENCIA", datosTf.objeto().toSimpleJson());
            Objeto response = TASRestTransferencias.postEjecutarTransferencia(contexto, datosTf);
            LogTAS.evento(contexto,"FIN_TRANSFERENCIA", response);
            return response;
        } catch (Exception e) {
            return validarErrorAPI(contexto, e, tipoCta);
        }
        
    }

    private static Objeto validarErrorAPI(ContextoTAS contexto, Exception e, String tipoCta){
        if(e instanceof ApiException){
            Objeto response = new Objeto();
            ApiException apiException = (ApiException) e;
            int codigoHttp = apiException.response.codigoHttp;
            String codigo = apiException.response.string("codigo");
            String mensajeAlDesarrollador = apiException.response.string("mensajeAlDesarrollador");
            if(mensajeAlDesarrollador.contains("FONDOS INSUFICIENTES")){
                response.set("estado", "FONDOS_INSUFICIENTES");
                response.set("codigoHttp", codigoHttp);
                response.set("codigo", codigo);
                response.set("mensajeAlDesarrollador", mensajeAlDesarrollador);
                response.set("mensaje", tipoCta.equals("AHO") ? 
                TASMensajesString.CAJA_AHORRO_FONDOS_INSUFICIENTES.getTipoMensaje() : TASMensajesString.CUENTA_CORRIENTE_FONDOS_INSUFICIENTES.getTipoMensaje());
                LogTAS.error(contexto, apiException);
                return response;
            }
            return RespuestaTAS.error(contexto, "TASTransferenciasController - ejecutarTransferencia()", e);
        }

        return RespuestaTAS.error(contexto, "TASTransferenciasController - ejecutarTransferencia()", e);
    }
    
    private static TASTransferenciasPropia validarDatosTf(ContextoTAS contexto, String idCliente, String idCuenta){
        TASTransferenciasPropia datosTf = new TASTransferenciasPropia();
        try { 
            datosTf.setCuentaOrigen(idCuenta);
            datosTf.setCuentaDestino(contexto.parametros.string("cuentaDestino"));
            datosTf.setIdCliente(idCliente);
            datosTf.setIdMoneda(contexto.parametros.integer("idMoneda", 80));
            datosTf.setIdMonedaOrigen(contexto.parametros.integer("idMonedaOrigen", 80));
            datosTf.setIdMonedaDestino(contexto.parametros.integer("idMonedaDestino", 80));
            datosTf.setImporte(contexto.parametros.bigDecimal("importe"));
            datosTf.setModoSimulacion(contexto.parametros.bool("modoSimulacion", false));
            datosTf.setReverso(contexto.parametros.bool("reverso", false));
            datosTf.setTipoCuentaOrigen(contexto.parametros.string("tipoCuentaOrigen","AHO"));
            datosTf.setTipoCuentaDestino(contexto.parametros.string("tipoCuentaDestino","AHO"));
            datosTf.setTransaccion(contexto.parametros.integer("transaccion", 0));           
            datosTf.setDdjjCompraventa(contexto.parametros.bool("ddjjCompraVenta", false));
            datosTf.setCotizacion(contexto.parametros.bigDecimal("cotizacion", "1"));
            datosTf.setImportePesos(contexto.parametros.bigDecimal("importePesos"));
            datosTf.setImporteDivisa(contexto.parametros.bigDecimal("importeDivisa","0"));
            datosTf.setEfectivo(contexto.parametros.bool("efectivo", false));            
            datosTf.setIdCuenta(idCuenta);
            datosTf.setCuentaPropia(true);
            datosTf.setInmediata(false);
            datosTf.setEspecial(false);
            return datosTf;
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            datosTf.setError(error);
            return datosTf;
        }
        
    }

     public static Objeto postOperacionesMonedaExtranjera(ContextoTAS contexto){
        String tipoCta = "";
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idCuenta = contexto.parametros.string("idCuenta");
            if (idCuenta.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Cuenta sin especificar");
            boolean modoSimulacion = contexto.parametros.bool("modoSimulacion", false);
            Objeto validacionHorario = UtilesTransferencias.comprobarHorario(contexto);
            if(!validacionHorario.string("estado").equals("false")){
                String estado = validacionHorario.string("estado");
                return estado.equals("ERROR") ?
                        RespuestaTAS.error(contexto, "TASTransferenciasController - ejecutarTransferencia()" ,(Exception) validacionHorario.get("error"))
                        : validacionHorario;
            }
            TASTransferenciasPropia datosCV = validarDatosCompraVenta(contexto, idCliente, idCuenta, modoSimulacion);
            if(datosCV.getError() != null) return RespuestaTAS.error(contexto, "TASTransferenciasController - ejecutarTransferencia()", (Exception) datosCV.getError().get("error"));
            tipoCta = datosCV.getTipoCuentaOrigen();
            Objeto responseCotizacion = getCotizacionMoneda(contexto, clienteSesion);
            if(responseCotizacion.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto,"TASTransferenciasController - getCotizacionMoneda()", (Exception) responseCotizacion.get("error"));
            BigDecimal cotDolar = responseCotizacion.bigDecimal("cotDolar");
            if (datosCV.getIdMonedaDestino().equals(2)) datosCV.setImporte(datosCV.getImporte().multiply(cotDolar));
            LogTAS.evento(contexto, "INICIO_OPERACION_MONEDA_EXT", contexto.parametrosOfuscados());
            Objeto responseCV = modoSimulacion ? simularCV(contexto, datosCV) : ejecutarCV(contexto, datosCV, cotDolar);
            if(responseCV.string("estado").equals("ERROR")) return validarErrorAPICV(contexto, (Exception) responseCV.get("error"), tipoCta);
            return armarResponseCompraVenta(contexto, responseCV, cotDolar);
        }catch (Exception e){
            return validarErrorAPICV(contexto, e, tipoCta);
        }
    }

    private static Objeto getCotizacionMoneda(ContextoTAS contexto, TASClientePersona cliente){
        try {
            Objeto cotizacionResponse = TASRestInversiones.getCotizacionByMercado(contexto, cliente.getIdCliente(), "06");
            Objeto cotDolar = new Objeto();
            cotDolar.set("estado", "OK");
            for (Objeto responseApi : cotizacionResponse.objetos()) {
                if (responseApi.string("moneda").equals("2")) {
                    cotDolar.set("cotDolar" , responseApi.bigDecimal("venta"));
                }
            }
            return cotDolar;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }
    //TODO QUITAR MOCK DE SIMULADOR DE COTIZACION DE MONEDA.....
    public static Objeto generarMock(){
        Objeto mock = new Objeto();
        mock.set("operacion", "0");
        mock.set("transaccion", "0");
        mock.set("cotizacion", "15.2");
        mock.set("importe", "1520.0");
        mock.set("fecha", "2019/04/02");
        mock.set("servicio", "TRANSF.TERCEROS");
        mock.set("referencia", "307051000050124");
        mock.set("idMonedaDestino", "2");
        mock.set("cuentaDestino", "205100013927615");
        mock.set("cuentaOrigen", "405100012723564");
        mock.set("tipoCuentaDestino", "AHO");
        mock.set("tipoCuentaOrigen", "AHO");
        mock.set("importeDivisa", "100.0");
        mock.set("importePesos", "1520.0");
        mock.set("impRg4815", "200.0");
        mock.set("monedaOrigen", "80");
        mock.set("estado", "P");
        mock.set("idCobis", "32595319");
        mock.set("idProceso", "9151240");
        mock.set("monto", "1520.00");
        mock.set("numeroError", "0");
        mock.set("recibo", "0");
        mock.set("impuestos", "0.00");
        mock.set("comision", "0.00");
        mock.set("totalTransferencia", "1520.00");
        return mock;
    }
    private static Objeto simularCV(ContextoTAS contexto, TASTransferenciasPropia datosCV){
        try {
            LogTAS.evento(contexto,"SIMULACION_COMPRA_VENTA_MON_EXT", datosCV.objeto().toSimpleJson());
            Objeto responseCompraVenta = TASRestTransferencias.postSimularTransferenciaCV(contexto, datosCV);
            return responseCompraVenta;
        }catch (Exception e){
            // capturo tipo error si es "121013", genero mock
//            if(e instanceof ApiException){
//                Objeto error = new Objeto();
//                Objeto response = new Objeto();
//                ApiException apiEx = (ApiException) e;
//                String codigo = apiEx.response.string("codigo");error.set("estado", "ERROR");
//                error.set("error", apiEx);
//                response = codigo.equals("121013") ? generarMock() : error;
//                return response;
//            }
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    private static Objeto ejecutarCV(ContextoTAS contexto, TASTransferenciasPropia datosCV, BigDecimal cotDolar){
        try {            
            TASTransferenciasPropia datosModificados = UtilesTransferencias.modificarParametrosCompraVenta(contexto, datosCV, cotDolar);
            LogTAS.evento(contexto, "EJECUCION_COMPRA_VENTA_MON_EXT", datosModificados.objeto().toSimpleJson());
            Objeto responseCompraVenta = TASRestTransferencias.postEjecutarTransferenciaCV(contexto, datosModificados);
            return responseCompraVenta;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    private static TASTransferenciasPropia validarDatosCompraVenta(ContextoTAS contexto, String idCliente, String idCuenta, boolean modoSimulacion){
        TASTransferenciasPropia datosCV = new TASTransferenciasPropia();
        try{
            datosCV.setIdCliente(idCliente);
            datosCV.setIdCuenta(idCuenta);
            datosCV.setCuentaPropia(true);
            datosCV.setInmediata(false);
            datosCV.setEspecial(false);
            datosCV.setCotizacion(contexto.parametros.bigDecimal("cotDolar", "0"));
            datosCV.setImporte(contexto.parametros.bigDecimal("importe"));
            datosCV.setImportePesos(contexto.parametros.bigDecimal("importePesos", "0"));
            datosCV.setImporteDivisa(contexto.parametros.bigDecimal("importeDivisa", "0"));
            datosCV.setImporteDestino(contexto.parametros.bigDecimal("importeDestino", "0"));
            datosCV.setModoSimulacion(modoSimulacion);
            datosCV.setReverso(contexto.parametros.bool("reverso", false));
            datosCV.setCuentaOrigen(idCuenta);
            datosCV.setCuentaDestino(contexto.parametros.string("cuentaDestino"));
            datosCV.setTipoCuentaOrigen(contexto.parametros.string("tipoCuentaOrigen"));
            datosCV.setTipoCuentaDestino(contexto.parametros.string("tipoCuentaDestino"));
            datosCV.setIdMoneda(contexto.parametros.integer("idMoneda"));
            datosCV.setIdMonedaOrigen(contexto.parametros.integer("idMoneda"));
            datosCV.setIdMonedaDestino(contexto.parametros.integer("idMonedaDestino"));
            datosCV.setTransaccion(contexto.parametros.integer("transaccion", 0));
            datosCV.setEfectivo(contexto.parametros.bool("efectivo", false));
            datosCV.setIdentificacionPersona(contexto.sesion().clienteTAS.numeroIdentificacionTributaria);
            datosCV.setPaisDocumento(80);
            datosCV.setMontoEnDivisa(contexto.parametros.bigDecimal("importeDivisa", "0"));
            datosCV.setDdjjCompraventa(contexto.parametros.bool("ddjjCompraventa", true));
            return datosCV;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            datosCV.setError(error);
            return datosCV;
        }
    }

    private static Objeto armarResponseCompraVenta(ContextoTAS contexto, Objeto responseAPI, BigDecimal cotDolar){
        Objeto responseCV = new Objeto();
        responseCV.set("numeroTicket", responseAPI.string("recibo"));
        responseCV.set("fechaOperacion", responseAPI.string("fechaHora"));
        responseCV.set("numeroControl", responseAPI.string("recibo"));
        if(responseAPI.string("idMonedaDestino").equals("80")) {
            responseCV.set("importeOrigen", Double.valueOf(responseAPI.bigDecimal("importeDivisa", "0.00").toString()));
            responseCV.set("importeDestino", Double.valueOf(responseAPI.bigDecimal("importePesos", "0.00").toString()));
            responseCV.set("tipoOperacion", "V");
        }else {
            responseCV.set("importeOrigen", Double.valueOf(responseAPI.bigDecimal("importePesos", "0.00").toString()));
            responseCV.set("importeDestino", Double.valueOf(responseAPI.bigDecimal("importeDivisa", "0.00").toString()));
            responseCV.set("tipoOperacion", "C");
            if(responseAPI.string("monedaOrigen").equals("80")) {
                responseCV.set("importeTotal", Double.valueOf(responseAPI.bigDecimal("importeTotal").toString()));
                /* 
                double impPais = Double.valueOf(responseAPI.bigDecimal("importePesos").toString());
                responseCV.set("importeImpPais", (impPais * 30)/100);
                */
                //! Modificacion, quita del Impuesto Pais
                double importeImpPais = 0.00;
                responseCV.set("importeImpPais", importeImpPais);
                responseCV.set("impRgAfip4815", Double.valueOf(responseAPI.bigDecimal("impRg4815").toString()));
                String leyenda = contexto.config.string("tas_leyenda_aceptadecljuradacompradolares");
                leyenda = leyenda.replaceAll("_monto_", responseAPI.string("importeDestino"));
                responseCV.set("leyendaAceptaDDJJ", leyenda);
            }
        }
        int monedaOrigen = responseAPI.integer("monedaOrigen");
        int monedaDestino = responseAPI.integer("idMonedaDestino");
        responseCV.set("codigoMonedaOrigen", monedaOrigen);
        responseCV.set("codigoMonedaOrigenDescripcion", monedaOrigen == 80 ? TASCodigoMonedaEnum.PESOS.getCodigoMoneda() : TASCodigoMonedaEnum.DOLARES.getCodigoMoneda());
        responseCV.set("codigoMonedaDestino", monedaDestino);
        responseCV.set("codigoMonedaDestinoDescripcion", monedaDestino == 2 ? TASCodigoMonedaEnum.DOLARES.getCodigoMoneda() : TASCodigoMonedaEnum.PESOS.getCodigoMoneda());
        responseCV.set("tasaConversion", Double.valueOf(responseAPI.bigDecimal("cotizacion", cotDolar.toString()).toString()));
        responseCV.set("cotDolar", Double.valueOf(cotDolar.toString()));
        responseCV.set("codigoReserva", responseAPI.string("transaccion"));
        LogTAS.evento(contexto, "FIN_OPERACION_MONEDA_EXT", responseCV);
        return responseCV;
    }

    private static Objeto validarErrorAPICV(ContextoTAS contexto, Exception e, String tipoCta){
        if(e instanceof ApiException){
            Objeto response = new Objeto();
            ApiException apiException = (ApiException) e;
            int codigoHttp = apiException.response.codigoHttp;
            String codigo = apiException.response.string("codigo");
            String mensajeAlDesarrollador = apiException.response.string("mensajeAlDesarrollador");
            if(mensajeAlDesarrollador.contains("FONDOS INSUFICIENTES")){
                response.set("estado", "FONDOS_INSUFICIENTES");
                response.set("codigoHttp", codigoHttp);
                response.set("codigo", codigo);
                response.set("mensajeAlDesarrollador", mensajeAlDesarrollador);
                response.set("mensaje", tipoCta.equals("AHO") ?
                        TASMensajesString.CAJA_AHORRO_FONDOS_INSUFICIENTES.getTipoMensaje() : TASMensajesString.CUENTA_CORRIENTE_FONDOS_INSUFICIENTES.getTipoMensaje());
                LogTAS.error(contexto, apiException);
                return response;
            } else if (mensajeAlDesarrollador.contains("MONTO BLOQUEADO SUPERA AL DISPONIBLE")){
                response.set("estado", "FONDOS_INSUFICIENTES");
                response.set("codigoHttp", codigoHttp);
                response.set("codigo", codigo);
                response.set("mensajeAlDesarrollador", mensajeAlDesarrollador);
                response.set("mensaje", tipoCta.equals("AHO") ?
                        TASMensajesString.CAJA_AHORRO_FONDOS_INSUFICIENTES.getTipoMensaje() : TASMensajesString.CUENTA_CORRIENTE_FONDOS_INSUFICIENTES.getTipoMensaje());
                LogTAS.error(contexto, apiException);
                return response;
            } else if (codigoHttp == 412) {
                response.set("estado", "PRECONDICION");
                response.set("codigoHttp", codigoHttp);
                response.set("codigo", codigo);
                response.set("mensajeAlDesarrollador", mensajeAlDesarrollador);
                if(mensajeAlDesarrollador.contains("CONTROL DE TOPES")){
                    response.set("mensaje", TASMensajesString.COMPRA_VENTA_USD_EXCEDE_CUPO.getTipoMensaje());
                } else {
                    response.set("mensaje", apiException.response.string("mensajeAlUsuario"));
                }
                LogTAS.error(contexto, apiException);
                return response;
            } else if (codigoHttp == 400) {
                response.set("estado", "COMPRA_VENTA");
                response.set("codigoHttp", codigoHttp);
                response.set("codigo", codigo);
                response.set("mensajeAlDesarrollador", mensajeAlDesarrollador);
                if(mensajeAlDesarrollador.contains("Excede cupo mensual") || mensajeAlDesarrollador.contains("Excedido otra causal") || mensajeAlDesarrollador.contains("supera el máximo de compra permitido")){
                    response.set("estado", "CUPO_MENSUAL");
                    response.set("mensaje", TASMensajesString.COMPRA_VENTA_USD_EXCEDE_CUPO.getTipoMensaje());
                } else if (codigo.equals("190528")){
                    response.set("mensaje", TASMensajesString.COMPRA_VENTA_USD_FERIADO.getTipoMensaje());
                }
                else {
                    response.set("mensaje", TASMensajesString.COMPRA_VENTA_USD_INHABILITADA.getTipoMensaje());
                }
                LogTAS.error(contexto, apiException);
                return response;
            }
            return RespuestaTAS.error(contexto, "TASTransferenciasController - ejecutarTransferencia()", e);
        }

        return RespuestaTAS.error(contexto, "TASTransferenciasController - ejecutarTransferencia()", e);
    }

    
}
