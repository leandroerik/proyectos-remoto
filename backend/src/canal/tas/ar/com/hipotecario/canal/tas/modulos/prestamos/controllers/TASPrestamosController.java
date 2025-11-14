package ar.com.hipotecario.canal.tas.modulos.prestamos.controllers;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.prestamos.services.TASRestPrestamos;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.productos.utiles.UtilesProductos;
import ar.com.hipotecario.canal.tas.shared.utils.models.strings.TASMensajesString;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TASPrestamosController {

    public static Objeto getPrestamo(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String numeroPrestamo = contexto.parametros.string("numeroPrestamo");
            boolean detalle = contexto.parametros.bool("detalle", true);
            if(numeroPrestamo.isEmpty()) return RespuestaTAS.sinParametros(contexto,"uno o mas parametros incompletos");
            Objeto responsePrestamo = TASRestPrestamos.getPrestamoByNro(contexto, numeroPrestamo, detalle);
            if(responsePrestamo.isEmpty()) return RespuestaTAS.sinResultados(contexto, "no existen prestamos para ese nro");
            String estado = responsePrestamo.string("estado");
            Objeto responseCuotas = new Objeto();
            if(estado.equals("NORMAL") || estado.equals("V") || estado.equals("R")){
                Date date = new Date();
                Fecha fechaHoy = new Fecha(date);
                int mes = fechaHoy.mes();
                int año = fechaHoy.año();
                Calendar calendario = Calendar.getInstance();
                calendario.set(Calendar.YEAR, año);
                calendario.set(Calendar.MONTH, mes - 1);
                Fecha fechaDesde = fechaHoy.restarDias(fechaHoy.dia() + 1);
                Fecha fechaHasta = fechaHoy.sumarDias(calendario.getActualMaximum(Calendar.DAY_OF_MONTH) - fechaHoy.dia());
                responseCuotas = TASRestPrestamos.getCuotasPrestamo(contexto, numeroPrestamo, fechaDesde.string("yyyy-MM-dd"), fechaHasta.string("yyyy-MM-dd"));

            }
            Objeto response = new Objeto();
            response.set("prestamo", responsePrestamo);
            if(responseCuotas.string("estado").equals("OK")) {
                response.set("cuota", armarResponseCuotas(responseCuotas));
                return response;
            }
            return response;
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASPrestamosController - getPrestamo()", e);
        }
    }

    private static Objeto armarResponseCuotas(Objeto responseCuotas){
        Objeto cuota = responseCuotas.objeto("cuota").objetos().get(0);
        Objeto response = new Objeto();
        response.set("numero", cuota.string("numero"));
        response.set("fechaVencimiento", cuota.string("fechaVencimiento"));
        response.set("saldo", cuota.string("saldoCapital"));
        response.set("importeTotal", cuota.string("total"));
        response.set("estado", cuota.string("estado"));
        return response;
    }
    public static Objeto getResumenPrestamo(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String numeroPrestamo = contexto.parametros.string("numeroPrestamo");
            boolean leyenda = contexto.parametros.bool("leyenda", true);
            Objeto resumenResponse = TASRestPrestamos.getResumenPrestamo(contexto, numeroPrestamo, leyenda);
            if(resumenResponse.isEmpty()) return RespuestaTAS.sinResultados(contexto, "no existe resumen para ese nro prestamo");
            return  armarResponseResumenPrestamo(resumenResponse);
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASPrestamoController - getResumenPrestamo()", e);
        }
    }

    private static Objeto armarResponseResumenPrestamo(Objeto resumenResponse){
        Objeto response = new Objeto();
        response.set("nombre", resumenResponse.string("nombre"));
        response.set("apellido", resumenResponse.string("apellido"));
        response.set("numeroPrestamo", resumenResponse.string("idPrestamo"));
        response.set("numeroHipoteca", resumenResponse.string("numeroHipoteca",""));
        String resumenCuenta = resumenResponse.string("periodoMes") + " de " + resumenResponse.string("periodoAnio");
        response.set("resumenDeCuenta",resumenCuenta);
        response.set("anioResumen", resumenResponse.string("periodoAnio"));
        response.set("mesResumen", resumenResponse.string("periodoMes"));
        response.set("saldoDeuda", String.valueOf(resumenResponse.bigDecimal("saldoRestante")));
        response.set("fechaVencimiento", resumenResponse.string("vencimiento"));
        response.set("cuotaNumero", "N-"+ resumenResponse.string("cuotaNumero"));
        response.set("fechaProximoVencimiento", resumenResponse.string("proximoVencimiento"));
        response.set("totalDeudaExigible", String.valueOf(resumenResponse.string("totalDeudaExigible")));
        response.set("symbolMoneda", resumenResponse.string("simboloMoneda"));
        response.set("cuotasRestantes", resumenResponse.string("cuotasRestantes"));
        Objeto pagosIngresados = new Objeto();
        List<Objeto> pagosIngresadosList = new ArrayList<>();
        pagosIngresados.set("descripcion", resumenResponse.string("descripcionPago"));
        String montoPagoIngresado = resumenResponse.string("simboloMoneda") + " " + resumenResponse.bigDecimal("importePago");
        pagosIngresados.set("monto", montoPagoIngresado);
        pagosIngresadosList.add(pagosIngresados);
        response.set("pagosIngresados", pagosIngresadosList);
        Objeto debitosCreditos = new Objeto();
        List<Objeto> debitosCreditosList = new ArrayList<>();
        debitosCreditos.set("descripcion", resumenResponse.string("decripcionDeuda"));
        String montoDeuda = resumenResponse.string("simboloMoneda") + " " + resumenResponse.bigDecimal("importeDeuda");
        debitosCreditos.set("monto", montoDeuda);
        debitosCreditosList.add(debitosCreditos);
        response.set("debitosCreditos", debitosCreditosList);
        List<Objeto> conceptosList = new ArrayList<>();
        for(Objeto concepto : resumenResponse.objeto("conceptos").objetos() ){
            conceptosList.add(concepto);
        }
        response.set("cuotaActual", conceptosList);
        return response;
    }

    public static Objeto postPagoPrestamoDebitoCuenta(ContextoTAS contexto){
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            Objeto datosPago = armarDatosPago(contexto);
            if (datosPago.string("estado").equals("ERROR"))
                return RespuestaTAS.sinParametros(contexto, "uno o mas parametros incorrectos");
            LogTAS.evento(contexto, "INICIO_PAGO_PRESTAMO_DEBITO", datosPago);
            Objeto responsePago = TASRestPrestamos.postPagoPrestamoDebitoCuenta(contexto, datosPago);
            LogTAS.evento(contexto, "FIN_PAGO_PRESTAMO_DEBITO", responsePago);
            return responsePago;
        }catch (Exception e) {
            if (e instanceof ApiException) {
                ApiException apiException = (ApiException) e;
                int codigoHttp = apiException.response.codigoHttp;
                String codigoError = apiException.response.string("codigo");
                String cause = apiException.response.string("cause");
                if(codigoHttp == 409 && cause.contains("no se pueden realizar en este horario")) return new Objeto().set("estado", "FUERA_HORARIO");
                if(codigoHttp == 409 && codigoError.equals("701124")) return RespuestaTAS.error(contexto, "FONDOS_INSUFICIENTES", TASMensajesString.PRESTAMOS_FONDOS_INSUFICIENTES.getTipoMensaje(), "fondos insuficientes para el pago del prestamo");
                return RespuestaTAS.error(contexto, "TASPrestamosController - postPagoPrestamosDebitoCuenta()", apiException);
            }
            return RespuestaTAS.error(contexto, "TASPrestamosController - postPagoPrestamosDebitoCuenta()", e);
        }
    }

    private static Objeto armarDatosPago(ContextoTAS contexto){
        try{
            Objeto datosPago = new Objeto();
            datosPago.set("cuenta", contexto.parametros.string("cuentaId"));
            datosPago.set("importe", contexto.parametros.bigDecimal("importe"));
            datosPago.set("tipoCuenta", contexto.parametros.string("tipoCuenta"));
            datosPago.set("tipoPrestamo", contexto.parametros.string("tipoPrestamo"));
            datosPago.set("numeroPrestamo", contexto.parametros.string("numeroPrestamo"));
            datosPago.set("moneda", contexto.parametros.string("idMoneda"));
            datosPago.set("idProducto", contexto.parametros.string("idProducto"));
            datosPago.set("reverso", contexto.parametros.bool("reverso", false));
            datosPago.set("estado", "OK");
            return datosPago;
        }catch (Exception e){
            return new Objeto().set("estado", TASMensajesString.ERROR.getTipoMensaje());
        }
    }
    
    public static Objeto getPrestamoTerceros(ContextoTAS contexto){
        try {
            String num = contexto.parametros.string("numero");            
            if (num.isEmpty()) return RespuestaTAS.sinParametros(contexto, "Uno o mas parametros no ingresados");
            Objeto response;
            	response = TASRestPrestamos.getPrestamoByNro(contexto, num, true);
                if(response.isEmpty()) return RespuestaTAS.sinResultados(contexto, "no existen prestamos para ese nro");
            	  if (response!=null) {
            		  Objeto prodCli = UtilesProductos.getTitularesProducto(contexto, num, "CCA");
            		  Objeto cli = prodCli.objeto("estado").objetos().get(0);
            		  response.set("codigoCliente", cli.string("clienteID"));            		  
            	  }
            	
            return response;
            
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASTarjetasController - getTarjetaCredito()", e);
        }
    }

    //TODO: armar logica para ProgramarPagoPrestamo en API-Scheduler
}
