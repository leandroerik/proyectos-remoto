package ar.com.hipotecario.canal.tas.modulos.auditoria.utils;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.TASAuditoria;
import ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.TASAuditoriaAdminContingencia;
import ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.reversas.*;
import ar.com.hipotecario.canal.tas.modulos.auditoria.services.sql.TASSqlAuditoria;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.models.TasDepositoEfectivo;
import ar.com.hipotecario.canal.tas.modulos.inicio.login.servicios.sql.TASSqlUsuariosAdministradores;
import ar.com.hipotecario.canal.tas.modulos.prestamos.pagos.models.TasPagoPrestamoEfectivo;
import ar.com.hipotecario.canal.tas.modulos.tarjetas.pagos.models.TasPagoTarjetaEfectivo;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.service.TasRestDepositos;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKiosco;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class UtilesAuditoria {

    public static boolean verificarAdministrador(ContextoTAS contexto, TASClientePersona cliente, TASKiosco kiosco){
        String docAdm = cliente.getNumeroDocumento();
        int sucursalId = kiosco.getSucursalId();
        LogTAS.evento(contexto, "VERIFICAR_ADMINISTRADOR_SQL", new Objeto().set("nroDoc", docAdm));
        Objeto usuarioAdmin = TASSqlUsuariosAdministradores.obtenerAdministrador(contexto,docAdm, sucursalId);
        boolean existeUsuario = !usuarioAdmin.string("estado").equals("SIN_RESULTADOS")
                && !usuarioAdmin.string("estado").equals("ERROR");
        return existeUsuario;
    }

    public static Objeto getLoteActual(ContextoTAS contexto, TASKiosco kiosco){
        LogTAS.evento(contexto, "OBTENER_LOTE_ACTUAL", new Objeto().set("idKiosco", kiosco.getKioscoId()));
        Objeto lote = TASSqlAuditoria.getLoteActual(contexto, kiosco.getKioscoId().toString(), kiosco.getSucursalId());
        if(lote.string("estado").equals("ERROR")) return lote;
        Objeto loteActual = new Objeto();
        loteActual.set("estado", "OK");
        loteActual.set("nroLote",lote.objeto("respuesta").objetos().get(0).string("nroLote"));
        return loteActual;
    }

    public static TASAuditoria getDatosCierre(ContextoTAS contexto, String idTAS){
        TASAuditoria ultAuditoria = new TASAuditoria();
        try{
            LogTAS.evento(contexto, "OBTENER_DATOS_CIERRE", new Objeto().set("idKiosco", idTAS));
            Objeto datosCierre = TASSqlAuditoria.getDatosCierre(contexto, idTAS);
            if( datosCierre.objetos().size() > 0) {
                ultAuditoria.setKioscoId(idTAS);
                ultAuditoria.setLote(datosCierre.objetos().get(0).string("Lote"));
                ultAuditoria.setPrecinto1(datosCierre.objetos().get(0).string("Precinto1"));
                ultAuditoria.setPrecinto2(datosCierre.objetos().get(0).string("Precinto2"));
                long fecha = Long.parseLong(datosCierre.objetos().get(0).string("FechaUltimaAuditoria"));
                ultAuditoria.setFechaUltimaAuditoria(new Fecha(new Date(fecha)));
                ultAuditoria.setTipoCierre(datosCierre.objetos().get(0).string("TipoCierre"));
                return ultAuditoria;
            }else{
                return ultAuditoria;
            }
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            ultAuditoria.setError(error);
            return ultAuditoria;
        }
    }

    public static Fecha getFechaUltimaAuditoria(ContextoTAS contexto, TASKiosco kiosco, String tipoCierre, String precinto1, String precinto2, String loteActual){
        LogTAS.evento(contexto, "OBTENER_ULTIMA_AUDITORIA");
        Objeto responseFechaAuditoria = TASSqlAuditoria.getFechaUltimaAuditoriaByIpPrecintos(contexto, kiosco, tipoCierre, precinto1, precinto2, loteActual);
        if(responseFechaAuditoria.string("estado").equals("ERROR")) return null;
        String fecha = responseFechaAuditoria.string("fechaUltimaAuditoria");
        long dateLong = Long.parseLong(fecha);
        Date fechaDate = new Date(dateLong);
        Fecha responseFecha = new Fecha(fechaDate);
        return responseFecha;
    }

    public static Fecha getFechaUltimaAuditoria(ContextoTAS contexto, TASKiosco kiosco, String tipoCierre){
        LogTAS.evento(contexto, "OBTENER_ULTIMA_AUDITORIA");
        Objeto responseFechaAuditoria = TASSqlAuditoria.getFechaUltimaAuditoriaByIdTipoCierre(contexto, kiosco, tipoCierre);
        if(responseFechaAuditoria.string("estado").equals("ERROR")) return null;
        String fecha = "";
        if(responseFechaAuditoria.objetos().size() > 1){
            for(Objeto response : responseFechaAuditoria.objetos()){
                if(tipoCierre.equals(response.string("tipoCierre"))){
                    fecha = response.string("FechaUltimaAuditoria");
                }
            }
        }else{
            fecha = responseFechaAuditoria.objetos().get(0).string("FechaUltimaAuditoria");
        }
        long dateLong = Long.parseLong(fecha);
        Date fechaDate = new Date(dateLong);
        Fecha responseFecha = new Fecha(fechaDate);
        return responseFecha;
    }

    public static Fecha getFechaUltimaAuditoriaVuelco(ContextoTAS contexto, TASKiosco kiosco){
        LogTAS.evento(contexto, "OBTENER_ULTIMA_AUDITORIA_VUELCOS");
        Objeto responseFechaVuelco = TASSqlAuditoria.getFechaUltimaAuditoriaVuelco(contexto, kiosco);
        if(responseFechaVuelco.string("estado").equals("ERROR") || responseFechaVuelco.string("estado").equals("SIN_RESULTADOS")) return null;
        Objeto rta = responseFechaVuelco.objeto("respuesta");
        if(rta.objetos().size() > 0){
             String fecha = rta.objetos().get(0).string("FechaUltimaAuditoria");
             long dateLong = Long.parseLong(fecha);
             Date fechaDate = new Date(dateLong);
             Fecha responseFecha = new Fecha(fechaDate);
        return responseFecha;
        } else {
            return null;
        }
    }

    public static int getCantBilletesRetenidos(ContextoTAS contexto, TASKiosco kiosco, Date fechaAuditoria, Date fechaUltimaAuditoria){
            Objeto responseCantBilletes = TASSqlAuditoria.getCantBilletesRetenidos(contexto, kiosco.getKioscoId(), fechaAuditoria, fechaUltimaAuditoria);
            if(responseCantBilletes.string("estado").equals("ERROR")) return 0;
            Objeto response = responseCantBilletes.objeto("respuesta");
            int cantBilletes = response.objetos().get(0).integer("cantidadRetenidos") != null ? response.objetos().get(0).integer("cantidadRetenidos") : 0;
            return cantBilletes;
    }
    public static int calcularCantTotales(Objeto totalesPesos, Objeto totalesDolares){
        Objeto responsePesos = totalesPesos.objeto("respuesta");
        Objeto responseDolares = totalesDolares.objeto("respuesta");
        int cantidadPesos = responsePesos.objetos().get(0).integer("cantidad") != null ? responsePesos.objetos().get(0).integer("cantidad") : 0;
        int cantidadDolares = responseDolares.objetos().get(0).integer("cantidad") != null ? responseDolares.objetos().get(0).integer("cantidad") : 0;
        return cantidadPesos + cantidadDolares;
    }

    public static double getTotalDeposito(Objeto totalDeposito){
        Objeto response = totalDeposito.objeto("respuesta");
        double montoTotal = response.objetos().get(0).bigDecimal("importe") != null ? Double.valueOf(response.objetos().get(0).bigDecimal("importe").toString()) : 0.00;
        return montoTotal;
    }

    //todo: ex metodo para operaciones NSR ver si se usa en TASi
    public static Objeto getVuelcoOperacionesPesos(ContextoTAS contexto, TASKiosco kiosco ,Date fechaUltimoCierre, Date fechaCierreActual){
        Objeto vuelcos = new Objeto();
        Objeto montoPesosOK = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"R", "EFECTIVO","PESOS");
        Objeto montoPesosError = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"C", "EFECTIVO","PESOS");
        Objeto montoPesosAbortado = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"P", "EFECTIVO", "PESOS");
        vuelcos.set("cuentaEfectivoOKPesos", montoPesosOK);
        vuelcos.set("cuentaEfectivoErrorPesos",montoPesosError);
        vuelcos.set("cuentaEfectivoErrorPesos",montoPesosAbortado);

        Objeto montoPesosTarjetaOk = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"R", "PAGOTARJETA","PESOS");
        Objeto montoPesosTarjetaError = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"C", "PAGOTARJETA","PESOS");
        Objeto montoPesosTarjetaAbortado = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"P", "PAGOTARJETA","PESOS");
        vuelcos.set("tarjetaPagoOKPesos", montoPesosTarjetaOk);
        vuelcos.set("tarjetaPagoErrorPesos",montoPesosTarjetaError);
        vuelcos.set("tarjetaPagoAbortadaPesos",montoPesosTarjetaAbortado);

        Objeto montoPesosPrestamoOk = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"R", "PAGOPRESTAMO","PESOS");
        Objeto montoPesosPrestamoError = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"C", "PAGOPRESTAMO","PESOS");
        Objeto montoPesosPrestamoAbortado = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"P", "PAGOPRESTAMO","PESOS");
        vuelcos.set("prestamoPagoOKPesos", montoPesosPrestamoOk);
        vuelcos.set("prestamoPagoErrorPesos",montoPesosPrestamoError);
        vuelcos.set("prestamoPagoAbortadaPesos",montoPesosPrestamoAbortado);

        Objeto montoDolaresOk = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"R", "EFECTIVO","DOLARES");
        Objeto montoDolaresError = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"C", "EFECTIVO","DOLARES");
        Objeto montoDolaresAbortado = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"P", "EFECTIVO","DOLARES");
        vuelcos.set("cuentaEfectivoOKDolares", montoDolaresOk);
        vuelcos.set("cuentaEfectivoErrorDolares",montoDolaresError);
        vuelcos.set("cuentaEfectivoAbortadaDolares",montoDolaresAbortado);

        Objeto montoDolaresTarjetaOk = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"R", "PAGOTARJETA","DOLARES");
        Objeto montoDolaresTarjetaError = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"C", "PAGOTARJETA","DOLARES");
        Objeto montoDolaresTarjetaAbortado = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"P", "PAGOTARJETA","DOLARES");
        vuelcos.set("tarjetaPagoOKDolares", montoDolaresTarjetaOk);
        vuelcos.set("tarjetaPagoErrorDolares",montoDolaresTarjetaError);
        vuelcos.set("tarjetaPagoAbortadaDolares",montoDolaresTarjetaAbortado);

        Objeto montoDolaresPrestamoOk = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"R", "PAGOPRESTAMO","DOLARES");
        Objeto montoDolaresPrestamoError = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"C", "PAGOPRESTAMO","DOLARES");
        Objeto montoDolaresPrestamoAbortado = TASSqlAuditoria.getMontosDepositosVuelco(contexto, kiosco ,fechaUltimoCierre, fechaCierreActual,"P", "PAGOPRESTAMO","DOLARES");
        vuelcos.set("prestamoPagoOKDolares", montoDolaresPrestamoOk);
        vuelcos.set("prestamoPagoErrorDolares",montoDolaresPrestamoError);
        vuelcos.set("prestamoPagoAbortadaRolares",montoDolaresPrestamoAbortado);

        return vuelcos;

    }

    public static TASAuditoriaAdminContingencia procesarOperacionesDepositos(ContextoTAS contexto, int kioscoId, String codigoRetorno){
        LogTAS.evento(contexto, "OBTENER_OPERACIONES_PARA_REINTENTO");
        TASAuditoriaAdminContingencia adminContingencia = new TASAuditoriaAdminContingencia();
        List<TASDatosDepositoReversa> datosDepositoReversa = TASSqlAuditoria.getDepositosEfectivoReversa(contexto, kioscoId, codigoRetorno);
        List<TASDatosPagoTarjetaReversa> datosPagoTarjetaReversa = TASSqlAuditoria.getDepositosTarjetaReversa(contexto, kioscoId, codigoRetorno);
        List<TASDatosPagoPrestamo> datosPagoPrestamo = TASSqlAuditoria.getDepositosPrestamo(contexto, kioscoId, codigoRetorno);
        List<TASDatosPagoPrestamoReversa> datosPagoPrestamoReversa = TASSqlAuditoria.getDepositosPrestamoReversa(contexto, kioscoId, codigoRetorno);
        List<TASDatosRegistroOperacion> datosRegistroOperacion = TASSqlAuditoria.getDatosRegistroOperacion(contexto, kioscoId, codigoRetorno);
        adminContingencia.setDepositoReversasAReintentar(datosDepositoReversa);
        adminContingencia.setPagoTarjetaReversasAReintentar(datosPagoTarjetaReversa);
        adminContingencia.setPagosPrestamoAReintentar(datosPagoPrestamo);
        adminContingencia.setPagoPrestamoReversasAReintentar(datosPagoPrestamoReversa);
        adminContingencia.setRegistroOperacionAReintentar(datosRegistroOperacion);
        return adminContingencia;
    }

    public static Objeto generarReversaDepositos(ContextoTAS contexto,TASDatosDepositoReversa depositoAReversar, TASKiosco kiosco){
        try {
            TasDepositoEfectivo deposito = generarDatosDepositos(depositoAReversar);
            String idReversa = depositoAReversar.getIdReversa();
            Objeto responsePatch = TasRestDepositos.patchDepositosEfectivoReversa(contexto, deposito, idReversa);
            Objeto response = new Objeto();
            response.set("estado", "ok");
            response.set("respuesta", responsePatch);
            if(responsePatch instanceof ApiResponse){
                ApiResponse responseAPI = (ApiResponse) responsePatch;
                response.set("codigo", responseAPI.codigoHttp);
             }
            return response;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    private static TasDepositoEfectivo generarDatosDepositos(TASDatosDepositoReversa depositoReversa){
        String cuenta = depositoReversa.getNumeroCuenta();
        Fecha fecha = new Fecha(depositoReversa.getFecha());
        Integer lote = Integer.valueOf(depositoReversa.getLote());
        String moneda = depositoReversa.getCodigoMoneda();
        BigDecimal importe = new BigDecimal(depositoReversa.getImporteTotalEfectivo());
        Integer oficina = Integer.valueOf(depositoReversa.getSucursalId());
        String precinto = depositoReversa.getPrecinto();
        String producto = depositoReversa.getTipoCuenta();
        String tas = depositoReversa.getKioscoId();
        TasDepositoEfectivo deposito = new TasDepositoEfectivo(cuenta, fecha, lote, moneda, importe, oficina, precinto, producto, tas);
        return deposito;
    }

    public static Objeto generarReversaPagoTarjeta(ContextoTAS contexto,TASDatosPagoTarjetaReversa pagoTarjetaAReversar,TASKiosco kiosco) {
        try {
            TasPagoTarjetaEfectivo datosPagoTarjeta = generarDatosPagoTarjeta(pagoTarjetaAReversar);
            String idReversa = pagoTarjetaAReversar.getIdReversa();
            Objeto responsePatch = TasRestDepositos.patchPagoTarjetaEfectivoReversa(contexto, datosPagoTarjeta, idReversa);
            Objeto response = new Objeto();
            response.set("estado", "ok");
            response.set("respuesta", responsePatch);
            if(responsePatch instanceof ApiResponse){
                ApiResponse responseAPI = (ApiResponse) responsePatch;
                response.set("codigo", responseAPI.codigoHttp);
            }
            return response;
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }
    
    private static TasPagoTarjetaEfectivo generarDatosPagoTarjeta(TASDatosPagoTarjetaReversa pagoTarjetaReversa){
        String cuentaTarjeta = pagoTarjetaReversa.getNumeroCuentaTarjeta();        
        Fecha fecha = new Fecha(pagoTarjetaReversa.getFecha());
        String lote = pagoTarjetaReversa.getLote();
        String idMoneda = pagoTarjetaReversa.getCodigoMoneda();
        BigDecimal importe = new BigDecimal(pagoTarjetaReversa.getImporteTotalEfectivo());
        String oficina = pagoTarjetaReversa.getSucursalId();
        String codigoPrecinto = pagoTarjetaReversa.getPrecinto();
        String tas = pagoTarjetaReversa.getKioscoId();
        String tipoProducto = "ATC";
        String tipoTarjeta = pagoTarjetaReversa.getTipoTarjeta();
        TasPagoTarjetaEfectivo pagoTarjeta = new TasPagoTarjetaEfectivo(cuentaTarjeta, fecha, lote, idMoneda, importe, oficina, codigoPrecinto, tas, tipoProducto, tipoTarjeta);
        return pagoTarjeta;
    }
    
    public static Objeto generarPagoPrestamo(ContextoTAS contexto, TASDatosPagoPrestamo datosPagoPrestamo){
       try{
           TasPagoPrestamoEfectivo pagoPrestamo = generarDatosPagoPrestamo(datosPagoPrestamo);
           Objeto responsePost = TasRestDepositos.postPagoPrestamoEfectivo(contexto, pagoPrestamo);
           Objeto response = new Objeto();
           response.set("estado", "ok");
           response.set("respuesta", responsePost);
           if(responsePost instanceof ApiResponse){
               ApiResponse responseAPI = (ApiResponse) responsePost;
               response.set("codigo", responseAPI.codigoHttp);
           }
           return response;
       } catch (Exception e) {
           Objeto error = new Objeto();
           error.set("estado", "ERROR");
           error.set("error", e);
           return error;
       }
    }
    
    private static TasPagoPrestamoEfectivo generarDatosPagoPrestamo(TASDatosPagoPrestamo datosPagoPrestamo){
        String nroPrestamo = datosPagoPrestamo.getNroPrestamo();
        Fecha fecha = new Fecha(datosPagoPrestamo.getFecha());
        String hora = fecha.hora().toString();
        String lote = datosPagoPrestamo.getLote();
        BigDecimal importe = new BigDecimal(datosPagoPrestamo.getImporte());
        String sucursal = datosPagoPrestamo.getSucursalId();
        String precinto = datosPagoPrestamo.getPrecinto();
        String tas = datosPagoPrestamo.getSucursalId();
        TasPagoPrestamoEfectivo pagoPrestamo = new TasPagoPrestamoEfectivo(nroPrestamo, fecha, hora,lote, importe, sucursal, precinto, tas);
        return pagoPrestamo;
    }

    public static Objeto generarReversaPagoPrestamo(ContextoTAS contexto, TASDatosPagoPrestamoReversa pagoPrestamoReversa){
        try{
            TASDatosPagoPrestamo datosPagoPrestamo = pagoPrestamoReversa.toClass(TASDatosPagoPrestamo.class);
            TasPagoPrestamoEfectivo pagoPrestamo = generarDatosPagoPrestamo(datosPagoPrestamo);
            String idReversa = pagoPrestamoReversa.getIdReversa();
            Objeto responsePost = TasRestDepositos.patchPagoPrestamoEfectivoReversa(contexto, pagoPrestamo, idReversa);
            Objeto response = new Objeto();
            response.set("estado", "ok");
            response.set("respuesta", responsePost);
            if(responsePost instanceof ApiResponse){
                ApiResponse responseAPI = (ApiResponse) responsePost;
                response.set("codigo", responseAPI.codigoHttp);
            }
            return response;
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }
}
