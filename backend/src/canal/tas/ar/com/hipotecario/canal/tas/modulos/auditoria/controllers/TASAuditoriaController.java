package ar.com.hipotecario.canal.tas.modulos.auditoria.controllers;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.conector.api.Api.Log;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.TASAuditoria;
import ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.TASAuditoriaAdminContingencia;
import ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.TASTicketSingleton;
import ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.reversas.TASDatosDepositoReversa;
import ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.reversas.TASDatosPagoPrestamo;
import ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.reversas.TASDatosPagoPrestamoReversa;
import ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.reversas.TASDatosPagoTarjetaReversa;
import ar.com.hipotecario.canal.tas.modulos.auditoria.services.TASRestAuditoria;
import ar.com.hipotecario.canal.tas.modulos.auditoria.services.sql.TASSqlAuditoria;
import ar.com.hipotecario.canal.tas.modulos.auditoria.utils.UtilesAuditoria;
import ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.service.TasSqlDepositoEfectivo;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKiosco;
import ar.com.hipotecario.canal.tas.shared.utils.models.strings.TASMensajesString;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;

import ar.com.hipotecario.canal.tas.modulos.auditoria.ticket.Ticket2PDF;
import ar.com.hipotecario.canal.tas.modulos.auditoria.ticket.Converter;
import ar.com.hipotecario.canal.tas.modulos.auditoria.ticket.Ticket2PDFException;
import ar.com.hipotecario.canal.tas.modulos.auditoria.ticket.TicketFile;

public class TASAuditoriaController {

