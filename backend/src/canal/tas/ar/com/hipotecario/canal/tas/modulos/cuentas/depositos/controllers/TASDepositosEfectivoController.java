package ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.controllers;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.models.DepositosDestinoCuenta;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.models.TasDepositoEfectivo;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.service.TasRestDepositoEfectivo;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.service.TasSqlDepositoEfectivo;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.utils.UtilesDepositos;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.auditor.controllers.TASApiAuditor;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.models.DepositoCuentaDTO;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.models.DepositoValores;
import ar.com.hipotecario.canal.tas.shared.modulos.depositos.models.ResponseInsertValores;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKiosco;
import ar.com.hipotecario.canal.tas.shared.utils.models.enums.TASCodigoMonedaEnum;
import ar.com.hipotecario.canal.tas.shared.utils.models.enums.TASEstadoDepositoEnum;
import ar.com.hipotecario.canal.tas.shared.utils.models.strings.TASMensajesString;
//import ar.gabrielsuarez.glib.G;

public class TASDepositosEfectivoController {

    // 1. inicia deposito
    public static Objeto postInicializarDeposito(ContextoTAS contexto) {

        LogTAS.evento(contexto, "INICIALIZANDO_DEPOSITO");

        String codigoCliente = contexto.parametros.string("codigoCliente", "");

        String valores = contexto.parametros.string("valores", "");
        String kioscoId = contexto.parametros.string("kioscoId", "0");
        String codigoMoneda = contexto.parametros.string("codigoMoneda", "");
        int numeroSobre = contexto.parametros.integer("numeroSobre", 0);
        String tipoDeposito = contexto.parametros.string("tipoDeposito",
                TASMensajesString.DESTINO_CUENTA.toString());
        String codigoBanca = contexto.parametros.string("codigoBanca", "");
        String descripcionBanca = contexto.parametros.string("descripcionBanca", "");
        String numeroCuenta = contexto.parametros.string("numeroCuenta", "");
        String tipoCuenta = contexto.parametros.string("tipoCuenta", "");
        String tipoCliente = contexto.parametros.string("tipoCliente", "");
        String productoId = contexto.parametros.string("productoId", "");
        String numeroCuentaCobis = contexto.parametros.string("numeroCuentaCobis", "");
        String tipoDocumento = contexto.parametros.string("tipoDocumento", "");
        String numeroDocumento = contexto.parametros.string("numeroDocumento", "");
        String numeroOperacionCanal = contexto.parametros.string("numeroOperacionCanal", "");

        if (kioscoId == "0" || codigoMoneda == "" || tipoDeposito == ""
                || numeroCuenta == "" || tipoCuenta == ""
                || numeroCuentaCobis == "" || codigoCliente == ""
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
                    "TASDepositosController - postInicializarDeposito() - TasSqlDepositoEfectivo.getPrecintosLotes", e);
        }

        // Datos OK
        TASKiosco kiosco = contexto.getKioscos().get(Integer.parseInt(kioscoId));
        String precintos = datosTas.string("Precinto1") + "-" + datosTas.string("Precinto2");
        String processId = UtilesDepositos.getNumeroTicket(kioscoId);
        // String numeroTicket = tad.save(deposito);

        DepositoCuentaDTO deposito = new DepositoCuentaDTO();
        deposito.setTasOriginaria(kiosco.getKioscoId());
        deposito.setNumeroSobre(numeroSobre);
        deposito.setNumeroOperacionCanal(numeroOperacionCanal);
        deposito.setSucursal(kiosco.getSucursalId());
        deposito.setTipoDocumento(tipoDocumento);
        deposito.setNumeroDocumento(numeroDocumento);
        deposito.setTipoDestino(tipoDeposito);
        deposito.setDestinoDescripcion("Cuenta " + tipoCuenta + " " + numeroCuenta);
        deposito.setMoneda(codigoMoneda.equals("80") ? TASCodigoMonedaEnum.PESOS.getCodigoMoneda() : TASCodigoMonedaEnum.DOLARES.getCodigoMoneda());
        deposito.setNumeroCuentaDestino(numeroCuenta);
        deposito.setCodigoCliente(codigoCliente);
        deposito.setTipoCliente(tipoCliente);
        deposito.setCodigoBanca(codigoBanca);
        deposito.setDescripcionBanca(descripcionBanca);
        deposito.setProcessId(processId);
        deposito.setIdProductoCuenta(productoId);
        deposito.setTipoCuenta(tipoCuenta);
        deposito.setNumeroCuentaCOBIS(numeroCuentaCobis);
        deposito.setEsInteligente(true);
        deposito.setLote(datosTas.integer("Lote"));
        deposito.setPrecinto(precintos);
        deposito.setValores(valores);
        deposito.setTipoCuentaDestino(tipoCuenta);

