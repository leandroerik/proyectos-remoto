package ar.com.hipotecario.canal.tas.modulos.inicio.login.controllers;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKiosco;
import ar.com.hipotecario.canal.tas.modulos.inicio.login.servicios.sql.TASSqlUsuariosAdministradores;

public class TASUsuariosAdminController {
    public static Objeto buscarUsuarioAdminByTipoDocSucursal(ContextoTAS contexto, TASKiosco kiosco) {
        try {
        Objeto resp = new Objeto();
        String nroDoc = contexto.parametros.string("nroDoc");
        String tipoDoc = contexto.parametros.string("tipoDoc");
        Integer sucursalId = kiosco.getSucursalId();
        resp = TASSqlUsuariosAdministradores.obtenerAdministrador(contexto, nroDoc, tipoDoc, sucursalId);
            return resp;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "Error al invocar el servicio Usuarios Administradores", e);
        }
    }

    public static Objeto buscarUsuarioAdminByDocSucursal(ContextoTAS contexto, TASKiosco kiosco) {
        try {
            String nroDoc = contexto.parametros.string("nroDoc");
            Integer sucursalId = kiosco.getSucursalId();
            Objeto resp = TASSqlUsuariosAdministradores.obtenerAdministrador(contexto, nroDoc, sucursalId);
            return resp;
        } catch (Exception e) {
            return new Objeto().set("ERROR", e);
        }
    }

    public static Objeto buscarUsuarioAdminByTipoDoc(ContextoTAS contexto) {
        try {
        Objeto resp = new Objeto();
        String nroDoc = contexto.parametros.string("nroDoc");
        String tipoDoc = contexto.parametros.string("tipoDoc");
        resp = TASSqlUsuariosAdministradores.obtenerAdministrador(contexto, nroDoc, tipoDoc);
        return resp;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "Error al invocar el servicio Usuarios Administradores", e);
        }
    }

    // ! que onda aca...
    public static Objeto existeSesionAdministrador(ContextoTAS contexto) {
        String nroDoc = contexto.parametros.string("nroDoc");
        if (nroDoc.isEmpty())
            RespuestaTAS.sinParametros(contexto, "Nro Documento no ingresado");
        boolean existeSesion = TASSqlUsuariosAdministradores.existeSesionAdministador(contexto, nroDoc);
        return new Objeto();
    }
    // TODO
    // rta.set("Session ADM", existeSessionAdministador(contexto, nroDoc));
}
