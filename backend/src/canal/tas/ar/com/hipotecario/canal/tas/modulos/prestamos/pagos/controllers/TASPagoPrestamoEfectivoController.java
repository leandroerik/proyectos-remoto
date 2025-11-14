package ar.com.hipotecario.canal.tas.modulos.prestamos.pagos.controllers;

import java.math.BigDecimal;
import java.util.Map;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.service.TasSqlDepositoEfectivo;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.utils.UtilesDepositos;
import ar.com.hipotecario.canal.tas.modulos.prestamos.pagos.models.DepositosDestinoPrestamo;
import ar.com.hipotecario.canal.tas.modulos.prestamos.pagos.models.TasPagoPrestamoEfectivo;
import ar.com.hipotecario.canal.tas.modulos.prestamos.pagos.services.TasRestPagoPrestamoEfectivo;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.auditor.controllers.TASApiAuditor;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.models.DepositoCuentaDTO;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.models.PagoPrestamoDTO;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKiosco;
import ar.com.hipotecario.canal.tas.shared.utils.models.enums.TASEstadoDepositoEnum;
import ar.com.hipotecario.canal.tas.shared.utils.models.strings.TASMensajesString;
//import ar.gabrielsuarez.glib.G;

public class TASPagoPrestamoEfectivoController {

    // 1. inicia deposito
    public static Objeto postInicializarPago(ContextoTAS contexto) {

        LogTAS.evento(contexto, "INICIALIZANDO_PAGO");

        String codigoCliente = contexto.parametros.string("codigoCliente", "");

        String valores = contexto.parametros.string("valores", "");
        String kioscoId = contexto.parametros.string("kioscoId", "0");
        String codigoMoneda = contexto.parametros.string("codigoMoneda", "");
        int numeroSobre = contexto.parametros.integer("numeroSobre", 0);
        String tipoDeposito = contexto.parametros.string("tipoDeposito",
                TASMensajesString.DESTINO_PRESTAMO.toString());
        String codigoBanca = contexto.parametros.string("codigoBanca", "");
        String descripcionBanca = contexto.parametros.string("descripcionBanca", "");
        String numeroPrestamo = contexto.parametros.string("numeroPrestamo", "");
        String tipoCliente = contexto.parametros.string("tipoCliente", "");
        String tipoPrestamo = contexto.parametros.string("tipoPrestamo", "");
        String productoId = contexto.parametros.string("productoId", "");
        String tipoDocumento = contexto.parametros.string("tipoDocumento", "");
        String numeroDocumento = contexto.parametros.string("numeroDocumento", "");
        String numeroOperacionCanal = contexto.parametros.string("numeroOperacionCanal", "");

        if (kioscoId == "0" || codigoMoneda == "" || tipoDeposito == ""
                || numeroPrestamo == "" 
                 || codigoCliente == ""
                ) {
            return RespuestaTAS.sinParametros(contexto, "Uno o más campos son null o están vacíos");
        }

        // llamar a BD [TASKioscosAuditorias]
        Objeto datosTas;
        try {
            datosTas = TasSqlDepositoEfectivo.getPrecintosLotes(contexto, kioscoId);
        } catch (Exception e) {
            return RespuestaTAS.error(
                    contexto,
                    "TASPagoPrestamoEfectivoController - postInicializarPago()  - TasSqlDepositoEfectivo.getPrecintosLotes", e);
        }

        // Datos OK
        TASKiosco kiosco = contexto.getKioscos().get(Integer.parseInt(kioscoId));
        String precintos = datosTas.string("Precinto1") + "-" + datosTas.string("Precinto2");
        String processId = UtilesDepositos.getNumeroTicket(kioscoId);
        // String numeroTicket = tad.save(deposito);

        PagoPrestamoDTO deposito = new PagoPrestamoDTO();
        deposito.setTasOriginaria(kiosco.getKioscoId());
        deposito.setNumeroSobre(numeroSobre);
        deposito.setNumeroOperacionCanal(numeroOperacionCanal);
        deposito.setSucursal(kiosco.getSucursalId());
        deposito.setTipoDocumento(tipoDocumento);
        deposito.setNumeroDocumento(numeroDocumento);
        deposito.setTipoDestino(tipoDeposito);
        deposito.setDestinoDescripcion("Prestamo " +numeroPrestamo);
        deposito.setMoneda(codigoMoneda);
        deposito.setNumeroCuentaDestino(numeroPrestamo);
        deposito.setCodigoCliente(codigoCliente);
        deposito.setTipoCliente(tipoCliente);
        deposito.setCodigoBanca(codigoBanca);
        deposito.setDescripcionBanca(descripcionBanca);
        deposito.setProcessId(processId);
        deposito.setIdProductoCuenta(productoId);
        deposito.setTipoPrestamo(tipoPrestamo);
        deposito.setNumeroPrestamo(numeroPrestamo);
        deposito.setEsInteligente(true);
        deposito.setLote(datosTas.integer("Lote"));
        deposito.setPrecinto(precintos);
        deposito.setValores(valores);

        Map<String, Object> responseSp = TasSqlDepositoEfectivo.saveOrdenDeposito(contexto, deposito);
        if(responseSp.isEmpty() || responseSp == null) return RespuestaTAS.error(contexto, " no se pudo grabar el deposito");
        Long depositoId = Long.decode(responseSp.get("DepositoId").toString());
        String numeroTicket = responseSp.get("NumeroTicket").toString();
        // Inseertar en la otra tabla
        DepositosDestinoPrestamo depo = new DepositosDestinoPrestamo(
                kiosco.getKioscoId(),
                depositoId,
                codigoCliente,
                tipoCliente,
                numeroPrestamo,
                tipoPrestamo,
                productoId
                );

        boolean resultsInsertTASKioscosDepositosDestinoCuenta;

        try {
            resultsInsertTASKioscosDepositosDestinoCuenta = TasSqlDepositoEfectivo
                    .saveTASKioscosDepositosDestinoPrestamo(contexto, depo);
        } catch (Exception e) {
            return RespuestaTAS.error(
                    contexto,
                    "TASPagoPrestamoEfectivoController - postInicializarPago() - TasSqlDepositoEfectivo.saveTASKioscosDepositosDestinoPrestamo",
                    e);
        }

        // si falla el ultimo insert, elimino el registro de la tabla del SP
        if (!resultsInsertTASKioscosDepositosDestinoCuenta) {
            try {

                boolean isDeletedOrdenDeposito = TasSqlDepositoEfectivo.deleteOrdenDeposito(contexto, depositoId,
                        numeroTicket);
                String msgError = isDeletedOrdenDeposito
                        ? "Fallo insert en [TASKioscosDepositosDestinoTarjeta]. Se elimino registro [TASKioscosDepositos]"
                        : "Fallo insert en [TASKioscosDepositosDestinoTarjeta]. Fallo borrado del registro [TASKioscosDepositos]";
                return RespuestaTAS.error(contexto, msgError);
            } catch (Exception e) {
                return RespuestaTAS.error(
                        contexto,
                        "TASDepositosController - postInicializarPago() - TasSqlDepositoEfectivo.deleteOrdenDeposito",
                        e);
            }
        }

        // Grabar en Api Auditor el numeroTicket
        Objeto reporte = TASApiAuditor.generarReporteGeneraTicket(contexto, kiosco, numeroTicket);
        if(!reporte.string("estado").equals("OK")) LogTAS.error(contexto, (Exception) reporte.get("error"));
        LogTAS.evento(contexto, "FINALIZO_PAGO");
        responseSp.put("processId", processId);
        return Objeto.fromMap(responseSp);
    }

