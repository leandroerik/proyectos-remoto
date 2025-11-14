package ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.utils;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.modelos.TASDatosBasicosCuenta;
import ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.services.TASRestCuenta;
import ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.services.TASSqlCuenta;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidada.servicios.TASRestPosicionConsolidada;
import ar.com.hipotecario.canal.tas.shared.utils.models.strings.TASMensajesString;

import java.util.*;

public class UtilesCuenta {

    public static Objeto armarParametrosUrl(String tipoCta) {
        Fecha fechaDesde = new Fecha(new Date()).restarMeses(3);
        Fecha fechaHasta = new Fecha(new Date());
        Objeto params = new Objeto();
        params.set("fechadesde", fechaDesde.string("yyyy-MM-dd"));
        params.set("fechahasta", fechaHasta.string("yyyy-MM-dd"));
        params.set("numeropagina", 1);
        params.set("orden", "D");
        params.set("tipomovimiento", "T");
        params.set("pendientes", 0);
        params.set("validactaempleado", false);
        return params;
    }

    public static Objeto armarRespuestaMovimientos(Objeto apiResponse) {
        Objeto response = new Objeto();
        for (Objeto obj : apiResponse.objetos()) {
            Objeto rpn = new Objeto();
            rpn.set("importe", obj.string("valor"));
            rpn.set("descripcion", obj.string("descripcionMovimiento"));
            rpn.set("fechaMovimiento", obj.string("fecha"));
            if (!rpn.string("fechaMovimiento").isEmpty())
                response.add(rpn);
        }
        return response;
    }

