package ar.com.hipotecario.canal.tas.modulos.plazosfijos.controllers;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.plazosfijos.modelos.TASTasas;
import ar.com.hipotecario.canal.tas.modulos.plazosfijos.services.TASRestPlazosFijos;
import ar.com.hipotecario.canal.tas.modulos.plazosfijos.services.TASSqlPlazosFijos;
import ar.com.hipotecario.canal.tas.modulos.plazosfijos.utils.UtilesPlazosFijos;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.catalogo.servicios.TASRestCatalogo;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.inversiones.servicios.TASRestInversiones;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;
import ar.com.hipotecario.canal.tas.shared.utils.models.strings.TASMensajesString;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

public class TASPlazosFijosController {
    
        public static Objeto getTiposPF(ContextoTAS contexto){
            try{
                String idCliente = contexto.parametros.string("idCliente");
                TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
                // ? comentar las proximas 2 lineas para no validar el cliente en sesion
                if (clienteSesion == null)
                    return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
                if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                    return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
                List<TASTasas> tasasResponse = UtilesPlazosFijos.getTasasPF(contexto, idCliente);
                if(tasasResponse == null) return RespuestaTAS.error(contexto, "Error obteniendo Tasas");
                Objeto tiposPF = armarTiposPF(tasasResponse);
                return tiposPF;
            }catch (Exception e){
                return RespuestaTAS.error(contexto, "TASPlazosFijosController - getTasasPF()", e);
            }
        }
    
    
        private static Objeto armarTiposPF(List<TASTasas> tasasList){
            Objeto tiposDiscriminados = UtilesPlazosFijos.discriminarTiposPF(tasasList);
            Objeto response = new Objeto();
            response.set("tasas", tiposDiscriminados.get("tasas"));
            response.set("cantidadTiposTasas", tiposDiscriminados.integer("cantTasas"));
            response.set("tipos", tiposDiscriminados.get("tipos"));
            response.set("cantidadTipos", tiposDiscriminados.integer("cant"));
            response.set("cantidadTiposUva", tiposDiscriminados.integer("tiposUva"));
            response.set("leyenda", TASMensajesString.PLAZO_FIJO_PESOS_LEYENDA.getTipoMensaje());
    
            return response;
        }
    
    
        public static Objeto getTasasPromocionalesPF(ContextoTAS contexto) {
            try {
                 String idCliente = contexto.parametros.string("idCliente");
                 TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
                 // ? comentar las proximas 2 lineas para no validar el cliente en sesion
                 if (clienteSesion == null)
                     return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
                 if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                     return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
                return TASRestPlazosFijos.getTasasPreferencialesByIdCliente(contexto, idCliente);
            } catch (Exception e) {
                return RespuestaTAS.error(contexto, "TASPlazosFijosController - getTasasPromocionalesPF()", e);
            }
        }
    