    public static Objeto postPagoPrestamoEfectivo(ContextoTAS contexto) {
        Objeto datosError = new Objeto();
        try {
            String numeroTicket = contexto.parametros.string("numeroTicket", "");
            String tas = contexto.parametros.string("tas", "");
            String idCliente = contexto.parametros.string("idCliente", "");
            String nroPrestamo = contexto.parametros.string("nroPrestamo", "");

            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();

            if (numeroTicket == "" || tas == "" || idCliente == "") {
                return RespuestaTAS.sinParametros(contexto, "Uno o más campos son null o están vacíos");
            }

            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");

            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");

            // Recuperar data del depo por el numero de tkt
            DepositoCuentaDTO depositoRecuperado = TasSqlDepositoEfectivo.getDepositoByNumeroTicket(contexto,
                    numeroTicket);
            datosError.set("depositoDTO", depositoRecuperado);
            Fecha fecha = depositoRecuperado.getFechaDeposito();
            String hora = depositoRecuperado.getFechaDeposito().string(
                    "HH:mm:ss");
            String lote = String.valueOf(depositoRecuperado.getLote());
            String moneda = depositoRecuperado.getCodigoMoneda();

            BigDecimal monto = depositoRecuperado.getImporteTotalEfectivo();
            String oficina = String.valueOf(depositoRecuperado.getSucursalId());
            String precinto = depositoRecuperado.getPrecinto();

            TasPagoPrestamoEfectivo deposito = new TasPagoPrestamoEfectivo(nroPrestamo, fecha, hora, lote, monto, oficina,
                    precinto, tas);

            LogTAS.evento(contexto, "INICIO_PAGO_PRESTAMO_EFECTIVO", deposito.objeto());
            Objeto responsePagoTarjetaEfectivo = TasRestPagoPrestamoEfectivo.postPagoPrestamoEfectivo(contexto, deposito);
            Objeto datosActualizar = armarDatosDepositoPositivo(depositoRecuperado, responsePagoTarjetaEfectivo);
            Objeto estadoActualizado = UtilesDepositos.actualizarEstadoDeposito(contexto, datosActualizar, TASEstadoDepositoEnum.OK.getEstadoError());
            if(estadoActualizado.string("estado").contains("ERROR")) LogTAS.error(contexto, (SqlException) estadoActualizado.get("error"));
            Objeto response = armarResponseDeposito(responsePagoTarjetaEfectivo, depositoRecuperado);
            LogTAS.evento(contexto, "FIN_PAGO_PRESTAMO_EFECTIVO", response);
            return response;
        } catch (Exception e) {
            // todo hay q pinchar el error dentro de la excepcion
            if(e instanceof ApiException){
                ApiException apiException = (ApiException) e;
                int httpCode = apiException.response.codigoHttp;
                DepositoCuentaDTO depositoCuentaDTO = (DepositoCuentaDTO) datosError.get("depositoDTO");                
                boolean esErrorMw = httpCode == 500 || httpCode == 502 || httpCode == 503 || httpCode == 504;
                if(httpCode != 200 && !esErrorMw) {
                    Objeto datos = armarDatosError(depositoCuentaDTO);
                    Objeto datosActualizados = UtilesDepositos.actualizarEstadoDeposito(contexto, datos, TASEstadoDepositoEnum.ERROR_FUNCIONAL.getEstadoError());
                    Objeto resp = new Objeto();
                    resp.set("estadoServer", "ERROR_FUNCIONAL");
                    resp.set("devolverBilletes", "1");
                    if(datosActualizados.string("estado").contains("ERROR")) LogTAS.error(contexto, (SqlException) datosActualizados.get("error"));
                    return resp;
                }
                if(httpCode != 200 && esErrorMw){
                    Objeto resp = new Objeto();
                    resp.set("estadoServer", "REVERSADO");
                    resp.set("devolverBilletes", "1");
                    patchPagoPrestamoEfectivoReversa(contexto);
                    return resp;
                } 
            }
            return RespuestaTAS.error(contexto, "TASDepositosController - postDepositosEfectivo()", e);
        }
    }
    