    // todo: armar la respuesta q se la pasa al controller
    public static Objeto validarDatosParaBaja(ContextoTAS contexto, String idCliente, String tipoCuenta,
            String idCuenta, String idCuentaCobis) {
        try {
            boolean solicitudesEnCurso = validarSolicitudesEnCurso(contexto, idCliente, tipoCuenta, idCuenta);
            if (solicitudesEnCurso)
                return RespuestaTAS.error(contexto, TASMensajesString.ERROR_SOLICITUD_EN_CURSO.toString(),
                        TASMensajesString.ERROR_SOLICITUD_EN_CURSO.getTipoMensaje(),
                        "El cliente " + idCliente + TASMensajesString.MOTIVO_SOLICITUD_EN_CURSO.getTipoMensaje());
            boolean variosTitulares = validartitulares(contexto);
            if (variosTitulares)
                return RespuestaTAS.error(contexto, TASMensajesString.ERROR_OFICIAL_DE_NEGOCIOS.toString(),
                        TASMensajesString.ERROR_OFICIAL_DE_NEGOCIOS.getTipoMensaje(), "El cliente " + idCliente
                                + TASMensajesString.MOTIVO_VALIDACION_VARIOS_TITULARES.getTipoMensaje());
            Objeto comitentesAsociadas = validarCuentasComitentesAsociadas(contexto, idCliente, idCuenta, tipoCuenta);
            if (comitentesAsociadas.bool("cuentaAsoc") || comitentesAsociadas.string("estado").equals("ERROR"))
                return RespuestaTAS.error(contexto, TASMensajesString.ERROR_OFICIAL_DE_NEGOCIOS.toString(),
                        TASMensajesString.ERROR_OFICIAL_DE_NEGOCIOS.getTipoMensaje(), "El cliente " + idCliente
                                + TASMensajesString.MOTIVO_VALIDACION_CUENTA_COMITENTE.getTipoMensaje());
            Objeto tieneBloqueosEnCta = validarBloqueosEnCta(contexto, idCuentaCobis);
            if (tieneBloqueosEnCta.bool("bloqueos") || tieneBloqueosEnCta.string("estado").equals("ERROR"))
                return RespuestaTAS.error(contexto, TASMensajesString.ERROR_OFICIAL_DE_NEGOCIOS.toString(),
                        TASMensajesString.ERROR_OFICIAL_DE_NEGOCIOS.getTipoMensaje(), "El cliente " + idCliente
                                + TASMensajesString.MOTIVO_VALIDACION_BLOQUEO_CUENTA.getTipoMensaje());
            return new Objeto().set("valida_baja", true);
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "UtilesCuenta - validarDatosParaBaja()", e);
        }
    }

    private static boolean validarSolicitudesEnCurso(ContextoTAS contexto, String idCliente, String tipoCuenta,
            String idCuenta) {
        List<String> fechasSql = TASSqlCuenta.getSolicitudesEnCurso(contexto, idCliente, tipoCuenta, idCuenta);
        if (fechasSql == null) return false;
        List<Fecha> fechasSolicitudes = parsearFechasSql(fechasSql);
        long timestamp = new Date().getTime();
        Fecha fechaLimite = new Fecha(timestamp);
        Date diaElegido = fechaLimite.restarDias(2).FechaDate();
        for (Fecha solicitud : fechasSolicitudes) {
            Date fechaSolicitud = solicitud.fechaDate();
            if (solicitud != null && fechaSolicitud.after(diaElegido)) {
                return true;
            }
        }
        return false;
    }
    private static List<Fecha> parsearFechasSql(List<String> fechasSql){
        Fecha fecha = null;
        List<Fecha> fechasSolicitudes = new ArrayList<>();
        for(String fechaSql : fechasSql){
            long fechaSqlConvert = Long.parseLong(fechaSql);
            fecha = new Fecha(new Date(fechaSqlConvert));
            fechasSolicitudes.add(fecha);
        }
        return fechasSolicitudes;
    }

    private static Objeto validarCuentasComitentesAsociadas(ContextoTAS contexto, String idCliente, String idCuenta,
            String tipoCuenta) {
        try {
            Objeto posicionConsolidada = TASRestPosicionConsolidada.getPosicionConsolidada(contexto, idCliente);
            if (posicionConsolidada == null)
                return RespuestaTAS.sinResultados(contexto, "Sin datos para el idCliente");
            Objeto cuentasComitentes = posicionConsolidada.objeto("inversiones"); //todo: verificar null pointer
            Objeto cuentasComitentesAsociadas = obtenerCuentasComitentesAsociadas(contexto, cuentasComitentes);

            if (cuentasComitentesAsociadas.objetos().size() > 1) {
                for (Objeto cuenta : cuentasComitentesAsociadas.objetos()) {
                    String nroCuentaAsoc = cuenta.string("NUMERO").trim();
                    String tipoCuentaAsoc = cuenta.string("TIPO");
                    if (nroCuentaAsoc.equals(idCuenta) && tipoCuentaAsoc.equals(tipoCuenta))
                        return new Objeto().set("cuentaAsoc", true);
                }
            }
            if (cuentasComitentesAsociadas.objetos().size() == 0)
            	return new Objeto().set("cuentaAsoc", false);
            
            String nroCuentaAsoc = cuentasComitentesAsociadas.objetos().get(0).string("NUMERO").trim();
            String tipoCuentaAsoc = cuentasComitentesAsociadas.objetos().get(0).string("TIPO");
            return new Objeto().set("cuentaAsoc", nroCuentaAsoc.equals(idCuenta) && tipoCuentaAsoc.equals("AHO"));
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "UtilesCuenta - validarCuentasComitentesAsociadas", e);
        }
    }

    private static Objeto obtenerCuentasComitentesAsociadas(ContextoTAS contexto, Objeto cuentasComitentes) {
        try {
            Objeto cuentasAsocList = new Objeto();
            for (Objeto cta : cuentasComitentes.objetos()) {
                Objeto cuentasComitentesAsociadas = TASRestCuenta.getCuentasComitentesPorId(contexto,
                        cta.string("numeroProducto"));
                int index = 0;
                if (cuentasComitentesAsociadas.objetos().size() > 1) {
                    cuentasAsocList.add(cuentasComitentesAsociadas.objetos().get(index));
                    index++;
                }
                cuentasAsocList.add(cuentasComitentesAsociadas.objetos().get(0));
            }
            return cuentasAsocList;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "UtilesCuentas - obtenerCuentasAsociadas() ", e);
        }
    }

    private static boolean validartitulares(ContextoTAS contexto) {
        Integer cantTitulares = contexto.parametros.integer("titulares", 0);
        return cantTitulares == 0 || cantTitulares > 1;
    }

    private static Objeto validarBloqueosEnCta(ContextoTAS contexto, String idCuentaCobis) {
        try {
            Objeto bloqueos = TASRestCuenta.getBloqueosCuentaCA(contexto, idCuentaCobis);
            return !bloqueos.isEmpty() && bloqueos.objetos().size() > 0 ? new Objeto().set("bloqueos", true)
                    : new Objeto().set("bloqueos", false);
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "UtilesCuenta - validarBloqueosEnCta()", e);
        }
    }
    public static String formatearSimboloMoneda(String codigoMoneda) {
        String simboloMoneda = "";
        simboloMoneda = "80".equals(codigoMoneda) ? "$" : simboloMoneda;
        simboloMoneda = "2".equals(codigoMoneda) ? "USD" : simboloMoneda;
        simboloMoneda = "98".equals(codigoMoneda) ? "Eur" : simboloMoneda;
        simboloMoneda = "88".equals(codigoMoneda) ? "UVAs" : simboloMoneda;
        return simboloMoneda;
    }

	public static String sacarCeros(String nume) {
		int j = 0;
		for (int i = 0; i < nume.length(); i++) {
			if (String.valueOf(nume.charAt(i)).equals("0")) {
				j = i;
				continue;
			} else
				break;
		}
		return nume.substring(j);
	}

    public static List<TASDatosBasicosCuenta> buscarCuentas(ContextoTAS contexto, String idCliente,  Objeto cuentasArray){
        try {
            Objeto responseApiCtas =  TASRestCuenta.getCuentas(contexto, idCliente, false, false, false);
            if(responseApiCtas.objetos().size() < 1) return null;
            List<TASDatosBasicosCuenta> cuentas = new ArrayList<>();
            for(Object cuentaArray : cuentasArray.toList()){
                TASDatosBasicosCuenta cuenta = new TASDatosBasicosCuenta();
                String nroCuenta = cuentaArray.toString();
                for(Objeto cuentaApi : responseApiCtas.objetos()){
                    if(cuentaApi.string("numeroProducto").equals(nroCuenta)){
                        cuenta.setNroProducto(cuentaApi.string("idProducto"));
                        cuenta.setTipoCuenta(cuentaApi.string("tipoProducto", "AHO").equals("AHO") ? "CA" : "CC");
                        cuenta.setTipoTitular(cuentaApi.string("tipoTitularidad", "T"));
                    }
                }
                cuenta.setNroCuenta(nroCuenta);
                cuentas.add(cuenta);
            }
            return cuentas;
        } catch (Exception e) {
            LogTAS.error(contexto, e);
            return null;
        }
    }

    public static Objeto getSaldosHistoricos(ContextoTAS contexto, List<TASDatosBasicosCuenta> cuentas){              
            Objeto response = new Objeto();  
            for(TASDatosBasicosCuenta cuenta : cuentas){                           
                Objeto responseSaldos = TASRestCuenta.getSaldosHistoricos(contexto, cuenta.getNroProducto(), cuenta.getNroCuenta());                
                if(responseSaldos.string("estado").equals("ERROR")){
                    cuenta.setIdCategoria(responseSaldos.string("categoria"));
                }else{
                    if(responseSaldos.string("categoria").equals("K") || responseSaldos.string("categoria").equals("EV")
                    || responseSaldos.string("categoria").equals("L") || responseSaldos.string("categoria").equals("M")){
                        cuenta.setIdCategoria(responseSaldos.string("categoria").equals("M") ? "EV" : responseSaldos.string("categoria"));
                    }else{
                        cuenta.setIdCategoria("MOV");
                    }              
                
                    if(!cuenta.getIdCategoria().equals("MOV")){
                        response.set("categoriaId", cuenta.getIdCategoria());
                        return response;
                    }
            }
                response.set("categoriaId", "MOV");
            }
            return response;       
    }

}