package ar.com.hipotecario.canal.tas.modulos.tarjetas.controllers;


import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.services.TASRestCuenta;
import ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.utils.UtilesCuenta;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.utils.UtilesDepositos;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidada.controllers.TASPosicionConsolidadaController;
import ar.com.hipotecario.canal.tas.modulos.tarjetas.services.TASRestTarjetas;
import ar.com.hipotecario.canal.tas.modulos.tarjetas.utils.UtilesTarjetas;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.notificaciones.controllers.TASNotificacionesController;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.productos.utiles.UtilesProductos;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.scheduler.controller.TASSchedulerController;
import ar.com.hipotecario.canal.tas.shared.utils.models.strings.TASMensajesString;

import java.util.Spliterators;

public class TASTarjetasController {

    public static Objeto getTarjetaCredito(ContextoTAS contexto){
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idTarjeta = contexto.parametros.string("tarjetaId");
            if (idTarjeta.isEmpty()) return RespuestaTAS.sinParametros(contexto, "Uno o mas parametros no ingresados");
            Objeto response = TASRestTarjetas.getTarjetaCredito(contexto, idTarjeta);
            return response == null ?  RespuestaTAS.sinResultados(contexto, "no existe tarjeta para ese ID") : response;
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASTarjetasController - getTarjetaCredito()", e);
        }
    }
    public static Objeto getResumenTarjetaCredito(ContextoTAS contexto){
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idCuenta = contexto.parametros.string("cuentaId");
            String idTarjeta = contexto.parametros.string("tarjetaId");
            if (idCuenta.isEmpty() || idTarjeta.isEmpty())
                return RespuestaTAS.sinParametros(contexto, "Uno o mas parametros no ingresados");
            Objeto response = TASRestTarjetas.getResumenCuentaTC(contexto, idCuenta, idTarjeta);
            return response.isEmpty() ?  RespuestaTAS.sinResultados(contexto, "no existe tarjeta para ese ID") :  response;
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASTarjetasController - getResumenTarjetaCredito()", e);
        }
    }

    public static Objeto patchCambioFormaPago(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            Objeto datosCambioFormaPago = armarDatosParaCambio(contexto);
            LogTAS.evento(contexto, "INICIO_CAMBIO_FORMA_PAGO", datosCambioFormaPago);
            if(datosCambioFormaPago.string("estado").equals("SIN_PARAMETROS")) return RespuestaTAS.sinParametros(contexto,"falta uno o mas parametros");
            Objeto response = TASRestTarjetas.patchCambioFormaPago(contexto, datosCambioFormaPago);
            LogTAS.evento(contexto, "FIN_CAMBIO_FORMA_PAGO", response);
            return response.string("estado").equals("OK") ? response.objeto("response") : armarResponseParaCambio(contexto, response);
        }catch (Exception e){
            return null;
        }
    }

    private static Objeto armarDatosParaCambio(ContextoTAS contexto){
        try {
            Objeto datos = new Objeto();
            datos.set("formaPago", contexto.parametros.string("formaPago"));
            datos.set("sucursal", contexto.parametros.string("sucursal"));
            datos.set("cuenta", contexto.parametros.string("cuentaId"));
            datos.set("numeroCuenta", contexto.parametros.string("numeroCuenta"));
            datos.set("tipoCuenta", contexto.parametros.string("tipoCuenta"));
            datos.set("idMoneda", contexto.parametros.string("idMoneda", "80"));
            String kioscoId = contexto.parametros.string("kioscoId","0");
            datos.set("ticket", UtilesDepositos.getNumeroTicket(kioscoId));
            datos.set("codigoCliente", "TASI");
            datos.set("nombreCliente", "TASI");
            datos.set("apellidoCliente", "TASI");
            return datos;
        }catch (Exception e){
            return RespuestaTAS.sinParametros(contexto, "falta uno o mas parametros");
        }
    }

    private static Objeto armarResponseParaCambio(ContextoTAS contexto, Objeto response){
        int codigoHTTP = response.integer("codigoHTTP");
        String codigoMW = response.string("codigoMW");
        Exception errorAPI = (Exception ) response.get("error");
        if(codigoHTTP == 404 || codigoHTTP == 412){
            if(codigoMW.equals("50050")){
                return RespuestaTAS.error(contexto, TASMensajesString.TARJETAS_ERROR_SOLICITUD_YA_GENERADA.toString(),
                        TASMensajesString.TARJETAS_ERROR_SOLICITUD_YA_GENERADA.getTipoMensaje(), "El Cliente tiene una solicitud en curso");
            }
            if(codigoMW.equals("50030")){
                String formaPago = contexto.parametros.string("formaPago");
                String cta = contexto.parametros.string("numeroCuenta");
                String ultimosCuatro = cta.substring(cta.length() - 4);
                if(formaPago.equals("02") || formaPago.equals("04")) return RespuestaTAS.error(contexto,TASMensajesString.TARJETAS_CAMBIO_PAGO_ERROR_PAGO_MINIMO.toString(),
                        TASMensajesString.TARJETAS_CAMBIO_PAGO_ERROR_PAGO_MINIMO.getTipoMensaje() + ultimosCuatro , "El cliente esta adherido al debito automatico");
                if(formaPago.equals("03") || formaPago.equals("05")) return RespuestaTAS.error(contexto, TASMensajesString.TARJETAS_CAMBIO_PAGO_ERROR_PAGO_TOTAL.toString(),
                        TASMensajesString.TARJETAS_CAMBIO_PAGO_ERROR_PAGO_TOTAL.getTipoMensaje() + ultimosCuatro, "El cliente esta adherido al debito automatico");
            }
            if(codigoHTTP == 204 || codigoHTTP == 404){
                return RespuestaTAS.error(contexto, TASMensajesString.TARJETAS_ERROR_VACIO.toString(),
                        TASMensajesString.TARJETAS_ERROR_VACIO.getTipoMensaje(), "Error 204 - 404");
            }
        }
        return RespuestaTAS.error(contexto, "TASTarjetasController - armarResponseParaCambio()", errorAPI);
    }

    public static Objeto envioResumenTC(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String cuentaTarjeta = contexto.parametros.string("cuentaTarjeta");
            String mail = contexto.parametros.string("mail");
            if(cuentaTarjeta.isEmpty() || mail.isEmpty()) return RespuestaTAS.sinParametros(contexto, "uno o mas paremetros incompletos");
            Objeto fileResponse = TASRestTarjetas.getUltimaLiquidacion(contexto, cuentaTarjeta);
            if(fileResponse.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASTarjetasController - envioResumenTC()", (Exception) fileResponse.get("error"));
            if(fileResponse.isEmpty()) return RespuestaTAS.sinResultados(contexto, "No se encontro ultima liquidacion");
            String file = fileResponse.objeto("response").string("file");
            Objeto datosParaMail = armarDatosParaMail(contexto, mail, file);
            LogTAS.evento(contexto, "INICIO_ENVIO_RESUMEN", datosParaMail);
            Objeto notificacionesResponse = TASNotificacionesController.envioMail(contexto, datosParaMail);
            if(notificacionesResponse.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASTarjetasController - envioResumenTC()",(Exception) notificacionesResponse.get("error"));
            LogTAS.evento(contexto, "FIN_ENVIO_RESUMEN", notificacionesResponse);
            return notificacionesResponse.objeto("response");
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASTarjetasController - envioResumenTC()", e);
        }
    }

    private static Objeto armarDatosParaMail(ContextoTAS contexto, String mail, String file){
        try{
        final Config config = new Config();
        Objeto datos = new Objeto();
        datos.set("nroPlantilla",config.string("tas_envioresumen_nroplantilla"));
        datos.set("plantilla",config.string("tas_envioresumen_plantilla"));
        datos.set("nombreRemitente",config.string("tas_envioresumen_nombreremitente"));
        datos.set("asunto",config.string("tas_envioresumen_asunto"));
        datos.set("nombre",config.string("tas_envioresumen_nombre"));
        datos.set("tipoMedio",config.string("tas_envioresumen_tipoMedio"));
        datos.set("file", file);
        datos.set("correo", mail);
        return datos;
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "armarDatosParaMail - ObtenerVE()", e);
        }
    }

    public static Objeto getMensajesFormaPago(ContextoTAS contexto){
        try{
            final Config config = new Config();
            Objeto leyendas = new Objeto();
            leyendas.set("leyenda_tarjeta_pago_total",config.string("tas_leyenda_tarjetapagototal"));
            leyendas.set("leyenda_tarjeta_pago_minimo",config.string("tas_leyenda_tarjetapagominimo"));
            return leyendas;
        }catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASTarjetasController - getMensajesFormaPago()", e);
        }
    }


    public static Objeto postPagoTarjetaDebitoCuenta(ContextoTAS contexto){
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");

            Objeto datosPago = armarDatosPagoTarjeta(contexto);
            LogTAS.evento(contexto, "INICIO_PAGO_TARJETA_DEBITO_CUENTA", datosPago);
            if (datosPago.string("estado").equals("ERROR"))
                return RespuestaTAS.sinParametros(contexto, "uno o mas parametros incorrectos");
            Objeto response = TASRestTarjetas.postPagoTarjetaDebitoCuenta(contexto, datosPago);
            LogTAS.evento(contexto, "FIN_PAGO_TARJETA_DEBITO_CUENTA", response);
            return response;
        }catch (Exception e){
            if(e instanceof ApiException){
                ApiException apiException = (ApiException) e;
                int codigoHttp = apiException.response.codigoHttp;
                String mensajeError = apiException.response.string("mensajeAlUsuario");
                String cause = apiException.response.string("cause");
                if(codigoHttp == 409 && cause.contains("no se pueden realizar en este horario")) return new Objeto().set("estado", "FUERA_HORARIO");
                if(codigoHttp == 409 && mensajeError.contains("FONDOS INSUFICIENTES")) return armarResponsePagoFondoIns(contexto, apiException);
                return RespuestaTAS.error(contexto, "TASTarjetasController - postPagoTarjetaDebitoCuenta()", apiException);

            }
            return RespuestaTAS.error(contexto, "TASTarjetasController - postPagoTarjetaDebitoCuenta()", e);
        }
    }

    private static Objeto armarDatosPagoTarjeta(ContextoTAS contexto){
        try {
            Objeto datosPago = new Objeto();
            datosPago.set("cuenta",contexto.parametros.string("cuentaId"));
            datosPago.set("cuentaTarjeta",contexto.parametros.string("cuentaTarjeta"));
            datosPago.set("importe",contexto.parametros.bigDecimal("importe"));
            datosPago.set("moneda",contexto.parametros.string("idMoneda"));
            datosPago.set("tipoCuenta",contexto.parametros.string("tipoCuenta"));
            datosPago.set("tipoTarjeta",contexto.parametros.string("tipoTarjeta"));
            datosPago.set("numeroTarjeta",contexto.parametros.string("numeroTarjeta"));
            datosPago.set("estado", "OK");
            return datosPago;
        }catch (Exception e){
            return new Objeto().set("estado", TASMensajesString.ERROR.getTipoMensaje());
        }
    }

    private static Objeto armarResponsePagoFondoIns(ContextoTAS contexto, ApiException apiException){
        String tipoCta = contexto.parametros.string("tipoCuenta");
        if(tipoCta.equals("AHO")){
            return RespuestaTAS.error(contexto,"FONDOS_INSUFICIENTES",TASMensajesString.TARJETAS_ERROR_FONDOS_INS_CA.getTipoMensaje() ,"Caja de ahorros con fondos insuficientes");
        } else {
            return RespuestaTAS.error(contexto,"FONDOS_INSUFICIENTES", TASMensajesString.TARJETAS_ERROR_FONDOS_INS_CC.getTipoMensaje(),"Cuenta corriente con fondos insuficientes");
        }
    }

    //TODO: terminar servicio de programar pago...
    public static Objeto postProgramarPagoTarjeta(ContextoTAS contexto){
        String idCliente = contexto.parametros.string("idCliente");
        TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
        // ? comentar las proximas 2 lineas para no validar el cliente en sesion
        if (clienteSesion == null)
            return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
        if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
            return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");

        Objeto datosPago = armarDatosPagoTarjeta(contexto);
        if (datosPago.string("estado").equals("ERROR"))
            return RespuestaTAS.sinParametros(contexto, "uno o mas parametros incorrectos");

        Objeto responseScheduler = TASSchedulerController.programarPagoTarjeta(contexto, datosPago);
        if(responseScheduler.string("estado").equals("ERROR_PARAMETROS")) return RespuestaTAS.sinParametros(contexto,"uno o mas parametros incorrectos");
        return  null;
    }

    public static Objeto getEstadosTarjetaDebito(ContextoTAS contexto){
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            Objeto tarjetasDebito = contexto.parametros.objeto("tarjetasDebito", new Objeto());
            if (tarjetasDebito.toList().size() < 1) return RespuestaTAS.sinParametros(contexto, "sin tarjetas para consultar");
            Objeto consultaTD = UtilesTarjetas.consultarEstadosTD(contexto, tarjetasDebito);
            if(consultaTD.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "UtilesTarjetas - consultarEstadosTD()", (Exception) consultaTD.get("error"));
            return armarResponseEstadosTD(contexto, consultaTD);
        }catch (Exception e){
            return  RespuestaTAS.error(contexto, "TASTarjetasController - getEstadosTarjetaDebito()", e);
        }
    }

    private static Objeto armarResponseEstadosTD(ContextoTAS contexto, Objeto consultaTD){
        Objeto response = new Objeto();
        boolean hayResultados = false;
        for(Objeto td : consultaTD.objetos()){
            if(td.string("estadoTarjeta").equals("ERROR") ){
                response.add(UtilesTarjetas.armarResponseEstadoErrorTD(td));
            }else{
                hayResultados = true;
                response.add(UtilesTarjetas.armarResponseEstadosTD(td));
            }
        }
        return hayResultados ? response : RespuestaTAS.sinResultados(contexto, "El estado de todas las TD arrojó error.");
    }

    public static Objeto postBlanqueoTD(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String numeroTarjeta = contexto.parametros.string("numeroTarjeta");
            String tipoBlanqueo = contexto.parametros.string("tipoBlanqueo");
            if(numeroTarjeta.isEmpty() || tipoBlanqueo.isEmpty() ) return RespuestaTAS.sinParametros(contexto, "uno o mas parametros no encontrados");
            String digitoVerificador = contexto.parametros.string("digitoVerificador","0");
            String numeroMiembro = contexto.parametros.string("numeroMiembro","0");
            String numeroVersion = contexto.parametros.string("numeroVersion","0");
            Objeto td = new Objeto();
            td.set("numeroTarjetaDebito", numeroTarjeta);
            Objeto blanqueoResponse = new Objeto();
            LogTAS.evento(contexto, "INICIO_BLANQUEO_TD", td);
            if(!tipoBlanqueo.equals("hab")) {
                blanqueoResponse = tipoBlanqueo.equals("pil") ?
                        TASRestTarjetas.deleteBlanquearPIL(contexto, numeroTarjeta, tipoBlanqueo, digitoVerificador, numeroMiembro, numeroVersion)
                        : TASRestTarjetas.deleteBlanquearPIN(contexto, numeroTarjeta, tipoBlanqueo, digitoVerificador, numeroMiembro, numeroVersion);
            }else{
                blanqueoResponse = TASRestTarjetas.patchHabilitarTD(contexto, numeroTarjeta);
                String tipoTarjeta = UtilesTarjetas.getTipoTarjetaFromDetalle(contexto, numeroTarjeta);
                blanqueoResponse.set("tipoTD", tipoTarjeta);
            }
            if(blanqueoResponse.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "ERROR_API", TASMensajesString.TARJETAS_DEBITO_HAB_PIL_PIN_ERROR.getTipoMensaje(), "error al blanquear tarjeta");
            LogTAS.evento(contexto, "FIN_BLANQUEO_TD", blanqueoResponse);
            int codigo = blanqueoResponse.integer("codigo");
            return codigo != 204 ? armarResponseBlanqueo(blanqueoResponse, tipoBlanqueo) : RespuestaTAS.sinResultados(contexto, "no existe resultados para ese nro de tarjeta") ;
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASTarjetasController - postBlanqueoTD", e);
        }
    }
    private static Objeto armarResponseBlanqueo(Objeto blanqueoResponse, String tipoBlanqueo){
        Objeto response = new Objeto();
        if(tipoBlanqueo.equals("hab")){
            response.set("resultado", true);
            response.set("tipoTD", blanqueoResponse.string("tipoTD").trim());
            return response;
        }else{
            return response.set("resultado", true);
        }
    }
    public static Objeto getTarjetaCreditoTerceros(ContextoTAS contexto){
        try {
            String num = contexto.parametros.string("numero");            
            if (num.isEmpty()) return RespuestaTAS.sinParametros(contexto, "Uno o mas parametros no ingresados");
            Objeto response;
            Boolean flagNumeroCuenta = contexto.parametros.bool("flagNum");
            if (flagNumeroCuenta) response = consultaTitularidadCuentaPrincipal(contexto, num);
            else 
            	{ response = TASRestTarjetas.getTarjetaCredito(contexto, num);
            	  if (response!=null) {
            		  Objeto prodCli = UtilesProductos.getTitularesProducto(contexto, num, "ATC");
            		  Objeto cli = prodCli.objeto("estado").objetos().get(0);
            		  response.set("codigoCliente", cli.string("clienteID"));
            		  response.set("nombre", cli.string("nombre"));
            		  response.set("apellido", cli.string("primerApellido"));
            		  Objeto cuentas = TASRestCuenta.getCuentas(contexto, cli.string("clienteID"), false, false, false);
            		  String bancaCuentaNum = cuentaAsociada(response.string("bancaCuentaNumero"), cuentas);
            		  response.set("cuentaNumero", bancaCuentaNum);
            			Objeto tarjetas = TASRestTarjetas.getTarjetasCredito(contexto, cli.string("clienteID"));
            			for(Objeto tarjeta : tarjetas.objetos()){
            		        String tarj = tarjeta.string("numero");
            		        if (tarj.equals(num)) {
            	           		  response.set("tipoTitularidad",tarjeta.string("tipoTitularidad"));
            		        }          		 
            		     }
            	  }
            	}
            return response == null ?  RespuestaTAS.sinResultados(contexto, "no existe tarjeta para ese ID") : response;
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASTarjetasController - getTarjetaCredito()", e);
        }
    }
	private static String cuentaAsociada(String num, Objeto cuentas) {
		for (Objeto cuenta: cuentas.objetos()) {
			if (cuenta.string("numeroProducto").contains(UtilesCuenta.sacarCeros(num))){
				return cuenta.string("numeroProducto");
			}
		}
		return null;
		
	}
	private static Objeto consultaTitularidadCuentaPrincipal(ContextoTAS contexto, String num) {
		Objeto responseTitularidad = TASRestTarjetas.consultaTitularidadCuentaPrincipal(contexto, num);
        Objeto response;
		String codigoCliente = responseTitularidad.string("codigoCliente");
		Objeto tarjetas = TASRestTarjetas.getTarjetasCredito(contexto, codigoCliente);
		for(Objeto tarjeta : tarjetas.objetos()){
	        String cuenta = tarjeta.string("cuenta");
	        if (cuenta.equals(num)) {
	        	String numTarjeta = tarjeta.string("numero");
	        	 response = TASRestTarjetas.getTarjetaCredito(contexto, numTarjeta);
	        	 if (response!=null) {
           		  Objeto prodCli = UtilesProductos.getTitularesProducto(contexto, numTarjeta, "ATC");
           		  Objeto cli = prodCli.objeto("estado").objetos().get(0);
           		  response.set("codigoCliente", cli.string("clienteID"));
           		  response.set("nombre", cli.string("nombre"));
           		  response.set("apellido", cli.string("primerApellido"));
           		  response.set("tipoTitularidad",tarjeta.string("tipoTitularidad"));
           		 Objeto cuentas = TASRestCuenta.getCuentas(contexto, cli.string("clienteID"), false, false, false);
           		 String bancaCuentaNum = cuentaAsociada(response.string("bancaCuentaNumero"), cuentas);
           		 response.set("cuentaNumero", bancaCuentaNum);

           	  }
	             return response == null ?  RespuestaTAS.sinResultados(contexto, "no existe tarjeta para ese ID") : response;
	        }
	}
		return RespuestaTAS.sinResultados(contexto, "no existe tarjeta para ese ID");
	}
}
