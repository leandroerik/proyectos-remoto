package ar.com.hipotecario.canal.tas.modulos.inicio.login.controllers;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.modulos.inicio.login.servicios.sql.TASSqlSesionCliente;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;

public class TASSesionClienteController {

    // Obtener sesion en Contexto por Id Cliente
    public static Objeto getSesionClienteById(ContextoTAS contexto) {
        Objeto response = new Objeto();
        String idCliente = contexto.parametros.string("idCliente");
        if (contexto.sesion().idCobis.equals(idCliente)) {
            response.set("idSesion", contexto.sesion().idSesionTAS);
            response.set("idCliente", contexto.sesion().idCobis);
            response.set("cliente", contexto.sesion().clienteTAS);
            response.set("hora_inicio", contexto.sesion().fechaLogin);
        } else {
            response = RespuestaTAS.sinResultados(contexto, "No existe sesion en contexto");
        }
        return response;
    }

    // Obtener sesion en Contexto por Nro Documento
    public static Objeto getSesionClienteByDoc(ContextoTAS contexto) {
        Objeto response = new Objeto();
        String nroDoc = contexto.parametros.string("nroDoc");
        TASClientePersona sesionCliente = contexto.sesion().clienteTAS;
        if (sesionCliente.getNumeroDocumento().equals(nroDoc)) {
            response.set("idSesion", contexto.sesion().idSesionTAS);
            response.set("idCliente", contexto.sesion().idCobis);
            response.set("cliente", contexto.sesion().clienteTAS);
            response.set("hora_inicio", contexto.sesion().fechaLogin);
        } else {
            response = RespuestaTAS.sinResultados(contexto, "No existe sesion en contexto");
        }
        return response;
    }

    // Obtener ultima sesion registrado en BBDD
    public static Objeto getLastSesion(ContextoTAS contexto) {
        String nroDoc = contexto.parametros.string("nroDoc");
        if (nroDoc.isEmpty())
            RespuestaTAS.sinParametros(contexto, "Nro Documento no ingresado");
        Objeto response = TASSqlSesionCliente.getUltimaSesion(contexto, nroDoc);
        return response;
    }

    // verifico si existe sesion activa
    public static Objeto existeSesionActiva(ContextoTAS contexto) {
        Objeto response = new Objeto();
        String nroDoc = contexto.parametros.string("nroDoc");
        String idTas = contexto.parametros.string("tasId");
        if (nroDoc.isEmpty() || idTas.isEmpty())
            RespuestaTAS.sinParametros(contexto, "Uno o mas parametros no ingresados");
        Boolean existeSesion = TASSqlSesionCliente.existeSesion(contexto, nroDoc, idTas);
        return existeSesion ? response.set("sesion_activa", true) : response.set("sesion_activa", false);
    }
}