    public static Objeto consultaAuditoria(ContextoTAS contexto) {
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idTas = contexto.parametros.string("tasId");
            String tipoCierre = contexto.parametros.string("tipoCierre");
            TASKiosco kiosco = contexto.getKioscoContexto(Integer.valueOf(idTas));
            if(kiosco == null) return RespuestaTAS.sinResultados(contexto, "Kiosco no encontrado");

            boolean esAdmin = UtilesAuditoria.verificarAdministrador(contexto, clienteSesion, kiosco);
            if(!esAdmin) return RespuestaTAS.sinResultados(contexto, "Usuario Admin no encontrado");

            String precinto1Inicial1 = "00000001";
            String precinto1Inicial2 = "00000000";

            Objeto loteActual = UtilesAuditoria.getLoteActual(contexto, kiosco);
            if(loteActual.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASAuditoriaController - getLoteActual()", (Exception) loteActual.get("error"));
            String nroLoteActual = loteActual.string("nroLote");

            TASAuditoria datosCierre = UtilesAuditoria.getDatosCierre(contexto, idTas);
            if(datosCierre.getError() != null || datosCierre == null) LogTAS.evento(contexto, "KioscoID " +idTas+" Cierre Inicial");
            boolean cierreInicial = datosCierre.getPrecinto1() == null && datosCierre.getPrecinto2() == null;
            Fecha fechaUltimaAuditoria = null;
            Fecha fechaUltimaAuditoriaVuelco = null;
            if(cierreInicial){
                fechaUltimaAuditoria = UtilesAuditoria.getFechaUltimaAuditoria(contexto, kiosco, tipoCierre, precinto1Inicial1, precinto1Inicial2, nroLoteActual);
                if(fechaUltimaAuditoria == null) return RespuestaTAS.error(contexto, "TASAuditoriaController - getFechaUltimaAuditoria");
            }else {
                fechaUltimaAuditoria = UtilesAuditoria.getFechaUltimaAuditoria(contexto, kiosco, tipoCierre);
                fechaUltimaAuditoriaVuelco = UtilesAuditoria.getFechaUltimaAuditoriaVuelco(contexto, kiosco);
            }
            Objeto response = new Objeto();
            response.set("loteActual", nroLoteActual);
            response.set("ultimaAuditoria", datosCierre);
            response.set("fechaUltimaAuditoria", formatFecha(fechaUltimaAuditoria));
            response.set("fechaUltimaAuditoriaVuelco", cierreInicial || fechaUltimaAuditoriaVuelco == null ?  formatFecha2(fechaUltimaAuditoria) :  formatFecha2(fechaUltimaAuditoriaVuelco));
            return response;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto,"TASAuditoriaController - consultarAuditoria()", e);
        }
    }
    private static String formatFecha(Fecha fecha){
        Date fechaDate = fecha.fechaDate();
        SimpleDateFormat spd = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
       String fechaString = spd.format(fechaDate);
       return fechaString;
    }
    
    private static String formatFecha2(Fecha fecha){
        Date fechaDate = fecha.fechaDate();
        SimpleDateFormat spd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       String fechaString = spd.format(fechaDate);
       return fechaString;
    }

    public static Objeto ultimaFechaAuditoria(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idTas = contexto.parametros.string("tasId");
            TASKiosco kiosco = contexto.getKioscoContexto(Integer.valueOf(idTas));
            if(kiosco == null) return RespuestaTAS.sinResultados(contexto, "Kiosco no encontrado");
            Fecha fechaUltimaAuditoria = UtilesAuditoria.getFechaUltimaAuditoria(contexto, kiosco, "diario");
            if( fechaUltimaAuditoria == null) return RespuestaTAS.error(contexto, "TASAuditoriaController - ultimaFechaAuditoria()");
            return new Objeto().set("fechaUltimaAuditoria", formatFecha(fechaUltimaAuditoria));
        } catch (Exception e) {
            return RespuestaTAS.error(contexto,"TASAuditoriaController - ultimaFechaAuditoria()", e);
        }
    }

    public static Objeto cierreDBInteligente(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idTas = contexto.parametros.string("tasId");
            TASKiosco kiosco = contexto.getKioscoContexto(Integer.valueOf(idTas));
            if(kiosco == null) return RespuestaTAS.sinResultados(contexto, "Kiosco no encontrado");
            String tipoCierre = contexto.parametros.string("tipoCierre");
            String numeroPrecinto1 = contexto.parametros.string("numeroPrecinto1");
            String numeroPrecinto2 = contexto.parametros.string("numeroPrecinto2");
            int totalSobres = contexto.parametros.integer("totalSobres", 0);
            int cantRetenidos = contexto.parametros.integer("cantRetenidos", 0);
            LogTAS.evento(contexto, "INICIO_CIERRE_INTELIGENTE", new Objeto().set("kiosco", kiosco.getKioscoId()));

            boolean esAdmin = UtilesAuditoria.verificarAdministrador(contexto, clienteSesion, kiosco);
            if(!esAdmin) return RespuestaTAS.sinResultados(contexto, "Usuario Admin no encontrado");

            Objeto loteActual = UtilesAuditoria.getLoteActual(contexto, kiosco);
            if(loteActual.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASAuditoriaController - getLoteActual()", (Exception) loteActual.get("error"));
            String nroLoteActual = loteActual.string("nroLote");

            TASAuditoria datosCierre = UtilesAuditoria.getDatosCierre(contexto, idTas);
            if(datosCierre.getError() != null || datosCierre == null) LogTAS.evento(contexto, "KioscoID " +idTas+" Cierre Inicial");
            boolean cierreInicial = datosCierre.getLote() == null && datosCierre.getPrecinto1() == null && datosCierre.getPrecinto2() == null;

            //ultimo cierre (ultima auditoria)
            Fecha fechaUltimoCierre = null;
            if(cierreInicial){
                fechaUltimoCierre = UtilesAuditoria.getFechaUltimaAuditoria(contexto, kiosco, tipoCierre, numeroPrecinto1, numeroPrecinto2, nroLoteActual);
                if(fechaUltimoCierre == null) return RespuestaTAS.error(contexto, "TASAuditoriaController - getFechaUltimaAuditoria");
            }else {
                fechaUltimoCierre = UtilesAuditoria.getFechaUltimaAuditoria(contexto, kiosco, tipoCierre);
            }

            //parseo fecha cierre actual (auditoria actual)
            Date fechaHoy = new Date();
            Fecha fechaCierreActual = new Fecha(fechaHoy);
            fechaCierreActual.string("yyyy-MM-dd HH:mm:ss");

            cantRetenidos = cantRetenidos == 0 ? UtilesAuditoria.getCantBilletesRetenidos(contexto, kiosco,  fechaUltimoCierre.fechaDate(), fechaCierreActual.fechaDate()) : cantRetenidos;

            //DEPOSITOS
            //todo refactor buscar la manera de hacerlo mas armonico
            Objeto totalPesos = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"C", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"R","$","AHO");
            if(totalPesos.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalDolares = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"C", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"R","U$D","AHO");
            if(totalDolares.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalPesosError = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"C", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"Y","$","AHO");
            if(totalPesosError.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalDolaresError = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"C", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"Y","U$D","AHO");
            if(totalDolaresError.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalPesosAbortado = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"C", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"X","$","AHO");
            if(totalPesosAbortado.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalDolaresAbortado = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"C", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"X","U$D","AHO");
            if(totalDolaresAbortado.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalPesosCC = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"C", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"R","$","CTE");
            if(totalPesosCC.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalDolaresCC = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"C", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"R","U$D","CTE");
            if(totalDolaresCC.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalPesosErrorCC = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"C", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"Y","$","CTE");
            if(totalPesosErrorCC.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalDolaresErrorCC = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"C", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"Y","U$D","CTE");
            if(totalDolaresErrorCC.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalPesosAbortadoCC = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"C", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"X","$","CTE");
            if(totalPesosAbortadoCC.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalDolaresAbortadoCC = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"C", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"X","U$D","CTE");
            if(totalDolaresAbortadoCC.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));

            //obtengo "cantidad" de depositos y los sumo
            int cantDepositosOK = UtilesAuditoria.calcularCantTotales(totalPesos, totalDolares);
            int cantDepositosError = UtilesAuditoria.calcularCantTotales(totalPesosError, totalDolaresError);
            int cantDepositosAbortados = UtilesAuditoria.calcularCantTotales(totalPesosAbortado, totalDolaresAbortado);
            int cantDepositosCCOK = UtilesAuditoria.calcularCantTotales(totalPesosCC, totalDolaresCC);
            int cantDepositosCCError = UtilesAuditoria.calcularCantTotales(totalPesosErrorCC, totalDolaresErrorCC);
            int cantDepositosCCAbortados = UtilesAuditoria.calcularCantTotales(totalPesosAbortadoCC, totalDolaresAbortadoCC);

            //obtengo "importe"
            double montoTotalDepositosPesosOk = UtilesAuditoria.getTotalDeposito(totalPesos);
            double montoTotalDepositosDolaresOk = UtilesAuditoria.getTotalDeposito(totalDolares);
            double montoTotalDepositosPesosError = UtilesAuditoria.getTotalDeposito(totalPesosError);
            double montoTotalDepositosDolaresError = UtilesAuditoria.getTotalDeposito(totalDolaresError);
            double montoTotalDepositosPesosAbortado = UtilesAuditoria.getTotalDeposito(totalPesosAbortado);
            double montoTotalDepositosDolaresAbortado = UtilesAuditoria.getTotalDeposito(totalDolaresAbortado);
            double montoTotalDepositosPesosCCOk = UtilesAuditoria.getTotalDeposito(totalPesosCC);
            double montoTotalDepositosDolaresCCOk = UtilesAuditoria.getTotalDeposito(totalDolaresCC);
            double montoTotalDepositosPesosCCError = UtilesAuditoria.getTotalDeposito(totalPesosErrorCC);
            double montoTotalDepositosDolaresCCError = UtilesAuditoria.getTotalDeposito(totalDolaresErrorCC);
            double montoTotalDepositosPesosCCAbortado = UtilesAuditoria.getTotalDeposito(totalPesosAbortadoCC);
            double montoTotalDepositosDolaresCCAbortado = UtilesAuditoria.getTotalDeposito(totalDolaresAbortadoCC);
            

            //TARJETAS
            Objeto totalPesosTarjeta = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"T", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"R","$");
            if(totalPesos.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalDolaresTarjeta = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"T", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"R","U$D");
            if(totalDolares.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalPesosTarjetaError = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"T", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"Y","$");
            if(totalPesosError.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalDolaresTarjetaError = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"T", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"Y","U$D");
            if(totalDolaresError.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalPesosTarjetaAbortado = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"T", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"X","$");
            if(totalPesosAbortado.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalDolaresTarjetaAbortado = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"T", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"X","U$D");
            if(totalDolaresAbortado.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));

            //obtengo "cantidad" y lo sumo
            int cantDepositosTarjetaOK = UtilesAuditoria.calcularCantTotales(totalPesosTarjeta, totalDolaresTarjeta);
            int cantDepositosTarjetaError = UtilesAuditoria.calcularCantTotales(totalPesosTarjetaError, totalDolaresTarjetaError);
            int cantDepositosTarjetaAbortados = UtilesAuditoria.calcularCantTotales(totalPesosTarjetaAbortado, totalDolaresTarjetaAbortado);
            //obtengo "importe"
            double montoTotalDepositosTarjetaPesosOk = UtilesAuditoria.getTotalDeposito(totalPesosTarjeta);
            double montoTotalDepositosTarjetaDolaresOk = UtilesAuditoria.getTotalDeposito(totalDolaresTarjeta);
            double montoTotalDepositosTarjetaPesosError = UtilesAuditoria.getTotalDeposito(totalPesosTarjetaError);
            double montoTotalDepositosTarjetaDolaresError = UtilesAuditoria.getTotalDeposito(totalDolaresTarjetaError);
            double montoTotalDepositosTarjetaPesosAbortado = UtilesAuditoria.getTotalDeposito(totalPesosTarjetaAbortado);
            double montoTotalDepositosTarjetaDolaresAbortado = UtilesAuditoria.getTotalDeposito(totalDolaresTarjetaAbortado);
            
            //PRESTAMOS
            Objeto totalPesosPrestamo = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"P", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"R","$");
            if(totalPesos.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalDolaresPrestamo = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"P", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"R","U$D");
            if(totalDolares.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalPesosPrestamoError = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"P", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"Y","$");
            if(totalPesosError.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalDolaresPrestamoError = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"P", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"Y","U$D");
            if(totalDolaresError.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalPesosPrestamoAbortado = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"P", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"X","$");
            if(totalPesosAbortado.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));
            Objeto totalDolaresPrestamoAbortado = TASSqlAuditoria.getTotalDepositos(contexto, kiosco.getKioscoId(),"P", fechaUltimoCierre.fechaDate(), fechaCierreActual.FechaDate(),"X","U$D");
            if(totalDolaresAbortado.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "no se pudo obtener depositos", (Exception) totalPesos.get("error"));

            //obtengo "cantidad" y lo sumo
            int cantDepositosPrestamoOK = UtilesAuditoria.calcularCantTotales(totalPesosPrestamo, totalDolaresPrestamo);
            int cantDepositosPrestamoError = UtilesAuditoria.calcularCantTotales(totalPesosPrestamoError, totalDolaresPrestamoError);
            int cantDepositosPrestamoAbortados = UtilesAuditoria.calcularCantTotales(totalPesosPrestamoAbortado, totalDolaresPrestamoAbortado);
            //obtengo "importe"
            double montoTotalDepositosPrestamoPesosOk = UtilesAuditoria.getTotalDeposito(totalPesosPrestamo);
            double montoTotalDepositosPrestamoDolaresOk = UtilesAuditoria.getTotalDeposito(totalDolaresPrestamo);
            double montoTotalDepositosPrestamoPesosError = UtilesAuditoria.getTotalDeposito(totalPesosPrestamoError);
            double montoTotalDepositosPrestamoDolaresError = UtilesAuditoria.getTotalDeposito(totalDolaresPrestamoError);
            double montoTotalDepositosPrestamoPesosAbortado = UtilesAuditoria.getTotalDeposito(totalPesosPrestamoAbortado);
            double montoTotalDepositosPrestamoDolaresAbortado = UtilesAuditoria.getTotalDeposito(totalDolaresPrestamoAbortado);

            //TOTALES
            Objeto vuelco = new Objeto();
            vuelco.set("cantRetenidos",cantRetenidos);
            vuelco.set("cuentaInteligenteOKPesos",montoTotalDepositosPesosOk);
            vuelco.set("cuentaInteligenteErrorPesos",montoTotalDepositosPesosError);
            vuelco.set("cuentaInteligenteAbortadaPesos",montoTotalDepositosPesosAbortado);
            vuelco.set("cuentaInteligenteOKDolares",montoTotalDepositosDolaresOk);
            vuelco.set("cuentaInteligenteErrorDolares",montoTotalDepositosDolaresError);
            vuelco.set("cuentaInteligenteAbortadaDolares",montoTotalDepositosDolaresAbortado);

            vuelco.set("cuentaInteligenteOKPesosCC",montoTotalDepositosPesosCCOk);
            vuelco.set("cuentaInteligenteErrorPesosCC",montoTotalDepositosPesosCCError);
            vuelco.set("cuentaInteligenteAbortadaPesosCC",montoTotalDepositosPesosCCAbortado);
            vuelco.set("cuentaInteligenteOKDolaresCC",montoTotalDepositosDolaresCCOk);
            vuelco.set("cuentaInteligenteErrorDolaresCC",montoTotalDepositosDolaresCCError);
            vuelco.set("cuentaInteligenteAbortadaDolaresCC",montoTotalDepositosDolaresCCAbortado);


            vuelco.set("tarjetaPagoInteligenteOKPesos",montoTotalDepositosTarjetaPesosOk);
            vuelco.set("tarjetaPagoInteligenteErrorPesos",montoTotalDepositosTarjetaPesosError);
            vuelco.set("tarjetaPagoInteligenteAbortadaPesos",montoTotalDepositosTarjetaPesosAbortado);
            vuelco.set("tarjetaPagoInteligenteOKDolares",montoTotalDepositosTarjetaDolaresOk);
            vuelco.set("tarjetaPagoInteligenteErrorDolares",montoTotalDepositosTarjetaDolaresError);
            vuelco.set("tarjetaPagoInteligenteAbortadaDolares",montoTotalDepositosTarjetaDolaresAbortado);

            vuelco.set("prestamoPagoInteligenteOKPesos",montoTotalDepositosPrestamoPesosOk);
            vuelco.set("prestamoPagoInteligenteErrorPesos",montoTotalDepositosPrestamoPesosError);
            vuelco.set("prestamoPagoInteligenteAbortadaPesos",montoTotalDepositosPrestamoPesosAbortado);
            vuelco.set("prestamoPagoInteligenteOKDolares",montoTotalDepositosPrestamoDolaresOk);
            vuelco.set("prestamoPagoInteligenteErrorDolares",montoTotalDepositosPrestamoDolaresError);
            vuelco.set("prestamoPagoInteligenteAbortadaDolares",montoTotalDepositosPrestamoDolaresAbortado);

            //CANTIDADES
            vuelco.set("totalDepositosInteligentesOkCantidad", cantDepositosOK);
            vuelco.set("totalDepositosInteligentesErrorCantidad", cantDepositosError);
            vuelco.set("totalDepositosInteligentesAbortadoCantidad", cantDepositosAbortados);

            vuelco.set("totalDepositosInteligentesOkCantidadCC", cantDepositosCCOK);
            vuelco.set("totalDepositosInteligentesErrorCantidadCC", cantDepositosCCError);
            vuelco.set("totalDepositosInteligentesAbortadoCantidadCC", cantDepositosCCAbortados);

            vuelco.set("totalPagoTarjetaInteligentesOkCantidad", cantDepositosTarjetaOK);
            vuelco.set("totalPagoTarjetaInteligentesErrorCantidad", cantDepositosTarjetaError);
            vuelco.set("totalPagoTarjetaInteligentesAbortadoCantidad", cantDepositosTarjetaAbortados);

            vuelco.set("totalPagoPrestamoInteligentesOkCantidad", cantDepositosPrestamoOK);
            vuelco.set("totalPagoPrestamoInteligentesErrorCantidad", cantDepositosPrestamoError);
            vuelco.set("totalPagoPrestamoInteligentesAbortadoCantidad", cantDepositosPrestamoAbortados);

            //TOTALES - CANTIDAD
            vuelco.set("totalDepositosInteligentesCantidad",cantDepositosOK);
            vuelco.set("totalDepositosInteligentesCCCantidad",cantDepositosCCOK);
            vuelco.set("totalPagoTarjetaInteligentesCantidad",cantDepositosTarjetaOK);
            vuelco.set("totalPagoPrestamoInteligentesCantidad",cantDepositosPrestamoOK);

            //TOTALES - PESOS
            vuelco.set("totalPesos", montoTotalDepositosPesosOk);
            vuelco.set("totalPesosCC", montoTotalDepositosPesosCCOk);
            vuelco.set("totalPesosTarjeta",montoTotalDepositosTarjetaPesosOk);
            vuelco.set("totalPesosPrestamo", montoTotalDepositosPrestamoPesosOk);
            //TOTALES - DOLARES
            vuelco.set("totalDolares", montoTotalDepositosDolaresOk);
            vuelco.set("totalDolaresCC", montoTotalDepositosDolaresCCOk);
            vuelco.set("totalDolaresTarjeta", montoTotalDepositosTarjetaDolaresOk);
            vuelco.set("totalDolaresPrestamo", montoTotalDepositosPrestamoDolaresOk);

            //TOTALES - FILAS TOTALES
            int totalOperacionesCantidad = cantDepositosOK + cantDepositosCCOK + cantDepositosTarjetaOK + cantDepositosPrestamoOK;
            double totalOperacionesPesos = montoTotalDepositosPesosOk + montoTotalDepositosPesosCCOk + montoTotalDepositosTarjetaPesosOk + montoTotalDepositosPrestamoPesosOk;
            double totalOperacionesDolares = montoTotalDepositosDolaresOk + montoTotalDepositosDolaresCCOk + montoTotalDepositosTarjetaDolaresOk + montoTotalDepositosPrestamoDolaresOk;
            vuelco.set("totalOperacionesCantidad", totalOperacionesCantidad);
            vuelco.set("totalOperacionesPesos", totalOperacionesPesos);
            vuelco.set("totalOperacionesDolares", totalOperacionesDolares);
            LogTAS.evento(contexto, "INSERT_LOG_AUDITORIA_BBBDD", new Objeto().set("kiosco", kiosco.getKioscoId()));
            Objeto insertLogAuditoria = TASSqlAuditoria.insertLogAuditoria(contexto, kiosco, fechaUltimoCierre.fechaDate(), fechaCierreActual.fechaDate(), clienteSesion);
            if(insertLogAuditoria.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASAuditoriaController - insertLogAuditoria()", (Exception) insertLogAuditoria.get("error"));
            if(insertLogAuditoria.string("estado").equals("false")) LogTAS.evento(contexto, "No se pudo insertar el LOG en Auditoria");
            String precintoFormateado = "";
            int loteAnterior = 0;

            if(cierreInicial){
                precintoFormateado = "00000001-00000001";
                loteAnterior = 1;
            } else {
                precintoFormateado = datosCierre.getPrecinto1() + "-" + datosCierre.getPrecinto2();
                loteAnterior = Integer.valueOf(datosCierre.getLote());
            }
            LogTAS.evento(contexto, "UPDATE_LOTE_BBDD", new Objeto().set("kiosco", kiosco.getKioscoId()));
            Objeto responseUpdLote = TASSqlAuditoria.actualizarLote(contexto, kiosco);
            if(responseUpdLote.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASAuditoriaController - actualizarLote()", (Exception) responseUpdLote.get("error"));
            if(responseUpdLote.string("estado").equals("false")) LogTAS.evento(contexto, "No se pudo actualziar el Lote Actual");
            LogTAS.evento(contexto, "UPDATE_AUDITORIA_BBBDD", new Objeto().set("kiosco", kiosco.getKioscoId()));
            Objeto responseUpdAuditoria = TASSqlAuditoria.updateAuditoria(contexto, kiosco, fechaCierreActual.fechaDate(), numeroPrecinto1, numeroPrecinto2, nroLoteActual, tipoCierre);
            if(responseUpdAuditoria.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASAuditoriaController - updateAuditoria()", (Exception) responseUpdAuditoria.get("error"));
            if(responseUpdAuditoria.string("estado").equals("false")) LogTAS.evento(contexto, "No se pudo actualizar la Auditoria actual");
            LogTAS.evento(contexto, "UPDATE_LOG_AUDITORIA_BBBDD", new Objeto().set("kiosco", kiosco.getKioscoId()));
            Objeto updateLogAuditoria = TASSqlAuditoria.updateLogAuditoria(contexto, kiosco, numeroPrecinto1, numeroPrecinto2, nroLoteActual, tipoCierre);
            if(updateLogAuditoria.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASAuditoriaController - updateLogAuditoria()", (Exception) updateLogAuditoria.get("error"));
            if(updateLogAuditoria.string("estado").equals("false")) LogTAS.evento(contexto, "No se pudo insertar el LOG en Auditoria");
            LogTAS.evento(contexto, "FIN_CIERRE_INTELIGENTE", new Objeto().set("kiosco", kiosco.getKioscoId()));
            return armarRespuestaAuditoria(kiosco, precintoFormateado, loteAnterior, fechaCierreActual, cantRetenidos, vuelco);
        }catch (Exception e) {
            return RespuestaTAS.error(contexto,"TASAuditoriaController - cierreDBInteligente()", e);
        }
    }

    private static Objeto armarRespuestaAuditoria(TASKiosco kiosco,String precintoFormateado,int loteAnterior,Fecha fechaCierreActual,int cantRetenidos,Objeto vuelco){
        Objeto response = new Objeto();
        response.set("tas", kiosco.getKioscoNombre());
        response.set("precintoAnteriorFormateado", precintoFormateado);
        response.set("loteAnterior", loteAnterior);
        response.set("fechaAuditoria", formatFecha(fechaCierreActual));
        response.set("cantRetenidos", cantRetenidos);
        response.set("vuelco", vuelco);
        return response;
    }

    public static Objeto reintentoOperacionesDBInteligente(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idTas = contexto.parametros.string("tasId");
            TASKiosco kiosco = contexto.getKioscoContexto(Integer.valueOf(idTas));
            if(kiosco == null) return RespuestaTAS.sinResultados(contexto, "Kiosco no encontrado");
            LogTAS.evento(contexto, "INICIO_REINTENTO_OPERACIONES_REVERSA", contexto.parametrosOfuscados());
            LogTAS.evento(contexto, "UPDATE_DEPOSITOS_ESTADO_A", new Objeto().set("kiosco", kiosco.getKioscoId()));
            Objeto updDepositoToReversado = TasSqlDepositoEfectivo.updateDepositoToReversado(contexto, kiosco.getKioscoId());
            if(updDepositoToReversado.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASAuditoriaController - updateDepositoToReversado()", (Exception) updDepositoToReversado.get("error"));
            if(updDepositoToReversado.string("estado").equals("false")) return RespuestaTAS.sinResultados(contexto, "No se encontraron depositos para reversar");

            String cantidadReintentosCierreDeposito = contexto.config.string("tas_cantidadReintentosCierreDepositoReversa");
		    String cantidadReintentosCierrePagoTarjetaReversa= contexto.config.string("tas_cantidadReintentosCierrePagoTarjetaReversa");
		    String cantidadReintentosCierrePagoPrestamo= contexto.config.string("tas_cantidadReintentosCierrePagoPrestamo");
		    String cantidadReintentosCierrePagoPrestamoReversa= contexto.config.string("tas_cantidadReintentosCierrePagoPrestamoReversa");
		    String cantidadReintentosCierreRegistroOperacion= contexto.config.string("tas_cantidadReintentosCierreRegistroOperacion");
		    String codigoRetornoTimeout = contexto.config.string("tas_codigoRetornoTimeout");
		    String codigoRetornoOperacionOk = contexto.config.string("tas_codigoRetornoOperacionOk");
            int cantidadReintentos = 0;
            int operacionesOK = 0;
            int operacionesFail = 0;

            TASAuditoriaAdminContingencia adminContingencia = UtilesAuditoria.procesarOperacionesDepositos(contexto, kiosco.getKioscoId(), codigoRetornoTimeout);
            Objeto response = new Objeto();
            if(adminContingencia.getDepositoReversasAReintentar() != null && adminContingencia.getDepositoReversasAReintentar().size() > 0){
                cantidadReintentos = Integer.valueOf(cantidadReintentosCierreDeposito);
                LogTAS.evento(contexto, "INICIO_PROCESO_REINTENTO_DEPOSITOS_REVERSA");
                for(TASDatosDepositoReversa depositoAReversar : adminContingencia.getDepositoReversasAReintentar()){
                    int intentos = 0;
                    boolean exito = false;
                    while (intentos < cantidadReintentos && !exito) {        
                    Objeto responseDepositoReversado = UtilesAuditoria.generarReversaDepositos(contexto, depositoAReversar, kiosco);
                    if(responseDepositoReversado.string("estado").equals("ERROR")){
                        Objeto estadoDepositoUpdResponse = TASSqlAuditoria.actualizarEstadoDeposito(contexto, depositoAReversar.getNumeroTicket(), depositoAReversar.getKioscoId(), "X");
                        if(estadoDepositoUpdResponse.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASAuditoriaController - updateLogAuditoria()", (Exception) estadoDepositoUpdResponse.get("error"));
                        if(estadoDepositoUpdResponse.string("estado").equals("false")) LogTAS.evento(contexto, "No se pudo actualizar el estado del deposito nro: " + depositoAReversar.getNumeroTicket());
                        operacionesFail++;
                    }
                    if(responseDepositoReversado.string("codigo").equals("200") || responseDepositoReversado.string("codigo").equals("202")){
                        Objeto codigoDepositoUpdResponse = TASSqlAuditoria.actualizarCodigoRetornoDeposito(contexto, depositoAReversar.getNumeroTicket(), codigoRetornoOperacionOk);
                        if(codigoDepositoUpdResponse.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASAuditoriaController - updateLogAuditoria()", (Exception) codigoDepositoUpdResponse.get("error"));
                        if(codigoDepositoUpdResponse.string("estado").equals("false")) LogTAS.evento(contexto, "No se pudo actualizar el Codigo de Retorno  del deposito nro: " + depositoAReversar.getNumeroTicket());
                        exito = true;
                        operacionesOK++;
                    }
                    intentos++;
                    }
                }
                response.set("depositoEfectivoReversas",adminContingencia.getDepositoReversasAReintentar().size());
                response.set("depositosEfectivoReversados", operacionesOK);
                response.set("depositosEfectivoReversaFallida", operacionesFail);
            }else{
                response.set("depositoEfectivoReversas",0);
                response.set("depositosEfectivoReversados", 0);
                response.set("depositosEfectivoReversaFallida", 0);
            }

            if(adminContingencia.getPagoTarjetaReversasAReintentar() != null && adminContingencia.getPagoTarjetaReversasAReintentar().size() > 0){
              cantidadReintentos = Integer.valueOf(cantidadReintentosCierrePagoTarjetaReversa);
              operacionesOK = 0;
              operacionesFail = 0;
              LogTAS.evento(contexto, "INICIO_PROCESO_REINTENTO_PAGO_TARJETA_REVERSA");
              for(TASDatosPagoTarjetaReversa pagoTarjetaAReversar : adminContingencia.getPagoTarjetaReversasAReintentar()){
                int intentos = 0;
                    boolean exito = false;
                    while (intentos < cantidadReintentos && !exito) {      
                Objeto responsePagoTarjetaReversada = UtilesAuditoria.generarReversaPagoTarjeta(contexto, pagoTarjetaAReversar, kiosco);
                  if(responsePagoTarjetaReversada.string("estado").equals("ERROR")){
                      Objeto estadoPagoTarjetaUpdResponse = TASSqlAuditoria.actualizarEstadoDeposito(contexto, pagoTarjetaAReversar.getNumeroTicket(), pagoTarjetaAReversar.getKioscoId(), "X");
                      if(estadoPagoTarjetaUpdResponse.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASAuditoriaController - updateLogAuditoria()", (Exception) estadoPagoTarjetaUpdResponse.get("error"));
                      if(estadoPagoTarjetaUpdResponse.string("estado").equals("false")) LogTAS.evento(contexto, "No se pudo actualizar el estado del deposito nro: " + pagoTarjetaAReversar.getNumeroTicket());
                      operacionesFail++;
                  }
                  if(responsePagoTarjetaReversada.string("codigo").equals("200") || responsePagoTarjetaReversada.string("codigo").equals("202")){
                      Objeto codigoDepositoUpdResponse = TASSqlAuditoria.actualizarCodigoRetornoDeposito(contexto, pagoTarjetaAReversar.getNumeroTicket(), codigoRetornoOperacionOk);
                      if(codigoDepositoUpdResponse.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASAuditoriaController - updateLogAuditoria()", (Exception) codigoDepositoUpdResponse.get("error"));
                      if(codigoDepositoUpdResponse.string("estado").equals("false")) LogTAS.evento(contexto, "No se pudo actualizar el Codigo de Retorno del deposito nro: " + pagoTarjetaAReversar.getNumeroTicket());
                      exito = true;
                      operacionesOK++;
                  }
                  intentos++;
                }
              }
                response.set("pagoTarjetaReversa",adminContingencia.getPagoTarjetaReversasAReintentar().size());
                response.set("pagoTarjetaReversadas",operacionesOK);
                response.set("pagoTarjetaReversaFallida",operacionesFail);
            }else{
                response.set("pagoTarjetaReversa",0);
                response.set("pagoTarjetaReversadas",0);
                response.set("pagoTarjetaReversaFallida",0);
            }

            if(adminContingencia.getPagosPrestamoAReintentar() != null && adminContingencia.getPagosPrestamoAReintentar().size() > 0){
                cantidadReintentos = Integer.valueOf(cantidadReintentosCierrePagoPrestamo);
                operacionesOK = 0;
                operacionesFail = 0;
                LogTAS.evento(contexto, "INICIO_PROCESO_REINTENTO_PAGO_PRESTAMO");
                for(TASDatosPagoPrestamo pagoPrestamo : adminContingencia.getPagosPrestamoAReintentar()){
                    int intentos = 0;
                    boolean exito = false;
                    while (intentos < cantidadReintentos && !exito) {    
                    Objeto pagoPrestamoResponse = UtilesAuditoria.generarPagoPrestamo(contexto, pagoPrestamo);
                    if(pagoPrestamoResponse.string("estado").equals("ERROR")) LogTAS.error(contexto, (Exception) pagoPrestamoResponse.get("error"));
                    if(pagoPrestamoResponse.string("codigo").equals("200") || pagoPrestamoResponse.string("codigo").equals("202")){
                        Objeto estadoPagoPrestamo = TASSqlAuditoria.actualizarEstadoDeposito(contexto, pagoPrestamo.getNumeroTicket(), pagoPrestamo.getTas(), "R");
                        Objeto codigoRetornoPagoPrestamo = TASSqlAuditoria.actualizarCodigoRetornoDeposito(contexto, pagoPrestamo.getNumeroTicket(), codigoRetornoOperacionOk);
                        if(estadoPagoPrestamo.string("estado").equals("false")) LogTAS.evento(contexto, "No se pudo actualizar el estado del deposito nro: " + pagoPrestamo.getNumeroTicket());
                        if(codigoRetornoPagoPrestamo.string("estado").equals("false")) LogTAS.evento(contexto, "No se pudo actualizar el Codigo de Retorno del deposito nro: " + pagoPrestamo.getNumeroTicket());
                        exito = true;
                        operacionesOK++;
                    } else {
                        Objeto estadoPagoPrestamo = TASSqlAuditoria.actualizarEstadoDeposito(contexto, pagoPrestamo.getNumeroTicket(), pagoPrestamo.getTas(), "Y");
                        if(estadoPagoPrestamo.string("estado").equals("false")) LogTAS.evento(contexto, "No se pudo actualizar el estado del deposito nro: " + pagoPrestamo.getNumeroTicket());
                        operacionesFail++;
                    }
                    intentos++;
                    }                
                }
                response.set("pagoPrestamoReintentos",adminContingencia.getPagosPrestamoAReintentar().size());
                response.set("pagoPrestamosOk", operacionesOK);
                response.set("pagoPrestamosFallidos", operacionesFail);
            }else {
                response.set("pagoPrestamoReintentos",0);
                response.set("pagoPrestamosOk", 0);
                response.set("pagoPrestamosFallidos", 0);
            }

            if(adminContingencia.getPagoPrestamoReversasAReintentar() != null && adminContingencia.getPagoPrestamoReversasAReintentar().size() > 0){
                cantidadReintentos = Integer.valueOf(cantidadReintentosCierrePagoPrestamoReversa);
                operacionesFail = 0;
                LogTAS.evento(contexto, "INICIO_PROCESO_REINTENTO_PAGO_PRESTAMO_REVERSA");
                for(TASDatosPagoPrestamoReversa pagoPrestamoReversa : adminContingencia.getPagoPrestamoReversasAReintentar()){
                    int intentos = 0;
                    while (intentos < cantidadReintentos) {    
                    Objeto pagoPrestamoReversaResponse = UtilesAuditoria.generarReversaPagoPrestamo(contexto, pagoPrestamoReversa);
                    if(pagoPrestamoReversaResponse.string("estado").equals("ERROR")){
                        Objeto estadoPagoTarjetaUpdResponse = TASSqlAuditoria.actualizarEstadoDeposito(contexto, pagoPrestamoReversa.getNumeroTicket(), pagoPrestamoReversa.getTas(), "X");
                        if(estadoPagoTarjetaUpdResponse.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, "TASAuditoriaController - updateLogAuditoria()", (Exception) estadoPagoTarjetaUpdResponse.get("error"));
                        if(estadoPagoTarjetaUpdResponse.string("estado").equals("false")) LogTAS.evento(contexto, "No se pudo actualizar el estado del deposito nro: " + pagoPrestamoReversa.getNumeroTicket());
                        operacionesFail++;
                    }
                    intentos++;
                }
                }
                response.set("pagoPrestamoReversa",adminContingencia.getPagoPrestamoReversasAReintentar().size());
                response.set("pagoPrestamosReversaFallidos",operacionesFail);
            }else{
                response.set("pagoPrestamoReversa",0);
                response.set("pagoPrestamosReversaFallidos",0);
            }
            response.set("registroOperacionAReintentar", adminContingencia.getRegistroOperacionAReintentar() != null ? adminContingencia.getRegistroOperacionAReintentar().size() : 0);
            LogTAS.evento(contexto, "FIN_REINTENTO_OPERACIONES_REVERSA", new Objeto().set("kiosco", kiosco.getKioscoId()));
            return response;
        }catch (Exception e){
            return RespuestaTAS.error(contexto,"TASAuditoriaController - reintentoOperacionesDBInteligente()", e);

        }
    }

    public static Objeto reintentoCierreDBInteligente(ContextoTAS contexto){
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idTas = contexto.parametros.string("tasId");
            TASKiosco kiosco = contexto.getKioscoContexto(Integer.valueOf(idTas));
            if(kiosco == null) return RespuestaTAS.sinResultados(contexto, "Kiosco no encontrado");

            String tipoCierre = contexto.parametros.string("tipoCierre", "");
            if(tipoCierre.equals("")) return RespuestaTAS.sinParametros(contexto, "tipoCierre vacio");
            String fechaAuditoria = contexto.parametros.string("fechaAuditoria", "");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fechaCierreActual = fechaAuditoria.equals("") ? new Date() : sdf.parse(fechaAuditoria);
            LogTAS.evento(contexto, "INICIO_REGISTRO_CIERRE_LOTE", contexto.parametrosOfuscados());
            TASAuditoria adminAuditoria = UtilesAuditoria.getDatosCierre(contexto, idTas);
            Objeto responseApi = TASRestAuditoria.postRegistroCierreLote(contexto, kiosco,adminAuditoria, sdf.format(fechaCierreActual), tipoCierre);
            if(responseApi.string("estado").equals("ERROR")) return RespuestaTAS.error(contexto, TASMensajesString.CIERRE_LOTE_ERROR.toString(), TASMensajesString.CIERRE_LOTE_ERROR.getTipoMensaje(), "TASAuditoriaController - reintentoCierreDBInteligente()");
            Objeto response = new Objeto();
            response.set("cierre_lote", "true");
            if(responseApi instanceof ApiResponse){
                ApiResponse responseApiObj = (ApiResponse)responseApi.get("respuesta");
                response.set("codigo", responseApiObj.codigoHttp);
            }
            response.set("mensaje", TASMensajesString.CIERRE_LOTE_SUCCESS.getTipoMensaje());
            LogTAS.evento(contexto,"FIN_REGISTRO_CIERRE_LOTE", response);
            return response;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto,"TASAuditoriaController - reintentoCierreDBInteligente()", e);

        }
    }
public static Objeto generacionTicket(ContextoTAS contexto){
        try{
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String idTas = contexto.parametros.string("tasId");
            String sucursalTAS = contexto.parametros.string("sucursalId");
            String fragmento = contexto.parametros.string("document");

            String nroLote = contexto.parametros.string("nroLote");


            TASKiosco kiosco = contexto.getKioscoContexto(Integer.valueOf(idTas));
            if(kiosco == null) return RespuestaTAS.sinResultados(contexto, "Kiosco no encontrado");
          
            
            
            String ticketId = "1";
            String pdfConverter = contexto.config.string("tas_path_properties").equals("G:/app/TAS/properties") ? contexto.config.string("tas_path_properties")+"/wkhtmltopdf/bin/wkhtmltopdf.exe" : contexto.config.string("tas_path_properties")+ "/wkhtmltopdf/bin/wkhtmltopdf";
           String pathTicketsTmp = contexto.config.string("tas_path_auditoria")+"/temporario/ticketTmp";
           String pathPdfTmp = contexto.config.string("tas_path_auditoria")+"/temporario/ticketPdfTmp";
    		
           Path dirPathTicketsTmp = Paths.get(pathTicketsTmp);
           Path dirPathPdfTmp = Paths.get(pathPdfTmp);
           
           if (!Files.exists(dirPathTicketsTmp)) {
               try {
     			Files.createDirectories(dirPathTicketsTmp);
     		} catch (IOException e) {
            	RespuestaTAS.error(contexto, "Imposible crear directorio: " + e);
     		}
            }
             
             if (!Files.exists(dirPathPdfTmp)) {
                
       			Files.createDirectories(dirPathPdfTmp);
        }
           
        // get the Ticket2PDF instance
   		Ticket2PDF t2p = Ticket2PDF.getInstance();

   		// configure the attributes
        t2p.setHtml2PDFConverter(pdfConverter);
   		t2p.setPdfRepoPath(pathPdfTmp);
   		t2p.setTicketRepoPath(pathTicketsTmp);
    		
   	 String ticketId0 = createSimpleTicket(t2p, fragmento, contexto);
   	 
     SimpleDateFormat df = new SimpleDateFormat();
     df.applyPattern("ddMMyyyyHHmmss");

     String nombrePDF = sucursalTAS + "_" + idTas + "_" + nroLote + "_" + df.format(new Date()) + ".pdf";
     

 	
     // convert the tickets
     convertTicket(t2p, ticketId0, nombrePDF, "", fragmento, Arrays.asList("", "", ""), contexto);
     

     // delete the original tickets
     t2p.getTicketFile(ticketId0).ticketDestroy();
    			
     Objeto obj = new Objeto();
     obj.set("estado", "EXITO");
     return obj;
    			
        } catch (Exception e) {
            return RespuestaTAS.error(contexto,"TASAuditoriaController - generacionTicket()", e);
        }
    }
    
    private static void convertTicket(Ticket2PDF t2p, String ticketId,
            String pdfFileName, String title, String fragmento, List<String> subtitles, ContextoTAS contexto) throws Ticket2PDFException {

// get a converter object
Converter converter = t2p.getConverter(t2p.getTicketFile(ticketId),
title, subtitles);

// try converting
generatePDFTicket(t2p,ticketId,pdfFileName, fragmento, Arrays.asList("", "", ""), contexto);
	/*try {
		converter.convert(pdfFileName);
	} catch (Exception e) {
    	RespuestaTAS.error(contexto,"error: " + e);
	}*/

		
}
    
    private static String createSimpleTicket(Ticket2PDF t2p, String fragment, ContextoTAS contexto) {

        // placeholder for the ticket name
        String ticketId = null;

        // try creating a ticket
        try {

            // create a new ticket file and get the id
            TicketFile tf0 = t2p.getTicketFile(234);
            ticketId = tf0.getTicketId();
            tf0.ticketCreate();
            tf0.ticketAppend(fragment);
            tf0.ticketComplete();
        } catch (IOException ioe) {
        	RespuestaTAS.error(contexto, "error: " + ioe);
        } catch (Ticket2PDFException tpe) {
        	RespuestaTAS.error(contexto,"error: " + tpe);
        }
        

        return ticketId;
    }
    
	public static Objeto generacionTicketLargo(ContextoTAS contexto) {
		try {
			String idCliente = contexto.parametros.string("idCliente");
			TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
			// ? comentar las proximas 2 lineas para no validar el cliente en sesion
			if (clienteSesion == null)
				return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
			if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
				return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
			String idTas = contexto.parametros.string("tasId");
			String sucursalTAS = contexto.parametros.string("sucursalId");
			String fragmento = contexto.parametros.string("fragmento");

			boolean ticketComplete = contexto.parametros.bool("ticketComplete");
			String nroLote = contexto.parametros.string("nroLote");
            LogTAS.evento(contexto, "INICIO_GENERACION_TICKET_LARGO", new Objeto().set("kiosco", idTas).set("nroLote", nroLote));
			TASKiosco kiosco = contexto.getKioscoContexto(Integer.valueOf(idTas));
			if (kiosco == null)
				return RespuestaTAS.sinResultados(contexto, "Kiosco no encontrado");
			agregarFragmento(idTas, fragmento);

			String ticketId = "1";
			String pathTicketsTmp = contexto.config.string("tas_path_auditoria") + "/temporario/ticketTmp";
			String pathPdfTmp = contexto.config.string("tas_path_auditoria") + "/temporario/ticketPdfTmp";

			// get the Ticket2PDF instance
			Ticket2PDF t2p = Ticket2PDF.getInstance();

			// configure the attributes
			t2p.setPdfRepoPath(pathPdfTmp);
			t2p.setTicketRepoPath(pathTicketsTmp);

			if (!ticketComplete) {
				Objeto fragAgregado = new Objeto();
				fragAgregado.set("fragmentoAgregado", "1");
				return fragAgregado;
			}

			String ticketId_ = null;

			List fragmentos = TASTicketSingleton.getInstance().getFragmentos().get(idTas);
            LogTAS.evento(contexto, "ARMADO_FRAGMENTOS");
			String ticketTotal = getTotalFragmentos(fragmentos);

			ticketId_ = createFragmentedTicket(t2p, idTas, ticketId, fragmentos);

			SimpleDateFormat df = new SimpleDateFormat();
			df.applyPattern("ddMMyyyyHHmmss");

			String nombrePDF = "vuelco_" + sucursalTAS + "_" + idTas + "_" + nroLote + "_" + df.format(new Date())
					+ ".pdf";
            LogTAS.evento(contexto, "GENERACION_PDF_TICKET");
			generatePDF(t2p, ticketId_, nombrePDF, ticketTotal, Arrays.asList("", "", ""), contexto);
			

			t2p.getTicketFile(ticketId_).ticketDestroy();

			Objeto ticket = new Objeto();
			ticket.set("ticketId", ticketId_);

			if (ticketComplete) {
				TASTicketSingleton tasTicket = TASTicketSingleton.getInstance();
				tasTicket.getFragmentos().remove(contexto.parametros.string("tasId"));
			}
            LogTAS.evento(contexto, "FIN_GENERACION_TICKET_LARGO", new Objeto().set("kiosco", idTas).set("nroLote", nroLote));
			return ticket;

		} catch (Exception e) {
			return RespuestaTAS.error(contexto, "TASAuditoriaController - generacionTicket()", e);
		} finally {
			
		}
	}

	private static void generatePDF(Ticket2PDF t2p, String ticketId_, String nombrePDF, String ticket,
			List<String> subtitles, ContextoTAS contexto) throws Ticket2PDFException {
		org.jsoup.nodes.Document doc = Jsoup.parse(ticket);
		 if (doc == null) {
	            throw new Ticket2PDFException("Cannot parse TicketFile " +
	                                          ticket);
	        }
		 try {
	 Converter convert = t2p.getConverter(t2p.getTicketFile(ticketId_), "", subtitles);
		 
		convert.convertDocumentTicket(doc);
		 
        String outputPath = t2p.getPdfRepoPath()+File.separator+nombrePDF;
		 String html =  doc.html().replaceAll("<br>", "<br></br>").replaceAll("<hr>", "<hr></hr>").replaceAll("&nbsp;", "&#xA0;");
		 
		 /*org.jsoup.nodes.Document doc2 = Jsoup.parse(html);
		 doc2.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
		 html = doc2.html();*/
		 
		 convertHtmlToPdf(html, outputPath, contexto);
		
	} catch (Exception e) {
    	RespuestaTAS.error(contexto, "error: " + e);
	}
}
	
	private static void generatePDFTicket(Ticket2PDF t2p, String ticketId_, String nombrePDF, String ticket,
			List<String> subtitles, ContextoTAS contexto) throws Ticket2PDFException {
		org.jsoup.nodes.Document doc = Jsoup.parse(ticket);
		 if (doc == null) {
	            throw new Ticket2PDFException("Cannot parse TicketFile " +
	                                          ticket);
	        }
		 try {
	 Converter convert = t2p.getConverter(t2p.getTicketFile(ticketId_), "", subtitles);
		 
		convert.convertDocumentTicket(doc);
		 
        String outputPath = t2p.getPdfRepoPath()+File.separator+nombrePDF;
		 String html =  doc.html().replaceAll("<br>", "<br></br>").replaceAll("<hr>", "<hr></hr>").replaceAll("&nbsp;", "&#xA0;");
		 
		 org.jsoup.nodes.Document doc2 = Jsoup.parse(html);
		 doc2.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
		 html = doc2.html();
		 
		 convertHtmlToPdf(html, outputPath, contexto);
		
	} catch (Exception e) {
    	RespuestaTAS.error(contexto, "error: " + e);
	}
}
	private static void convertHtmlToPdf(String html, String outputPath, ContextoTAS contexto) {
		try {
			File output = new File(outputPath);
	        ITextRenderer iTextRenderer = new ITextRenderer();
	        html = html.replace("&nbsp;", " ");
	        iTextRenderer.setDocumentFromString(html);
	        iTextRenderer.layout();
	        OutputStream os = new FileOutputStream(output);
	        iTextRenderer.createPDF(os);
	        os.close();
			} catch (Exception e) {
		    	RespuestaTAS.error(contexto, "error: " + e);
			}
		
	}
	private static String createFragmentedTicket(Ticket2PDF t2p, String idTas, String ticketId, List fragmentos) {
		// create a new ticket file and get the id
				TicketFile tf = t2p.getTicketFile(idTas);
				ticketId = tf.getTicketId();
				
				fragmentos.remove(idTas);

				return ticketId;
	}
	private static String getTotalFragmentos(List fragmentos) {
		String ticket = "<html>";
		

		for (int i = 0; i < fragmentos.size(); i++) {
			ticket += fragmentos.get(i).toString()+"\n";
		}
		ticket += "</html>";
				
		return ticket;
	}
	private static void agregarFragmento(String tasId, String fragmento) {
		TASTicketSingleton tasTicket = TASTicketSingleton.getInstance();

		List listaFragmentos = tasTicket.getFragmentos().get(tasId);

		if (listaFragmentos == null || listaFragmentos.isEmpty()) {
			List listaNueva = new ArrayList();
			listaNueva.add(fragmento);
			
			tasTicket.getFragmentos().put(tasId, listaNueva);
			
		} else {
			tasTicket.getFragmentos().get(tasId).add(fragmento);
		}
			
	}  
}