        Map<String, Object> responseSp = TasSqlDepositoEfectivo.saveOrdenDeposito(contexto, deposito);
        if(responseSp.isEmpty() || responseSp == null) return RespuestaTAS.error(contexto, " no se pudo grabar el deposito");
        Long depositoId = Long.decode(responseSp.get("DepositoId").toString());
        String numeroTicket = responseSp.get("NumeroTicket").toString();
        // Inseertar en la otra tabla
        DepositosDestinoCuenta depo = new DepositosDestinoCuenta(
                kiosco.getKioscoId(),
                depositoId,
                codigoCliente,
                tipoCuenta,
                numeroCuenta,
                productoId,
                numeroCuentaCobis);

        boolean resultsInsertTASKioscosDepositosDestinoCuenta;

        try {
            resultsInsertTASKioscosDepositosDestinoCuenta = TasSqlDepositoEfectivo
                    .saveTASKioscosDepositosDestinoCuenta(contexto, depo);
        } catch (Exception e) {
            return RespuestaTAS.error(
                    contexto,
                    "TASDepositosController - postInicializarDeposito() - TasSqlDepositoEfectivo.saveTASKioscosDepositosDestinoCuenta",
                    e);
        }

        // si falla el ultimo insert, elimino el registro de la tabla del SP
        if (!resultsInsertTASKioscosDepositosDestinoCuenta) {
            try {

                boolean isDeletedOrdenDeposito = TasSqlDepositoEfectivo.deleteOrdenDeposito(contexto, depositoId,
                        numeroTicket);
                String msgError = isDeletedOrdenDeposito
                        ? "Fallo insert en [TASKioscosDepositosDestinoCuenta]. Se elimino registro [TASKioscosDepositos]"
                        : "Fallo insert en [TASKioscosDepositosDestinoCuenta]. Fallo borrado del registro [TASKioscosDepositos]";
                return RespuestaTAS.error(contexto, msgError);
            } catch (Exception e) {
                return RespuestaTAS.error(
                        contexto,
                        "TASDepositosController - postInicializarDeposito() - TasSqlDepositoEfectivo.deleteOrdenDeposito",
                        e);
            }
        }