    public static Objeto postActualizarPago(ContextoTAS contexto) {
        Objeto datosError = new Objeto();
        try {
            String numeroTicket = contexto.parametros.string("numeroTicket", "");
            String tas = contexto.parametros.string("tas", "");
            String idCliente = contexto.parametros.string("idCliente", "");
            String estado = contexto.parametros.string("estado", "");
            Integer cantRetenidos = contexto.parametros.integer("cantRetenidos", 0);
            
            String estadoErrorCodigo = "";
            
            if ("errorCashEnd".equals(estado)) {
            	patchPagoPrestamoEfectivoReversa(contexto);
            	estadoErrorCodigo =  TASEstadoDepositoEnum.ERROR_RETENIDO.getEstadoError();
   			  }
            
            else  if ("cancelar".equals(estado)) {
            	estadoErrorCodigo =  TASEstadoDepositoEnum.ERROR.getEstadoError();
    		} else if ("retener".equals(estado)) {
            	estadoErrorCodigo =  TASEstadoDepositoEnum.ERROR_RETENIDO.getEstadoError();
    		} else if ("abortar".equals(estado)) {
            	estadoErrorCodigo =  TASEstadoDepositoEnum.ABORTADO.getEstadoError();
    		} else if ("errorCim".equals(estado)) {
            	estadoErrorCodigo =  TASEstadoDepositoEnum.ERROR_CIM.getEstadoError();
    		}
            
    		else if ("reversar".equals(estado)) {
    			patchPagoPrestamoEfectivoReversa(contexto);
    		}

            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();

            if (numeroTicket == "" || tas == "" || idCliente == "") {
                return RespuestaTAS.sinParametros(contexto, "Uno o más campos son null o están vacíos");
            }

            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");

            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");

            // Recuperar data del depo por el numero de tkt
            DepositoCuentaDTO depositoRecuperado = TasSqlDepositoEfectivo.getDepositoByNumeroTicket(contexto,
                    numeroTicket);
            datosError.set("depositoDTO", depositoRecuperado);
           

            Objeto datosActualizar = armarDatosError(depositoRecuperado);
            if (cantRetenidos!=0) {
                datosActualizar.set("cantidadRetenidos", cantRetenidos);
            }
            Objeto estadoActualizado = UtilesDepositos.actualizarEstadoDeposito(contexto, datosActualizar, estadoErrorCodigo);
            if (estadoActualizado.string("estado").contains("ERROR")) LogTAS.error(contexto, (SqlException) estadoActualizado.get("error")); 
            return armarResponseError(estadoActualizado, depositoRecuperado);
            
        } catch (Exception e) {
           
            return RespuestaTAS.error(contexto, "TASDepositosController - postActualizarDeposito", e);
        }
    }
    private static Objeto armarDatosDepositoPositivo(DepositoCuentaDTO depositoCuentaDTO, Objeto datosPositivo){
        Objeto datos = new Objeto();
        datos.set("numeroOperacion", datosPositivo.string("recibo"));
        datos.set("personaEstado", null);
        datos.set("cantidadRetenidos", 0);
        datos.set("kioscoId", depositoCuentaDTO.getKioscoId());
        datos.set("depositoId", depositoCuentaDTO.getDepositoId());
        return datos;
    }
    private static Objeto armarDatosError(DepositoCuentaDTO depositoCuentaDTO){
        Objeto datos = new Objeto();
        datos.set("numeroOperacion", null);
        datos.set("personaEstado", null);
        datos.set("cantidadRetenidos", 0);
        datos.set("kioscoId", depositoCuentaDTO.getKioscoId());
        datos.set("depositoId", depositoCuentaDTO.getDepositoId());
        return datos;
    }
    private static Objeto armarResponseDeposito(Objeto responseDeposito, DepositoCuentaDTO depositoRecuperado) {
        Objeto response = new Objeto();
        response.set("numeroTicket", responseDeposito.string("recibo"));
        response.set("processId", depositoRecuperado.getProcessId());
        return response;
    }
    