        public static Objeto getDetallePF(ContextoTAS contexto){
            try{
                String idCliente = contexto.parametros.string("idCliente");
                TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
                // ? comentar las proximas 2 lineas para no validar el cliente en sesion
                if (clienteSesion == null)
                    return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
                if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                    return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
                String idPF = contexto.parametros.string("idPlazoFijo");
                Objeto consultaPF = TASRestPlazosFijos.getDetallePF(contexto, idPF);
                if(consultaPF.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASPlazosFijosController - getDetallePF()", (Exception) consultaPF.get("error"));
                if(consultaPF.string("estado").equals("SIN_RESULTADOS")) return RespuestaTAS.sinResultados(contexto, "Sin resultados para ese ID de Plazo Fijo");
                return armarResponseDetallePF(contexto, consultaPF, idPF);
            }catch (Exception e) {
                return RespuestaTAS.error(contexto, "TASPlazosFijosController - getPlazosFijos()", e);
            }
        }
    
        private static Objeto armarResponseDetallePF(ContextoTAS contexto, Objeto responseApi, String idPF){
            Objeto response = new Objeto();
            Objeto responseDetalle = responseApi.objeto("respuesta").objetos().get(0);
            response.set("estadoPlazoFijo", responseDetalle.string("idEstado"));
            response.set("estadoPlazoFijoDescripcion", responseDetalle.string("descEstado"));
            Date fechaConstitucion = new Fecha(responseDetalle.string("fechaValor"), "yyyy-MM-dd").fechaDate();
            response.set("fechaConstitucion", new SimpleDateFormat("dd/MM/yyyy").format(fechaConstitucion));
            Date fechaVencimiento = new Fecha(responseDetalle.string("fechaVencimiento"),"yyyy-MM-dd").FechaDate();
            response.set("fechaVencimiento", new SimpleDateFormat("dd/MM/yyyy").format(fechaVencimiento));
            double importeConstitucion = Double.valueOf(responseDetalle.string("importe"));
            response.set("importeConstitucion", importeConstitucion);
            double importeInteres = Double.valueOf(responseDetalle.string("interesEstimado"));
            response.set("importeInteres",importeInteres );
            response.set("importeVencimiento", importeInteres != 0 ? importeConstitucion + importeInteres : importeInteres);
            response.set("codigoMoneda", responseDetalle.string("idMoneda"));
            String tipoOperacion = responseDetalle.string("tipoOperacion");
            response.set("tipoOperacion", tipoOperacion);
            response.set("tipoOperacionDescripcion", contexto.config.string("tas_plazofijo_"+tipoOperacion));
            boolean esUva = tipoOperacion.equals("0018") || tipoOperacion.equals("0041") || tipoOperacion.equals("0042") || tipoOperacion.equals("0043") || tipoOperacion.equals("0044");
            response.set("esUva", esUva);
            response.set("numeroOperacion", responseDetalle.integer("nroOperacion"));
            response.set("numeroSolicitud", idPF);
            response.set("plazoFijoNumero", idPF);
            response.set("tasaNominal", Double.valueOf(responseDetalle.bigDecimal("tasa").toString()));
            response.set("flagRenovacionAutomatica", responseDetalle.bool("renovacionAutomatica") ? "S" : "N");
            response.set("tasaEfectiva", Double.valueOf(responseDetalle.string("tasaEfectiva").toString()));
            response.set("renovaciones", responseDetalle.string("renovaciones"));
            response.set("cuenta", responseDetalle.string("cuenta"));
            response.set("acredita", responseDetalle.bool("renuevaIntereses") ? "SI" : "NO");
            response.set("plazo", responseDetalle.integer("cantidadDias"));
            response.set("impuestos", Double.valueOf(responseDetalle.string("impuesto").toString()));
            response.set("sellos", responseDetalle.integer("sellos"));
            response.set("cubiertoPorGarantia", responseDetalle.bool("cubiertoPorGarantia")? "S" : "N");
            response.set("renovacionesPendientes", responseDetalle.string("renovacionesPendientes", ""));
            if(esUva) {
                response.set("esCancAnt", responseDetalle.string("cancelacionAnticipada",""));
                if (StringUtils.isNotEmpty(responseDetalle.string("tasaCancelacionAnt"))) response.set("tasaCancelAnt", Double.valueOf(responseDetalle.string("tasaCancelacionAnt").toString()));
                if (StringUtils.isNotEmpty(responseDetalle.string("teaCancelacionAnt"))) response.set("teaCancelAnt", Double.valueOf(responseDetalle.string("teaCancelacionAnt").toString()));
                if (StringUtils.isNotEmpty(responseDetalle.string("tnaCancelacionAnt"))) response.set("tnaCancelAnt", Double.valueOf(responseDetalle.string("tnaCancelacionAnt").toString()));
                Date fechaDesdeCan = new Fecha(responseDetalle.string("fechaDesdeCancelacionAnt"), "yyyy-MM-dd").FechaDate();
                if (fechaDesdeCan!=null) response.set("fechaDesdeCanAnt",new SimpleDateFormat("dd/MM/yyyy").format(fechaDesdeCan));
                Date fechaHastaCan = new Fecha(responseDetalle.string("fechaHastaCancelacionAnt"), "yyyy-MM-dd").FechaDate();
                if (fechaHastaCan!=null) response.set("fechaHastaCanAnt", new SimpleDateFormat("dd/MM/yyyy").format(fechaHastaCan));
            }
            return response;
        }
    
        public static Objeto postConstituirPF(ContextoTAS contexto){
            try{
                String idCliente = contexto.parametros.string("idCliente");
                TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
                // ? comentar las proximas 2 lineas para no validar el cliente en sesion
                if (clienteSesion == null)
                    return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
                if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                    return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
                boolean flagSimulacion = contexto.parametros.bool("simulacion");
    
                Objeto responseConstitucion = flagSimulacion ? simularPF(contexto, idCliente) : constituirPF(contexto, idCliente);
                if(responseConstitucion.string("estado").equals("SIN_PARAMETROS")) return RespuestaTAS.sinParametros(contexto, "Uno o mas parametros no encontrados");
                if(responseConstitucion.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASPlazosFijosController - postConstituirPF()",(Exception) responseConstitucion.get("error"));
                return responseConstitucion;
            }catch (Exception e){
                return RespuestaTAS.error(contexto, "TASPlazosFijosController - postConstituirPF()", e);
            }
        }
    
        private static Objeto simularPF(ContextoTAS contexto, String idCliente){
            try{
                String numeroCuenta = contexto.parametros.string("numeroCuenta", "");
                String tipoCuenta = contexto.parametros.string("tipoCuenta", "");
                String idMoneda = contexto.parametros.string("idMoneda", "");
                int plazo = contexto.parametros.integer("plazo", -1);
                String importe = contexto.parametros.string("importe", "");
                importe = String.valueOf(importe).replace(',', '.');
                boolean renovacion = contexto.parametros.bool("renovacion", false);
                int renovaciones = contexto.parametros.integer("renovaciones", -1);
                boolean acredita = contexto.parametros.bool("acredita", false);
                String tipoPFCodigo = contexto.parametros.string("codigoTipo", "");
                String descPF = contexto.parametros.string("descripcionTipo", "");
    
                List<String> datosPF = Arrays.asList(numeroCuenta, tipoCuenta, idMoneda, String.valueOf(plazo), importe, String.valueOf(renovaciones), tipoPFCodigo);
                boolean verificaParams = verificaParams(datosPF);
                if(verificaParams) return new Objeto().set("estado", "SIN_PARAMETROS");
                boolean isUva = tipoPFCodigo.equals("0018") || tipoPFCodigo.equals("0042") || tipoPFCodigo.equals("0043");
                BigDecimal cotizacionUVA = isUva ? contexto.parametros.bigDecimal("cotizacionUVA", null) : null;
                if(isUva && cotizacionUVA == null) return new Objeto().set("estado", "SIN_PARAMETROS");
    
                String renueva = renovacion ? "S" : "N";
                int canal = Integer.valueOf(contexto.config.string("tas_plazofijo_canal"));
    
                Objeto diaHabil = UtilesPlazosFijos.verificarDiaHabil(contexto,plazo);
                if(diaHabil.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASPlazoFijoController - verificarDiaHabil()", (Exception) diaHabil.get("error"));
                plazo = diaHabil.string("estado").equals("true") ? plazo : diaHabil.integer("nuevoPlazo");
    
                Objeto parametrosSimulador = armarParametrosSimulador(idCliente, acredita, idMoneda, importe, numeroCuenta,
                        renovaciones, plazo, renueva, tipoCuenta, tipoPFCodigo, descPF, canal);
                LogTAS.evento(contexto, "INICIO_SIMULACION_PF", parametrosSimulador);
            Objeto simuladorResponse = TASRestPlazosFijos.postSimularPF(contexto, parametrosSimulador);
            Objeto response = armarResponseSimuladorPF(simuladorResponse, parametrosSimulador, isUva, cotizacionUVA);
            
            LogTAS.evento(contexto, "FIN_SIMULACION_PF", response);
            return response;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    private static Objeto armarParametrosSimulador(String idCliente, boolean acredita, String idMoneda, String importe, String numeroCuenta,
                                                   int renovaciones, int plazo, String renueva, String tipoCuenta, String tipoPF, String descPF, int canal){
        Objeto parametros = new Objeto();
        parametros.set("idCliente", idCliente);
        parametros.set("acredita", acredita);
        parametros.set("moneda", Integer.valueOf(idMoneda));
        parametros.set("monto", importe);
        parametros.set("cuenta", numeroCuenta);
        parametros.set("periodo", renovaciones);
        parametros.set("plazo", plazo);
        parametros.set("renova", renueva);
        parametros.set("tipoCuenta", tipoCuenta);
        parametros.set("tipoPlazoFijoCodigo", tipoPF);
        parametros.set("descPF", descPF);
        parametros.set("canal", canal);
        return parametros;
    }

    private static Objeto armarResponseSimuladorPF(Objeto responseApi, Objeto parametrosSimulador, boolean isUva, BigDecimal cotizacionUva ){
        Objeto response = new Objeto();
        Objeto responseSimulador = responseApi.objetos().get(0);
        response.set("importe",Double.valueOf(responseSimulador.bigDecimal("capital").toString()));
        response.set("numeroCuenta",responseSimulador.string("cuentaADebitar"));
        response.set("numeroCuotas",responseSimulador.string("cuotas", null));
        String diaPago = responseSimulador.string("diaDePago");
        response.set("diaDePago", diaPago);
        Date fechaActual = new Fecha(responseSimulador.string("fechaActual"), "yyy-MM-dd").FechaDate();
        response.set("fechaOperacion", new SimpleDateFormat("dd/MM/yyyy").format(fechaActual));
        Date fechaPagoInteres = new Fecha(responseSimulador.string("fechaPagoInteres"), "yyy-MM-dd").FechaDate();
        response.set("fechaPagoIntereses", new SimpleDateFormat("dd/MM/yyyy").format(fechaPagoInteres));
        Date fechaVencimiento = new Fecha(responseSimulador.string("fechaVencimiento"), "yyy-MM-dd").FechaDate();
        response.set("fechaVencimiento", new SimpleDateFormat("dd/MM/yyyy").format(fechaVencimiento));
        response.set("impuestos", Double.valueOf(responseSimulador.bigDecimal("impuestosAPagar").toString()));
        response.set("sellos",Double.valueOf(responseSimulador.bigDecimal("sellos").toString()));
        response.set("interesEstimado",Double.valueOf(responseSimulador.bigDecimal("interesEstimado").toString()));
        response.set("codigoMoneda",responseSimulador.integer("moneda"));
        response.set("montoEstimado",Double.valueOf(responseSimulador.bigDecimal("montoTotal").toString()));
        response.set("plazo",responseSimulador.integer("plazo"));
        response.set("tasa",Double.valueOf(responseSimulador.bigDecimal("tasa").toString()));
        response.set("codigoTipo",parametrosSimulador.string("tipoPlazoFijoCodigo"));
        response.set("descripcionTipo",parametrosSimulador.string("descPF"));
        response.set("totalInteresEstimado",Double.valueOf(responseSimulador.bigDecimal("totalInteresEstimado").toString()));
        response.set("cubiertoPorGarantia",responseSimulador.string("gtiaDeDepositos").equals("S") ? "SI" : "NO");
        if(isUva) {
            response.set("importeUVAVenc", Double.valueOf(responseSimulador.bigDecimal("capital").divide(cotizacionUva,2).toString()));// todo hacer la operacion como double y asignar
            response.set("importeUVAInicial", Double.valueOf(responseSimulador.bigDecimal("capital").divide(cotizacionUva,2).toString()));// todo hacer la operacion como double y asignar
            response.set("esCancAnt", responseSimulador.string("cancelacionAnticipada"));
            response.set("tasaCancelAnt", Double.valueOf(responseSimulador.bigDecimal("tasaCancelacionAnt").toString()));
            response.set("teaCancelAnt", Double.valueOf(responseSimulador.bigDecimal("teaCancelacionAnt").toString()));
            response.set("tnaCancelAnt", Double.valueOf(responseSimulador.bigDecimal("tnaCancelacionAnt").toString()));
            Date fechaDesdeCanAnt = new Fecha(responseSimulador.string("fechaDesdeCancelacionAnt"), "yyy-MM-dd").FechaDate();
            if (fechaDesdeCanAnt!=null) response.set("fechaDesdeCanAnt", new SimpleDateFormat("dd/MM/yyyy").format(fechaDesdeCanAnt));
            Date fechaHastaCanAnt = new Fecha(responseSimulador.string("fechaHastaCancelacionAnt"), "yyy-MM-dd").FechaDate();
            if (fechaHastaCanAnt!=null) response.set("fechaHastaCanAnt",new SimpleDateFormat("dd/MM/yyyy").format(fechaHastaCanAnt));
            if (responseSimulador.bigDecimal("tasaCancelacionLeliq120")!=null) response.set("tasaCancelLeliq120", Double.valueOf(responseSimulador.bigDecimal("tasaCancelacionLeliq120").toString()));
            if (responseSimulador.bigDecimal("teaCancelLeliq120")!=null) response.set("teaCancelLeliq120", Double.valueOf(responseSimulador.bigDecimal("teaCancelacionLeliq120").toString()));
            if (responseSimulador.bigDecimal("tnaCancelLeliq120")!=null) response.set("tnaCancelLeliq120", Double.valueOf(responseSimulador.bigDecimal("tnaCancelacionLeliq120").toString()));
            Date fechaCanLeliq = new Fecha(responseSimulador.string("fechaCancelacionLeliq120"), "yyy-MM-dd").FechaDate();
           if (fechaCanLeliq!=null) response.set("fechaCancelLeliq120", new SimpleDateFormat("dd/MM/yyyy").format(fechaCanLeliq));
            response.set("montoUVAVenc", Double.valueOf(responseSimulador.bigDecimal("montoTotal").divide(cotizacionUva,2).toString()));//todo hacer la operacion como double y asignar
        }
        return response;
    }

    private static Objeto constituirPF(ContextoTAS contexto, String idCliente){
        try{
            String numeroCuenta = contexto.parametros.string("numeroCuenta", "");
            String tipoCuenta = contexto.parametros.string("tipoCuenta", "");
            String idMoneda = contexto.parametros.string("idMoneda", "");
            int plazo = contexto.parametros.integer("plazo", -1);
            String importe = contexto.parametros.string("importe", "");
            importe = String.valueOf(importe).replace(',', '.');
            boolean renovacion = contexto.parametros.bool("renovacion", false);
            int renovaciones = contexto.parametros.integer("renovaciones", -1);
            boolean acredita = contexto.parametros.bool("acredita", false);
            String tipoPFCodigo = contexto.parametros.string("codigoTipo", "");
            String descPF = contexto.parametros.string("descripcionTipo", "");

            List<String> datosPF = Arrays.asList(numeroCuenta, tipoCuenta, idMoneda, String.valueOf(plazo), importe, String.valueOf(renovaciones), tipoPFCodigo);
            boolean verificaParams = verificaParams(datosPF);
            if(verificaParams) return new Objeto().set("estado", "SIN_PARAMETROS");
            boolean isUva = tipoPFCodigo.equals("0018") || tipoPFCodigo.equals("0042") || tipoPFCodigo.equals("0043");
            BigDecimal cotizacionUVA = isUva ? contexto.parametros.bigDecimal("cotizacionUVA", null) : null;
            if(isUva && cotizacionUVA == null) return new Objeto().set("estado", "SIN_PARAMETROS");

            String renueva = renovacion ? "S" : "N";
            int canal = Integer.valueOf(contexto.config.string("tas_plazofijo_canal"));

            Objeto diaHabil = UtilesPlazosFijos.verificarDiaHabil(contexto,plazo);
            if(diaHabil.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASPlazoFijoController - verificarDiaHabil()", (Exception) diaHabil.get("error"));
            plazo = diaHabil.string("estado").equals("true") ? plazo : diaHabil.integer("nuevoPlazo");

            Objeto parametrosAltaPF = armarParametrosAlta(idCliente, acredita, idMoneda, importe, numeroCuenta,
                    renovaciones, plazo, renueva, tipoCuenta, tipoPFCodigo, descPF, canal);
            LogTAS.evento(contexto, "INICIO_ALTA_PF", parametrosAltaPF);
            Objeto altaPFResponse = TASRestPlazosFijos.postAltaPF(contexto, parametrosAltaPF);
            if(altaPFResponse.string("estado"). equals("ERROR")) return armarResponseError(contexto, altaPFResponse);
            Objeto response = armarResponseAltaPF(altaPFResponse, parametrosAltaPF, isUva, cotizacionUVA);
            LogTAS.evento(contexto, "FIN_ALTA_PF", response);
            return response;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    private static Objeto armarParametrosAlta(String idCliente, boolean acredita, String idMoneda, String importe, String numeroCuenta,
                                              int renovaciones, int plazo, String renueva, String tipoCuenta, String tipoPF, String descPF, int canal){
        Objeto parametros = new Objeto();
        parametros.set("idCliente", idCliente);
        parametros.set("acredita", acredita ? "S" : "N");
        parametros.set("moneda", Integer.valueOf(idMoneda));
        parametros.set("monto", importe);
        parametros.set("cuenta", numeroCuenta);
        parametros.set("periodo", renovaciones);
        parametros.set("plazo", plazo);
        parametros.set("renova", renueva);
        parametros.set("tipoCuenta", tipoCuenta);
        parametros.set("tipoPlazoFijoCodigo", tipoPF);
        parametros.set("descPF", descPF);
        parametros.set("canal", canal);
        return parametros;
    }

    private static Objeto armarResponseAltaPF(Objeto responseAlta, Objeto parametrosAlta, boolean isUva, BigDecimal cotizacionUva ){
        Objeto response = new Objeto();
        response.set("numeroTicket", responseAlta.string("idOperacion"));
        double importe = Double.valueOf(responseAlta.bigDecimal("capital").toString());
        response.set("importe", importe);
        response.set("numeroCuenta", responseAlta.string("cuenta"));
        response.set("numeroCuotas", responseAlta.string("cuotas"));
        String diaPago = responseAlta.string("diaDePago");
        response.set("diaDePago", diaPago);
        Date fechaOperacion = new Fecha(responseAlta.string("fechaActual"), "yyyy-MM-dd").FechaDate();
        response.set("fechaOperacion", new SimpleDateFormat("dd/MM/yyyy").format(fechaOperacion));
        Date fechaPagoInteres = new Fecha(responseAlta.string("fechaPagoIntereses"), "yyyy-MM-dd").FechaDate();
        response.set("fechaPagoIntereses",new SimpleDateFormat("dd/MM/yyyy").format(fechaPagoInteres));
        Date fechaVencimiento = new Fecha(responseAlta.string("fechaVencimiento"), "yyyy-MM-dd").FechaDate();
        response.set("fechaVencimiento", new SimpleDateFormat("dd/MM/yyyy").format(fechaVencimiento));
        response.set("impuestos", Double.valueOf(responseAlta.bigDecimal("impuestos").toString()));
        response.set("interesEstimado", Double.valueOf(responseAlta.bigDecimal("interesEstimado").toString()));
        response.set("codigoMoneda", responseAlta.integer("moneda"));
        response.set("montoEstimado", Double.valueOf(responseAlta.bigDecimal("monto").toString()));
        response.set("numeroPlazoFijo", responseAlta.string("nroPlazoFijo"));
        response.set("plazo", responseAlta.integer("plazo"));
        response.set("tasa", Double.valueOf(responseAlta.bigDecimal("tasa").toString()));
        response.set("codigoTipo", responseAlta.string("tipoOperacion"));
        response.set("descripcionTipo", parametrosAlta.string("descPF"));
        response.set("totalInteresEstimado", Double.valueOf(responseAlta.bigDecimal("totalInteresEstimado").toString()));
        response.set("cubiertoPorGarantia", responseAlta.string("cubiertoPorGarantia").equals("S") ? "SI" : "NO");
        if(isUva) {
            response.set("importeUVAVenc", Double.valueOf(responseAlta.bigDecimal("capital").divide(cotizacionUva,2).toString()));
            response.set("importeUVAInicial", Double.valueOf(responseAlta.bigDecimal("capital").divide(cotizacionUva,2).toString()));
            response.set("esCancAnt", responseAlta.string("cancelacionAnticipada"));
            response.set("tasaCancelAnt", Double.valueOf(responseAlta.string("tasaCancelacionAnt").toString()));
            response.set("teaCancelAnt", Double.valueOf(responseAlta.string("teaCancelacionAnt").toString()));
            response.set("tnaCancelAnt", Double.valueOf(responseAlta.string("tnaCancelacionAnt").toString()));
            Date fechaDesdeCan = new Fecha(responseAlta.string("fechaDesdeCancelacionAnt"), "yyyy-MM-dd").FechaDate();
            if (fechaDesdeCan!=null) response.set("fechaDesdeCanAnt", new SimpleDateFormat("dd/MM/yyyy").format(fechaDesdeCan));
            Date fechaHastaCan = new Fecha(responseAlta.string("fechaHastaCancelacionAnt"), "yyyy-MM-dd").FechaDate();
           if (fechaHastaCan!=null) response.set("fechaHastaCanAnt", new SimpleDateFormat("dd/MM/yyyy").format(fechaHastaCan));
           if (StringUtils.isNotEmpty(responseAlta.string("tasaCancelacionLeliq120"))) response.set("tasaCancelLeliq120", Double.valueOf(responseAlta.string("tasaCancelacionLeliq120").toString()));
           if (StringUtils.isNotEmpty(responseAlta.string("teaCancelLeliq120"))) response.set("teaCancelLeliq120", Double.valueOf(responseAlta.string("teaCancelacionLeliq120").toString()));
           if (StringUtils.isNotEmpty(responseAlta.string("tnaCancelLeliq120"))) response.set("tnaCancelLeliq120", Double.valueOf(responseAlta.string("tnaCancelacionLeliq120").toString()));
            Date fechaCanLeliq = new Fecha(responseAlta.string("fechaCancelacionLeliq120"), "yyyy-MM-dd").FechaDate();
           if (fechaCanLeliq!=null) response.set("fechaCancelLeliq120", new SimpleDateFormat("dd/MM/yyyy").format(fechaCanLeliq));
        }
        return response;
    }

    private static Objeto armarResponseError(ContextoTAS contexto, Objeto responseError){
        Exception e = (Exception) responseError.get("error");
        if(e instanceof ApiException) {
            ApiException apiException = (ApiException) e;
            int codigoHttp = apiException.response.codigoHttp;
            String codigo = apiException.response.string("codigo");
            String tipoError = "";
            String mensajeAlUsuario = "";
            if(codigoHttp == 404) {
                switch (codigo){
                    case "258402":
                        tipoError = codigo;
                        mensajeAlUsuario = TASMensajesString.PLAZO_FIJO_CLIENTE_SIN_PERFIL_PATRIMONIAL.getTipoMensaje();
                        break;
                    case "141144":
                        tipoError = codigo;
                        mensajeAlUsuario = TASMensajesString.PLAZO_FIJO_CUENTA_SIN_SALDO.getTipoMensaje();
                        break;
                    case "141225":
                        tipoError = codigo;
                        mensajeAlUsuario = TASMensajesString.PLAZO_FIJO_NO_EXISTE_OFICIAL.getTipoMensaje();
                        break;
                    default:
                        tipoError = codigo;
                        mensajeAlUsuario = apiException.response.string("mensajeAlUsuario");
                        break;
                }
                return RespuestaTAS.error(contexto, tipoError, mensajeAlUsuario, "404 - codigo: " + codigo);
            }
            return RespuestaTAS.error(contexto, "TASPlazosFijosController - constituirPF()", e);
        }
        return RespuestaTAS.error(contexto, "TASPlazosFijosController - constituirPF()", e);
    }

    private static boolean verificaParams(List<String> parametrosAVerificar){
        for(String parametro : parametrosAVerificar){
            if(parametro.isEmpty() || parametro.equals("-1")) return true;
        }
        return false;
    }

    /*
     * TODO:
     *  PROBAAAARRR..... TERMINAR PARSEO DE DATOSSSSS.....
     * invertir los llamados a la api
     * negar el no encontrado
     * armar response con datos
     * */
    public static Objeto getCancelacionAnticipada(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idPF = contexto.parametros.string("idPlazoFijo");
            if(idPF.isEmpty()) return RespuestaTAS.sinParametros(contexto, "uno o mas parametros no ingresados");
            Objeto cancelacionAnticipadaResponse = getEstadoCancelacionAnticipada(contexto);
            if (cancelacionAnticipadaResponse.isEmpty()) return RespuestaTAS.sinResultados(contexto, "no se encontro estado");
            Objeto estadoCancelacionResponse = cancelacionAnticipadaResponse.string("solicitudCargada").equals("S") ? TASRestPlazosFijos.getCancelacionAnticipada(contexto, idPF) : null;
            String estadoCancelacion = estadoCancelacionResponse != null ? obtenerEstadoCancelacion(estadoCancelacionResponse) : "";

            return armarResponseEstadoCancelacion(cancelacionAnticipadaResponse, estadoCancelacion);
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASPlazosFijosController - getEstadoCancelacionAnt()",e);
        }
    }
    private static Objeto armarResponseEstadoCancelacion(Objeto responseCanAnt, String estadoCancelacion){
        Objeto response = new Objeto();
        response.set("permiteSolicitud", responseCanAnt.string("permiteSolicitud"));
        response.set("teaCancAnt", Double.valueOf(responseCanAnt.bigDecimal("teaCancelacionAnt").toString()));
        response.set("tasaCancAnt", Double.valueOf(responseCanAnt.bigDecimal("tasaCancelacionAnt").toString()));
        response.set("montoCancAnt", Double.valueOf(responseCanAnt.bigDecimal("monto").toString()));
        response.set("interesCancAnt", Double.valueOf(responseCanAnt.bigDecimal("interesCancelacionAnt").toString()));
        response.set("fechaVenc", responseCanAnt.string("fechaFinCanAnt"));
        if(estadoCancelacion != null && !estadoCancelacion.isEmpty()
                && !estadoCancelacion.equals("no_encontrado")) response.set("estadoCancAnt", estadoCancelacion);
        return response;
    }

    public static Objeto getEstadoCancelacionAnticipada(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idPF = contexto.parametros.string("idPlazoFijo");
            if(idPF.isEmpty()) return RespuestaTAS.sinParametros(contexto, "uno o mas parametros no ingresados");
            Objeto cancelacionAnticipadaResponse = TASRestPlazosFijos.getEstadoCancelacionAnticipada(contexto, idPF);
            if (cancelacionAnticipadaResponse.isEmpty()) return RespuestaTAS.sinResultados(contexto, "no se encontro estado");
            return cancelacionAnticipadaResponse;
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASPlazosFijosController - getEstadoCancelacionAnt()",e);
        }
    }

    private static String obtenerEstadoCancelacion(Objeto estadoCancelacionResponse){
        String estadoCancelacion = "";
        String response = estadoCancelacionResponse.string("estado");
        switch (response){
            case "ACT":
                estadoCancelacion = "Activa";
                break;
            case "ANU":
                estadoCancelacion = "Anulada";
                break;
            case "CON":
                estadoCancelacion = "En Proceso";
                break;
            case "SOK":
                estadoCancelacion = "Cancelación procesada";
                break;
            case "SFA":
                estadoCancelacion = "Cancelación no procesada";
                break;
            default:
                estadoCancelacion = "no_encontrado";
                break;
        }
        return estadoCancelacion;
    }

    public static Objeto postCancelacionAnticipada(ContextoTAS contexto){
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idPF = contexto.parametros.string("idPlazoFijo");
            LogTAS.evento(contexto, "INICIO_SOLICITUD_CANCELACION_ANT", contexto.parametrosOfuscados());
            Objeto solicitudCancelacionResponse = TASRestPlazosFijos.postSolicitarCancelacionAnt(contexto, idPF);
            Objeto response = armarSolicitudCancelacionResponse(solicitudCancelacionResponse);
            LogTAS.evento(contexto, "FIN_SOLICITUD_CANCELACION_ANT", response);
            return response;
        } catch (Exception e){
            return RespuestaTAS.error(contexto, "TASPlazosFijosController - postCancelacionAnticipada()", e);
        }
    }

    private static Objeto armarSolicitudCancelacionResponse(Objeto solicitudResponse){
        Objeto response = new Objeto();
        response.set("permiteSolicitud", solicitudResponse.string("permiteSolicitud"));
        response.set("teaCancAnt", Double.valueOf(solicitudResponse.bigDecimal("teaCancelacionAnt").toString()));
        response.set("tasaCancAnt", Double.valueOf(solicitudResponse.string("tasaCancelacionAnt").toString()));
        response.set("montoCancAnt", Double.valueOf(solicitudResponse.string("monto").toString()));
        response.set("interesCancAnt", Double.valueOf(solicitudResponse.string("interesCancelacionAnt").toString()));
        Date fechaFin = new Fecha(solicitudResponse.string("fechaFinCanAnt"), "yyyy-MM-dd").fechaDate();
        response.set("fechaVenc", fechaFin);
        return response;
    }
    
    public static Objeto getPlazoFijoDetalle(ContextoTAS contexto){
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idPF = contexto.parametros.string("idPlazoFijo");
            Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "dd/MM/yyyy", new Fecha(new Date()));
            Fecha fechaHasta = contexto.parametros.fecha("fechaDesde", "dd/MM/yyyy", new Fecha(new Date()));
            Objeto plazoFijoDetalleResponse = TASRestPlazosFijos.getPlazoFijoDetalle(contexto, idCliente, fechaDesde, fechaHasta, idPF);
                    return plazoFijoDetalleResponse.isEmpty() ?
                    RespuestaTAS.sinResultados(contexto, "detalle no encontrado")
            : plazoFijoDetalleResponse.objetos().get(0);
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASPlazosFijosController - getPlazoFijoDetalle()", e);
        }
    }

    public static Objeto getTiposPFLogros(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            Objeto tiposPFResponse = TASRestCatalogo.getTiposPFAhorro(contexto, idCliente, "Q", "1");
            if(tiposPFResponse == null) return RespuestaTAS.sinResultados(contexto, "Sin resultados para ese cliente");
            return new Objeto().set("tipos", armarResponseTiposPFLogros(contexto, tiposPFResponse));
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASPlazosFijosController - getTiposPFLogros()", e);
        }
    }

    private static Objeto armarResponseTiposPFLogros(ContextoTAS contexto, Objeto tipoPFResponse){
        Objeto response = new Objeto();        
        for(Objeto tipoPF : tipoPFResponse.objetos()){
            Objeto tipoPFParsed = new Objeto();
            int moneda = tipoPF.integer("moneda");
            tipoPFParsed.set("codigoMoneda", UtilesPlazosFijos.stringMoneda(moneda));
            double tasa = Double.valueOf( tipoPF.bigDecimal("tasa") != null ? 
            tipoPF.bigDecimal("tasa").toString() : "0.0");
            tipoPFParsed.set("tasa", tasa);
            tipoPFParsed.set("plazos", tipoPF.string("plazo"));
            tipoPFParsed.set("codPlan", tipoPF.integer("secuencial"));
            tipoPFParsed.set("codigo", tipoPF.string("tipoPlazoFijo"));
            tipoPFParsed.set("descripcion", contexto.config.string("tas_plazofijo_"+tipoPF.string("tipoPlazoFijo")));

            response.add(tipoPFParsed);
        }
        return response;
    }

    public static Objeto getCabeceraPFLogros(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            Objeto responseApiCabecera = TASRestPlazosFijos.getCabeceraPFLogros(contexto, idCliente, 0, "Q", "2");
            int cantidad = responseApiCabecera.objetos().size();
            Objeto response = new Objeto();
            if (cantidad == 15){
                int idPlanAhorro = 0;
                while(cantidad == 15){
                    idPlanAhorro = responseApiCabecera.objetos().get(responseApiCabecera.objetos().size() - 1).integer("idPlanAhorro");    
                    responseApiCabecera = TASRestPlazosFijos.getCabeceraPFLogros(contexto, idCliente, idPlanAhorro, "Q", "2");
                    cantidad = responseApiCabecera.objetos().size();
                    response.add(responseApiCabecera);
                }
            }else{
                response = responseApiCabecera;
            }
            if(response instanceof ApiResponse){
                ApiResponse cabeceras = (ApiResponse) response;
                int codigo = cabeceras.codigoHttp;
                if(codigo == 204){
                    return RespuestaTAS.sinResultados(contexto, "Sin resultados para ese cliente");
                }
            }
            
            return new Objeto().set("cabecerasLogros", armarResponseCabecerasPFLogros(contexto, response));
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASPlazosFijosController - getCabeceraPFLogros()", e);
        }
    }

    private static Objeto armarResponseCabecerasPFLogros(ContextoTAS contexto, Objeto responseCabecerasApi) throws ParseException{
        Objeto response = new Objeto();
        for(Objeto cabeceraPF : responseCabecerasApi.objetos()){
            Objeto cabeceraPFParsed = new Objeto();
            cabeceraPFParsed.set("nombreLogro", cabeceraPF.string("nombre"));
            cabeceraPFParsed.set("idPlanAhorro", cabeceraPF.integer("idPlanAhorro"));
            cabeceraPFParsed.set("cuotas", cabeceraPF.integer("cantidadPlazos"));
            cabeceraPFParsed.set("diaDebito", cabeceraPF.integer("diaConstitucionPF"));
            cabeceraPFParsed.set("monto", Double.valueOf(cabeceraPF.bigDecimal("monto").toString()));
            String fechaVenc = cabeceraPF.string("vencimiento").replace("T", " ");          
            Date fechaVencimiento = new Fecha(fechaVenc, "yyyy-MM-dd").fechaDate();            
            cabeceraPFParsed.set("fechaVenc", new SimpleDateFormat("dd/MM/yyyy").format(fechaVencimiento));
            cabeceraPFParsed.set("estado", UtilesPlazosFijos.stringEstado(cabeceraPF.string("estado")));
            cabeceraPFParsed.set("cuenta", cabeceraPF.string("cuenta"));
            String moneda = UtilesPlazosFijos.stringMoneda(cabeceraPF.integer("moneda"));
            cabeceraPFParsed.set("moneda", moneda);
            cabeceraPFParsed.set("tipoCuenta", cabeceraPF.string("tipoCuenta"));
            cabeceraPFParsed.set("tipoPlazoFijo", cabeceraPF.string("tipoPlazoFijo"));
            String tyc = armarTyC(contexto, cabeceraPF, moneda);
            if(!tyc.isEmpty()) cabeceraPFParsed.set("termsYCond",tyc);
            response.add(cabeceraPFParsed);
        }
        return response;
    }

    private static String armarTyC(ContextoTAS contexto, Objeto cabeceraPF, String moneda){
        try{
        String fecha = cabeceraPF.string("fechaConstPlan").replace("T", " ");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date fechaConstitucion = sdf.parse(fecha);
        Objeto dataTyC = TASSqlPlazosFijos.getTerminosYCondiciones(contexto, fechaConstitucion);        
        if(dataTyC.string("estado").equals("OK") && !dataTyC.string("respuesta").isEmpty()){
            Objeto respuesta = dataTyC.objeto("respuesta");
            String terminosYCondiciones = respuesta.objetos().get(0).string("data");
            if(terminosYCondiciones != null) terminosYCondiciones = terminosYCondiciones.replace("_moneda_", moneda.equals("$") ? "Pesos" : "Dolares");
            return terminosYCondiciones;
        }else{
        return "";
        }
    }catch (Exception e){
        LogTAS.error(contexto, e);
        return "";
    }
    }

    public static Objeto postConstituirPFLogros(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            boolean flagSimulacion = contexto.parametros.bool("simulacion", false);

            Objeto responseApiConstitucion = flagSimulacion ? postSimulacionPFLogros(contexto, idCliente) : postConstitucionPFLogros(contexto, idCliente);
            if(responseApiConstitucion.string("estado").equals("SIN_PARAMETROS")) return RespuestaTAS.sinParametros(contexto, "uno o mas parametros no ingresados");
            if(responseApiConstitucion.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASPlazosFijosController - postConstituirPFLogros()", (Exception) responseApiConstitucion.get("error"));
            return responseApiConstitucion;
        }catch (Exception e){
            return RespuestaTAS.error(contexto, "TASPlazosFijosController - getCabeceraPFLogros()", e);
        }
    }

    private static Objeto postSimulacionPFLogros(ContextoTAS contexto, String idCliente){
        try {
            Objeto parametrosPFLogros = armarParametrosSimuladorPFLogros(contexto, idCliente);
            if(parametrosPFLogros.string("estado").equals("ERROR")) return new Objeto().set("estado", TASMensajesString.SIN_PARAMETROS.getTipoMensaje());
            LogTAS.evento(contexto, "INICIO_SIMULACION_PF_LOGROS", parametrosPFLogros);
            Objeto responseSimuladorPfLogros = TASRestPlazosFijos.postSimularPFLogros(contexto, parametrosPFLogros);
            Objeto primerCuota = new Objeto();
            for(Objeto datosPrimerCuota : responseSimuladorPfLogros.objetos()){
                if(datosPrimerCuota.integer("nroSecuencial") == 1) primerCuota = datosPrimerCuota;
            }
            int plazo = primerCuota.integer("diasPlazo");
            Objeto diaHabil = UtilesPlazosFijos.verificarDiaHabil(contexto,plazo);
            if(diaHabil.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASPlazoFijoController - verificarDiaHabil()", (Exception) diaHabil.get("error"));
            plazo = diaHabil.string("estado").equals("true") ? plazo : diaHabil.integer("nuevoPlazo");
            String fechaConstTeorica = primerCuota.string("fechaConstiTeo");
            int nroCuota = primerCuota.integer("nroSecuencial");
            
            Objeto parametrosSimuladorPF = armarParametrosSimuladorPF(contexto, parametrosPFLogros, idCliente, plazo);
            Objeto responseSimuladorPF = TASRestPlazosFijos.postSimuladorPFLogros(contexto, parametrosSimuladorPF);
            
            Objeto response = new Objeto();
            response.set("plazoFijoLogros", armarResponseSimuladorPFLogros(responseSimuladorPF, parametrosPFLogros, nroCuota, fechaConstTeorica));
            response.set("cuotasPlazoFijo", armarDatosCuotasPFLogros(responseSimuladorPfLogros));
            
            String tyc = armarTyC(contexto, new Objeto().set("fechaConstPlan", fechaConstTeorica), parametrosPFLogros.string("moneda"));
            if(!tyc.isEmpty()) response.set("termsYCond",tyc);
            LogTAS.evento(contexto, "FIN_SIMULACION_PF_LOGROS", response);
            return response;
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    private static Objeto armarParametrosSimuladorPFLogros(ContextoTAS contexto, String idCliente){
        try {
            Objeto parametros = new Objeto();
            parametros.set("codCliente", idCliente);
            parametros.set("cuenta", contexto.parametros.string("numeroCuenta"));
            parametros.set("tipoCuenta", contexto.parametros.string("tipoCuenta"));
            parametros.set("moneda", contexto.parametros.string("idMoneda"));
            parametros.set("monto", contexto.parametros.string("importe"));
            parametros.set("nombre", "TAS"+idCliente+contexto.parametros.string("numeroLogro"));
            parametros.set("planContratado", contexto.parametros.string("codPlan"));
            parametros.set("tipoPlazoFijo", contexto.parametros.string("codPlazoFijo"));
            parametros.set("cuota", contexto.parametros.string("cuota"));
            parametros.set("dia", contexto.parametros.string("diaDebito"));            
            parametros.set("opcion", contexto.parametros.string("opcion", "S"));
            parametros.set("operacion", contexto.parametros.string("operacion", "S"));        
            parametros.set("estado", "OK");
            return parametros;
        } catch (Exception e) {
            return new Objeto().set("estado", "ERROR");
        }
        
    }

    private static Objeto armarParametrosSimuladorPF(ContextoTAS contexto, Objeto parametroPFLogro, String idCliente,int plazo){
        Objeto param = new Objeto();
        param.set("idCliente", idCliente);
        param.set("moneda", parametroPFLogro.string("moneda"));
        param.set("monto", parametroPFLogro.string("monto"));
        param.set("cuenta", parametroPFLogro.string("cuenta"));
        param.set("plazo", plazo);
        param.set("tipoCuenta", parametroPFLogro.string("tipoCuenta"));
        param.set("tipoOperacion", parametroPFLogro.string("tipoPlazoFijo"));
        String canalStr = contexto.config.string("tas_plazofijo_canal");
        int canal = (canalStr == null || canalStr.isEmpty()) ? 5 : Integer.valueOf(canalStr);
        param.set("canal", canal);;
        return param;
    }

    private static Objeto armarDatosCuotasPFLogros(Objeto responseSimuladorPF){
        Objeto response = new Objeto();
        for(Objeto cuota : responseSimuladorPF.objetos()){
            Objeto cuotaParsed = new Objeto();
            cuotaParsed.set("nroCuota", cuota.integer("nroSecuencial"));
            cuotaParsed.set("fechaDebito", cuota.string("fechaConstiTeo"));
            response.add(cuotaParsed);
        }
        return response;
    }

    private static Objeto armarResponseSimuladorPFLogros(Objeto responseApi, Objeto parametrosPFLogros, int nroCuota, String fechaConstTeorica){
        Objeto response = new Objeto();
        Objeto responseSimulador = responseApi.objetos().get(0);
        response.set("importe",Double.valueOf(responseSimulador.bigDecimal("capital").toString()));
        response.set("numeroCuenta",responseSimulador.string("cuentaADebitar"));
        response.set("numeroCuotas",responseSimulador.string("cuotas", null));
        String diaPago = responseSimulador.string("diaDePago");
        response.set("diaDePago", diaPago);
        Date fechaActual = new Fecha(responseSimulador.string("fechaActual"), "yyy-MM-dd").FechaDate();
        response.set("fechaOperacion", new SimpleDateFormat("dd/MM/yyyy").format(fechaActual));
        Date fechaPagoInteres = new Fecha(responseSimulador.string("fechaPagoInteres"), "yyy-MM-dd").FechaDate();
        response.set("fechaPagoIntereses", new SimpleDateFormat("dd/MM/yyyy").format(fechaPagoInteres));
        Date fechaVencimiento = new Fecha(responseSimulador.string("fechaVencimiento"), "yyy-MM-dd").FechaDate();
        response.set("fechaVencimiento", new SimpleDateFormat("dd/MM/yyyy").format(fechaVencimiento));
        response.set("impuestos", Double.valueOf(responseSimulador.bigDecimal("impuestosAPagar").toString()));
        response.set("sellos",Double.valueOf(responseSimulador.bigDecimal("sellos").toString()));
        response.set("interesEstimado",Double.valueOf(responseSimulador.bigDecimal("interesEstimado").toString()));
        response.set("codigoMoneda",responseSimulador.integer("moneda"));
        response.set("montoEstimado",Double.valueOf(responseSimulador.bigDecimal("montoTotal").toString()));
        response.set("plazo",responseSimulador.integer("plazo"));
        response.set("tasa",Double.valueOf(responseSimulador.bigDecimal("tasa").toString()));
        response.set("codigoTipo", parametrosPFLogros.string("planContratado"));
        response.set("descripcionTipo", responseSimulador.string("producto"));
        response.set("totalInteresEstimado",Double.valueOf(responseSimulador.bigDecimal("totalInteresEstimado").toString()));
        response.set("cubiertoPorGarantia",responseSimulador.string("gtiaDeDepositos").equals("S") ? "SI" : "NO");
        response.set("cuota", nroCuota);
        response.set("nombreLogro", parametrosPFLogros.string("nombre"));
        response.set("diaDebito", fechaConstTeorica);
        
        return response;
    }

    private static Objeto postConstitucionPFLogros(ContextoTAS contexto, String idCliente){
        try {
            Objeto parametrosAltaPfLogros = armarParametrosAltaPFLogros(contexto, idCliente);
            if(parametrosAltaPfLogros.string("estado").equals("ERROR")) return new Objeto().set("estado", TASMensajesString.SIN_PARAMETROS.getTipoMensaje());
            LogTAS.evento(contexto, "INICIO_ALTA_PF_LOGROS", parametrosAltaPfLogros);
            Objeto responseApiAltaPFLogros = TASRestPlazosFijos.postAltaPFLogros(contexto, parametrosAltaPfLogros);
            if(responseApiAltaPFLogros.string("estado").equals("ERROR")) return responseApiAltaPFLogros;
            Objeto responseAltaPFLogros = responseApiAltaPFLogros.objeto("respuesta");
            int plazo = responseAltaPFLogros.objetos().get(0).integer("diasPlazo");
            int secuencial = responseAltaPFLogros.objetos().get(0).integer("secuencial");
            
            Objeto diaHabil = UtilesPlazosFijos.verificarDiaHabil(contexto,plazo);
            if(diaHabil.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASPlazoFijoController - verificarDiaHabil()", (Exception) diaHabil.get("error"));
            plazo = diaHabil.string("estado").equals("true") ? plazo : diaHabil.integer("nuevoPlazo");
            
            Objeto parametrosAltaPF = armarParametrosAltaPF(contexto, parametrosAltaPfLogros, idCliente, plazo, secuencial);
            Objeto responseAPIAltaPF = TASRestPlazosFijos.postConstitucionPF(contexto, parametrosAltaPF);
            if(responseAPIAltaPF.string("estado"). equals("ERROR")) return armarResponseError(contexto, responseAPIAltaPF);
            Objeto responseAltaPF = responseAPIAltaPF.objeto("respuesta");
            Objeto response = new Objeto();
            response.set("ticket", responseAltaPF.string("idOperacion"));
            response.set("plazoFijoLogros", armarResponseAltaPFLogros(contexto, responseAltaPF, parametrosAltaPfLogros));
            
            Fecha fecha = new Fecha(new Date());
            String fechaActual = fecha.string("yyyy-MM-dd HH:mm:ss");
            fechaActual = fechaActual.replace(" ", "T");
            String tyc = armarTyC(contexto, new Objeto().set("fechaConstPlan", fechaActual), parametrosAltaPF.string("moneda"));
            if(!tyc.isEmpty()) response.set("termsYCond",tyc);
            LogTAS.evento(contexto, "FIN_ALTA_PF_LOGROS", response);
            return response;
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    private static Objeto armarParametrosAltaPFLogros(ContextoTAS contexto, String idCliente){
        try{
            Objeto params = new Objeto();
            params.set("codCliente", idCliente);
            params.set("cuenta", contexto.parametros.string("numeroCuenta"));
            params.set("cuota", contexto.parametros.string("cuota"));
            params.set("dia", contexto.parametros.string("diaDebito"));
            params.set("moneda", contexto.parametros.string("idMoneda"));
            params.set("monto", contexto.parametros.string("importe"));
            params.set("nombre", contexto.parametros.string("numeroLogro"));
            params.set("planContratado", contexto.parametros.string("codPlan"));
            params.set("tipoCuenta", contexto.parametros.string("tipoCuenta"));
            params.set("opcion", contexto.parametros.string("opcion", "A"));
            params.set("operacion", contexto.parametros.string("operacion", "A"));
            String canalStr = contexto.config.string("tas_plazofijo_canal");
            int canal = (canalStr == null || canalStr.isEmpty()) ? 5 : Integer.valueOf(canalStr);
            params.set("canal", canal);
            params.set("tipoPlazoFijo", contexto.parametros.string("codPlazoFijo"));
            params.set("descPF", contexto.config.string("tas_plazofijo_"+contexto.parametros.string("codPlazoFijo")));
            params.set("estado", "OK");
            return params;
        }catch (Exception e){
            return new Objeto().set("estado", "ERROR");
        }        
    }

    private static Objeto armarParametrosAltaPF(ContextoTAS contexto,Objeto parametrosAltaPfLogros,String idCliente,int plazo,int secuencial){
        Objeto params = new Objeto();
        params.set("canal", parametrosAltaPfLogros.integer("canal"));
        params.set("capInteres", "N");
        params.set("cuenta", parametrosAltaPfLogros.string("cuenta"));
        params.set("idCliente", idCliente);
        params.set("moneda", parametrosAltaPfLogros.string("moneda"));
        params.set("monto", parametrosAltaPfLogros.string("monto"));
        params.set("periodo", 0);
        params.set("plazo", plazo);
        params.set("renova", "N");
        params.set("tipoCuenta", parametrosAltaPfLogros.string("tipoCuenta"));
        params.set("tipoOperacion", parametrosAltaPfLogros.string("tipoPlazoFijo"));
        params.set("usuarioAlta", idCliente);
        params.set("idPlanAhorro", secuencial);
        return params;
    }

    private static Objeto armarResponseAltaPFLogros(ContextoTAS contexto, Objeto responseAlta, Objeto parametrosAlta){
        Objeto response = new Objeto();
        response.set("numeroTicket", responseAlta.string("idOperacion"));
        double importe = Double.valueOf(responseAlta.bigDecimal("capital").toString());
        response.set("importe", importe);
        response.set("numeroCuenta", responseAlta.string("cuenta"));
        response.set("numeroCuotas", responseAlta.string("cuotas"));
        String diaPago = responseAlta.string("diaDePago");
        response.set("diaDePago", diaPago);
        Date fechaOperacion = new Fecha(responseAlta.string("fechaActual"), "yyyy-MM-dd").FechaDate();
        response.set("fechaOperacion", new SimpleDateFormat("dd/MM/yyyy").format(fechaOperacion));
        Date fechaPagoInteres = new Fecha(responseAlta.string("fechaPagoIntereses"), "yyyy-MM-dd").FechaDate();
        response.set("fechaPagoIntereses",new SimpleDateFormat("dd/MM/yyyy").format(fechaPagoInteres));
        Date fechaVencimiento = new Fecha(responseAlta.string("fechaVencimiento"), "yyyy-MM-dd").FechaDate();
        response.set("fechaVencimiento", new SimpleDateFormat("dd/MM/yyyy").format(fechaVencimiento));
        response.set("impuestos", Double.valueOf(responseAlta.bigDecimal("impuestos").toString()));
        response.set("interesEstimado", Double.valueOf(responseAlta.bigDecimal("interesEstimado").toString()));
        response.set("codigoMoneda", responseAlta.integer("moneda"));
        response.set("montoEstimado", Double.valueOf(responseAlta.bigDecimal("monto").toString()));
        response.set("numeroPlazoFijo", responseAlta.string("nroPlazoFijo"));
        response.set("plazo", responseAlta.integer("plazo"));
        response.set("tasa", Double.valueOf(responseAlta.bigDecimal("tasa").toString()));
        response.set("codigoTipo", responseAlta.string("tipoOperacion"));
        response.set("descripcionTipo", parametrosAlta.string("descPF"));
        response.set("totalInteresEstimado", Double.valueOf(responseAlta.bigDecimal("totalInteresEstimado").toString()));
        response.set("cubiertoPorGarantia", responseAlta.string("cubiertoPorGarantia").equals("S") ? "SI" : "NO");
        return response;
    }

    public static Objeto getDetalleCuotasPFLogros(ContextoTAS contexto){
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            int planContratado = contexto.parametros.integer("codPlan", -1);
            if(planContratado == -1) return RespuestaTAS.sinParametros(contexto, "uno o mas parametros no ingresados");
            Objeto responseApiDetalleCuotas = TASRestPlazosFijos.getDetalleCuotasPFLogros(contexto, idCliente, planContratado);
            int cantidad = responseApiDetalleCuotas.objetos().size();
            Objeto response = new Objeto();
            if (cantidad == 15){
                int secuencial = 0;
                while(cantidad == 15){
                    secuencial = responseApiDetalleCuotas.objetos().get(responseApiDetalleCuotas.objetos().size() - 1).integer("secuencial");
                    responseApiDetalleCuotas = TASRestPlazosFijos.getDetalleCuotasPFLogros(contexto, idCliente, planContratado);
                    cantidad = responseApiDetalleCuotas.objetos().size();
                    response.add(responseApiDetalleCuotas);
                }
            }else{
                response = responseApiDetalleCuotas;
            }
            if(response instanceof ApiResponse){
                ApiResponse detalles = (ApiResponse) response;
                int codigo = detalles.codigoHttp;
                if(codigo == 204){
                    return RespuestaTAS.sinResultados(contexto, "Sin resultados para ese cliente");           
                }
            }
            return new Objeto().set("detallesCuotas", armarResponseDetalleCuotasPFLogros(contexto, response));
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASPlazosFijosController - getDetalleCuotasPFLogros()", e);
        
        }
    }

    private static Objeto armarResponseDetalleCuotasPFLogros(ContextoTAS contexto, Objeto responseApi){
        Objeto response = new Objeto();
        Date fechaConstitucion = null;
        Date fechaVencimiento = null;
        for(Objeto detalleCuota : responseApi.objetos()){
            Objeto detalleCuotaParsed = new Objeto();
            double monto = Double.valueOf(detalleCuota.string("monto", "0.00"));
            detalleCuotaParsed.set("nroCuota", detalleCuota.integer("secuencial"));
            detalleCuotaParsed.set("monto", monto);
            if(detalleCuota.string("fechaVen") != null){
            String fechaVenc = detalleCuota.string("fechaVen").replace("T", " ");
            fechaVencimiento = new Fecha(fechaVenc, "yyyy-MM-dd").fechaDate();
            detalleCuotaParsed.set("fechaVenc", new SimpleDateFormat("dd/MM/yyyy").format(fechaVencimiento));
            }
            detalleCuotaParsed.set("estado", detalleCuota.string("estado"));
            if(detalleCuota.string("fechaConstiTeo") != null){
            String fechaConst = detalleCuota.string("fechaConstiTeo").replace("T", " ");    
            fechaConstitucion = new Fecha(fechaConst, "yyyy-MM-dd").fechaDate();
            detalleCuotaParsed.set("fechaConst", new SimpleDateFormat("dd/MM/yyyy").format(fechaConstitucion));
            }
            double interes = Double.valueOf(detalleCuota.string("montoInteres", "0.00"));
            double impuestos = Double.valueOf(detalleCuota.string("montoImpuestos", "0.00"));
            detalleCuotaParsed.set("montoInteres", interes);
            detalleCuotaParsed.set("montoImpuestos", impuestos);
            detalleCuotaParsed.set("montoVencimiento", Double.valueOf(monto + interes - impuestos));
            detalleCuotaParsed.set("tasa", Double.valueOf(detalleCuota.string("tasa", "0.00")));
            detalleCuotaParsed.set("garDep", detalleCuota.string("garantizado"));
            detalleCuotaParsed.set("nroCert", detalleCuota.string("nroCertificado"));
            if(detalleCuota.string("estado").equals("A") && fechaConstitucion != null && fechaVencimiento != null){
                detalleCuotaParsed.set("plazo", calcularPlazo(fechaConstitucion, fechaVencimiento));
            }
            
            response.add(detalleCuotaParsed);
        }
        return response;
    }

    private static long calcularPlazo(Date fechaConstitucion, Date fechaVencimiento) {
    long plazo = fechaVencimiento.getTime() - fechaConstitucion.getTime();
    return TimeUnit.DAYS.convert(plazo, TimeUnit.MILLISECONDS);
}

public static Objeto getForzadoCuotaPFLogros(ContextoTAS contexto){
    try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");

            int planContratado = contexto.parametros.integer("codPlan", -1);
            int secuencial = contexto.parametros.integer("secuencial", -1);
            if(planContratado == -1 || secuencial == -1) return RespuestaTAS.sinParametros(contexto, "uno o mas parametros no ingresados");
            
            Objeto responseApiForzadoCuota = TASRestPlazosFijos.getForzadoCuotaPFLogros(contexto, idCliente, planContratado, secuencial);
            if(responseApiForzadoCuota instanceof ApiResponse){
                ApiResponse detalles = (ApiResponse) responseApiForzadoCuota;
                int codigo = detalles.codigoHttp;
                if(codigo == 204){
                    return RespuestaTAS.sinResultados(contexto, "Sin resultados para ese cliente");           
                }
            }
            return responseApiForzadoCuota;
    } catch (Exception e) {
        return RespuestaTAS.error(contexto, "TASPlazosFijosController - getForzadoCuotaPFLogros()", e);
    }
}

public static Objeto getBajaPFLogros(ContextoTAS contexto){
    try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            int planContratado = contexto.parametros.integer("codPlan", -1);
            if(planContratado == -1) return RespuestaTAS.sinParametros(contexto, "uno o mas parametros no ingresados");
            Objeto responseApiBajaPF = TASRestPlazosFijos.getBajaPFLogros(contexto, idCliente, planContratado);
            if(responseApiBajaPF instanceof ApiResponse){
                ApiResponse detalles = (ApiResponse) responseApiBajaPF;
                int codigo = detalles.codigoHttp;
                if(codigo == 204){
                    return RespuestaTAS.sinResultados(contexto, "Sin resultados para ese cliente");           
                }
            }
        return responseApiBajaPF;
    } catch (Exception e) {
        return RespuestaTAS.error(contexto, "TASPlazosFijosController - getBajaPFLogros()", e);
    }
}

public static Objeto getModificarPFLogros(ContextoTAS contexto){
    try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            Objeto parametrosModificacionPF = armarParametrosModificacionPF(contexto, idCliente);
            if(parametrosModificacionPF.string("estado").equals("ERROR")) return RespuestaTAS.sinParametros(contexto, "uno o mas parametros no ingresados");
            
            Objeto responseApiModificacionPF = TASRestPlazosFijos.getModificarPFLogros(contexto, parametrosModificacionPF);
            if(responseApiModificacionPF instanceof ApiResponse){
                ApiResponse detalles = (ApiResponse) responseApiModificacionPF;
                int codigo = detalles.codigoHttp;
                if(codigo == 204){
                    return RespuestaTAS.sinResultados(contexto, "Sin resultados para ese cliente");           
                }
            }
        return responseApiModificacionPF;
    } catch (Exception e) {
        return RespuestaTAS.error(contexto, "TASPlazosFijosController - getMdificarPFLogros()", e);
    }
}

private static Objeto armarParametrosModificacionPF(ContextoTAS contexto, String idCliente){
    try {
        Objeto params = new Objeto();
        params.set("idCobis", idCliente);
        params.set("cuenta", contexto.parametros.string("numeroCuenta"));
        params.set("moneda", contexto.parametros.string("idMoneda"));
        params.set("monto", contexto.parametros.string("importe"));
        params.set("nombre", contexto.parametros.string("numeroLogro"));
        params.set("planContratado", contexto.parametros.string("codPlan"));
        params.set("tipoCuenta", contexto.parametros.string("tipoCuenta"));
        params.set("estado", "OK");
        return params;
    } catch (Exception e) {
        return new Objeto().set("estado", "ERROR");
    }
}
}