        // Grabar en Api Auditor el numeroTicket
        Objeto reporte = TASApiAuditor.generarReporteGeneraTicket(contexto, kiosco, numeroTicket);
        if(!reporte.string("estado").equals("OK")) LogTAS.error(contexto, (Exception) reporte.get("error"));
        LogTAS.evento(contexto, "FINALIZO_DEPOSITO");
        responseSp.put("processId", processId);
        return Objeto.fromMap(responseSp);
    }

    // 2. Carga valores y modifica el monto total en efectivo
    public static Objeto registrarBilletesDeposito(ContextoTAS contexto) {
        String codigoCliente = contexto.parametros.string("codigoCliente", "");
        String numeroTicket = contexto.parametros.string("numeroTicket", "");
        BigDecimal importe = contexto.parametros.bigDecimal("importe", "0.0");
        Integer cantNoReconocidos = contexto.parametros.integer("cantNoReconocidos", 0);
        List<Object> valores = contexto.parametros.objeto("valores", new Objeto()).toList();

        if (numeroTicket == "" || importe == null || cantNoReconocidos == null || valores.isEmpty()
                || codigoCliente == "") {
            return RespuestaTAS.sinParametros(contexto, "Uno o más campos son null o están vacíos");
        }

        try {
            // 1- Buscar el deposito por NumeroTicket.
            DepositoCuentaDTO depositoRecuperado = TasSqlDepositoEfectivo.getDepositoByNumeroTicket(contexto,
                    numeroTicket);

            // 2- Insertar en la tabla [TASKioscosDepositosValores] los valores que recibo
            List<DepositoValores> valoresList = UtilesDepositos.armaListaDepositos(depositoRecuperado, valores);
            ResponseInsertValores grabaronValores = TasSqlDepositoEfectivo.insertValores(contexto, valoresList);
            if (grabaronValores.hasErrors || !grabaronValores.getImporteTotal().equals(importe)) {
                // Responder error y finalizar
                return RespuestaTAS.error(contexto, "Error al grabar los valores enviados.");
            }

            // 3- Update del deposito [TASKioscosDepositos] el campo ImporteTotalEfectivo
            boolean guardoTotales = TasSqlDepositoEfectivo.updateImporteTotalEfectivo(contexto, numeroTicket, importe);

            if (!guardoTotales) {
                return RespuestaTAS.error(contexto, "Error al hacer la modificacion de total en efectivo");
            }

            return RespuestaTAS.exito();
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASDepositosController - registrarBilletesDeposito()", e);
        }
    }

    public static Objeto postDepositosEfectivo(ContextoTAS contexto) {
        Objeto datosError = new Objeto();
        try {
            String numeroTicket = contexto.parametros.string("numeroTicket", "");
            String tas = contexto.parametros.string("tas", "");
            String idCliente = contexto.parametros.string("idCliente", "");
            String cuenta = contexto.parametros.string("cuenta", "");
            String producto = contexto.parametros.string("producto", "AHO");

            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();

            if (numeroTicket == "" || tas == "" || idCliente == "") {
                return RespuestaTAS.sinParametros(contexto, "Uno o más campos son null o están vacíos");
            }

            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");

            /* 
            fix 06/01/2025

            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            */
            // Recuperar data del depo por el numero de tkt
            DepositoCuentaDTO depositoRecuperado = TasSqlDepositoEfectivo.getDepositoByNumeroTicket(contexto,
                    numeroTicket);
            datosError.set("depositoDTO", depositoRecuperado);
            Fecha fecha = depositoRecuperado.getFechaDeposito();
            Integer lote = depositoRecuperado.getLote();
            String moneda = depositoRecuperado.getCodigoMoneda();

            BigDecimal monto = depositoRecuperado.getImporteTotalEfectivo();
            Integer oficina = depositoRecuperado.getSucursalId();
            String precinto = depositoRecuperado.getPrecinto();

            TasDepositoEfectivo deposito = new TasDepositoEfectivo(cuenta, fecha, lote, moneda, monto, oficina,
                    precinto, producto, tas);
            
            LogTAS.evento(contexto, "INICIO_DEPOSITO_EFECTIVO", deposito.objeto());
            Objeto responseDepositoEfectivo = TasRestDepositoEfectivo.postDepositosEfectivo(contexto, deposito);
            Objeto datosActualizar = armarDatosDepositoPositivo(depositoRecuperado, responseDepositoEfectivo);
            Objeto estadoActualizado = UtilesDepositos.actualizarEstadoDeposito(contexto, datosActualizar, TASEstadoDepositoEnum.OK.getEstadoError());
            if(estadoActualizado.string("estado").contains("ERROR")) LogTAS.error(contexto, (SqlException) estadoActualizado.get("error"));
            Objeto response = armarResponseDeposito(responseDepositoEfectivo, depositoRecuperado);
            LogTAS.evento(contexto, "FIN_DEPOSITO_EFECTIVO", response);
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
                    patchDepositosEfectivoReversa(contexto);
                    return resp;
                } 
            }
            return RespuestaTAS.error(contexto, "TASDepositosController - postDepositosEfectivo()", e);
        }
    }
    
    public static Objeto postActualizarDeposito(ContextoTAS contexto) {
        Objeto datosError = new Objeto();
        try {
            String numeroTicket = contexto.parametros.string("numeroTicket", "");
            String tas = contexto.parametros.string("tas", "");
            String idCliente = contexto.parametros.string("idCliente", "");
            String cuenta = contexto.parametros.string("cuenta", "");
            String producto = contexto.parametros.string("producto", "AHO");
            String estado = contexto.parametros.string("estado", "");
            Integer cantRetenidos = contexto.parametros.integer("cantRetenidos", 0);
            
            String estadoErrorCodigo = "";
            
            if ("errorCashEnd".equals(estado)) {
            	patchDepositosEfectivoReversa(contexto);
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
    			patchDepositosEfectivoReversa(contexto);
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
    public static Objeto patchDepositosEfectivoReversa(ContextoTAS contexto) {
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
            String cuenta = contexto.parametros.string("cuenta", "");
            String producto = contexto.parametros.string("producto", "AHO");

            

            if (cuenta == ""  || producto == "" || tas == "") {
                return RespuestaTAS.sinParametros(contexto, "Uno o más campos son null o están vacíos");
            }
            
            // Recuperar data del depo por el numero de tkt
            DepositoCuentaDTO depositoRecuperado = TasSqlDepositoEfectivo.getDepositoByNumeroTicket(contexto,
                    numeroTicket);
            Fecha fecha = depositoRecuperado.getFechaDeposito();
            Integer lote = depositoRecuperado.getLote();
            String moneda = depositoRecuperado.getCodigoMoneda();

            BigDecimal monto = depositoRecuperado.getImporteTotalEfectivo();
            Integer oficina = depositoRecuperado.getSucursalId();
            String precinto = depositoRecuperado.getPrecinto();
            String idReversa = depositoRecuperado.getProcessId();
            
            

            TasDepositoEfectivo deposito = new TasDepositoEfectivo(cuenta, fecha, lote, moneda, monto, oficina,
                    precinto, producto, tas);
            
            Objeto datosActualizar = armarDatosError(depositoRecuperado);
            Objeto estadoActualizado = UtilesDepositos.actualizarEstadoDeposito(contexto, datosActualizar, TASEstadoDepositoEnum.REVERSAR.getEstadoError());
   
            LogTAS.evento(contexto, "INICIO_DEPOSITO_EFECTIVO_REVERSA", deposito.objeto());
            Objeto responseDepositoEfectivo = TasRestDepositoEfectivo.patchDepositosEfectivoReversa(contexto, deposito,
                    idReversa);
            LogTAS.evento(contexto, "FIN_DEPOSITO_EFECTIVO_REVERSA", responseDepositoEfectivo);

            if(estadoActualizado.string("estado").contains("ERROR")) LogTAS.error(contexto, (SqlException) estadoActualizado.get("error"));

            return responseDepositoEfectivo;
            
        } catch (Exception e) {
        	UtilesDepositos.actualizarCodigoRetornoTimeout(contexto, numeroTicket, 100);
            return RespuestaTAS.error(contexto, "TASDepositosController - pathDepositosEfectivoReversa()", e);
        }
    }

}