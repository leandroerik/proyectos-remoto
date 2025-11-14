package ar.com.hipotecario.canal.tas.shared.modulos.apis.auditor.controllers;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.auditor.modelos.negocio.TASReporte;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.auditor.servicios.TASRestAuditor;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKiosco;
import ar.com.hipotecario.mobile.lib.Util;
import org.apache.http.HttpStatus;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TASApiAuditor {

    public static Objeto generarReporteReinicio(ContextoTAS contexto, TASKiosco datosKiosco) {
        Objeto mensajeEntrada = new Objeto();
        mensajeEntrada.set("uri", "/v1/reportes");
        mensajeEntrada.set("terminal", datosKiosco.getKioscoId());

        Objeto mensajeSalida = new Objeto();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        mensajeSalida.set("uri", "/v1/reportes");
        mensajeSalida.set("inicio", df.format(new Date()));
        mensajeSalida.set("fin", df.format(new Date()));
        mensajeSalida.set("terminal", datosKiosco.getKioscoId());

        TASReporte reporte = generarDatosReporte(contexto, datosKiosco, "API-Auditor-ReinicioTerminal", mensajeEntrada,
                mensajeSalida);
        return TASRestAuditor.generarReporte(contexto, reporte);
    }

    public static Objeto generarReporteGeneraTicket(ContextoTAS contexto, TASKiosco datosKiosco, String numeroTicket) {
        Objeto mensajeEntrada = new Objeto();
        mensajeEntrada.set("uri", "/v1/reportes");
        mensajeEntrada.set("terminal", datosKiosco.getKioscoId());

        Objeto mensajeSalida = new Objeto();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        mensajeSalida.set("uri", "/v1/reportes");
        mensajeSalida.set("inicio", df.format(new Date()));
        mensajeSalida.set("fin", df.format(new Date()));
        mensajeSalida.set("terminal", datosKiosco.getKioscoId());
        mensajeSalida.set("numeroTicket", numeroTicket);
        TASReporte reporte = generarDatosReporte(contexto, datosKiosco, "API-Auditor-GenerarTicket", mensajeEntrada,
                mensajeSalida);
        return TASRestAuditor.generarReporte(contexto, reporte);
    }

    public static TASReporte generarDatosReporte(ContextoTAS contexto, TASKiosco datosKiosco, String servicio,
            Objeto mensajeEntrada, Objeto mensajeSalida) {
        TASReporte reporte = new TASReporte();
        reporte.setCanal("TAS");
        reporte.setSubCanal(datosKiosco.getKioscoId().toString());
        TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
        String clienteId = clienteSesion != null ? clienteSesion.getIdCliente() : "";
        reporte.setUsuario( !clienteId.isEmpty() ? clienteId : datosKiosco.getKioscoId().toString());
        reporte.setIdProceso(Util.idProceso());
        reporte.setSesion(contexto.idSesion());
        reporte.setServicio(servicio);
        reporte.setResultado(String.valueOf(HttpStatus.SC_OK));
        reporte.setMensajeEntrada(mensajeEntrada);
        reporte.setMensajeSalida(mensajeSalida);
        reporte.setDuracion(1);
        return reporte;
    }
}