    private static Objeto armarResponseError(Objeto estado, DepositoCuentaDTO depositoRecuperado) {
        Objeto response = new Objeto();
        response.set("estado", estado.string("estado"));

        return response;
    }
    
    

    // FIXME: Revisame luego
    public static Objeto patchPagoPrestamoEfectivoReversa(ContextoTAS contexto) {
        String numeroTicket = contexto.parametros.string("numeroTicket", "");
        try {

            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();

            // ?hardcode para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");

            String tas = contexto.parametros.string("tas", "");
            String nroPrestamo = contexto.parametros.string("nroPrestamo", "");

            

            if (nroPrestamo == "" || tas == "") {
                return RespuestaTAS.sinParametros(contexto, "Uno o más campos son null o están vacíos");
            }
            
            // Recuperar data del depo por el numero de tkt
            DepositoCuentaDTO depositoRecuperado = TasSqlDepositoEfectivo.getDepositoByNumeroTicket(contexto,
                    numeroTicket);
            Fecha fecha = depositoRecuperado.getFechaDeposito();
            String hora = depositoRecuperado.getFechaDeposito().string(
                    "HH:mm:ss");
            String lote = String.valueOf(depositoRecuperado.getLote());
            String moneda = depositoRecuperado.getCodigoMoneda();

            BigDecimal monto = depositoRecuperado.getImporteTotalEfectivo();
            String oficina = String.valueOf(depositoRecuperado.getSucursalId());
            String precinto = depositoRecuperado.getPrecinto();
            String idReversa = depositoRecuperado.getProcessId();
            
            

            TasPagoPrestamoEfectivo deposito = new TasPagoPrestamoEfectivo(nroPrestamo, fecha, hora, lote, monto, oficina,
                    precinto, tas);
            
            Objeto datosActualizar = armarDatosError(depositoRecuperado);
            Objeto estadoActualizado = UtilesDepositos.actualizarEstadoDeposito(contexto, datosActualizar, TASEstadoDepositoEnum.REVERSAR.getEstadoError());
   
            LogTAS.evento(contexto, "INICIO_PAGO_PRESTAMO_EFECTIVO_REVERSA", deposito.objeto());
            Objeto responsePagoTarjetaEfectivo = TasRestPagoPrestamoEfectivo.patchPagoPrestamoEfectivoReversa(contexto, deposito,
                    idReversa);
            LogTAS.evento(contexto, "FIN_PAGO_PRESTAMO_EFECTIVO_REVERSA", responsePagoTarjetaEfectivo);

            if(estadoActualizado.string("estado").contains("ERROR")) LogTAS.error(contexto, (SqlException) estadoActualizado.get("error"));

            return responsePagoTarjetaEfectivo;
            
        } catch (Exception e) {
        	UtilesDepositos.actualizarCodigoRetornoTimeout(contexto, numeroTicket, 100);
            return RespuestaTAS.error(contexto, "TASDepositosController - pathDepositosEfectivoReversa()", e);
        }
    }

}