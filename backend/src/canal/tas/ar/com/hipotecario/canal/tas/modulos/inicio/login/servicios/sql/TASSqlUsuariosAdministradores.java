package ar.com.hipotecario.canal.tas.modulos.inicio.login.servicios.sql;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;

import java.util.Date;

public class TASSqlUsuariosAdministradores {
    // sobrecarga de metodo 1: Administrador especifico tipo y nro Documento +
    // sucursal
    public static Objeto obtenerAdministrador(ContextoTAS contexto, String numeroDocumento, String tipoDocumento,
            Integer sucursalId) {
        Objeto rta = new Objeto();
        String sql = "";
        sql += "SELECT * FROM [hipotecarioTAS].[dbo].[TASAdministradores] ";
        sql += "WHERE NumeroDocumento = ? ";
        sql += "AND TipoDocumento = ? ";
        sql += "AND SucursalId = ?";
        try {
            rta = Sql.select(contexto, "hipotecariotas", sql, numeroDocumento, tipoDocumento, sucursalId);
            if (rta.isEmpty())
                return RespuestaTAS.sinResultados(contexto, "TASSqlUsuariosAdministradores - obtenerAdministrador()");
        } catch (Exception e) {
            return RespuestaTAS.error(contexto,
                    "[TASAdminAdministradores - obtenerAdministrador()] - " + e.getMessage());
        }
        return rta;
    }

    // sobrecarga de metodo 2: Administrador especifico nro Documento + sucursal
    public static Objeto obtenerAdministrador(ContextoTAS contexto, String numeroDocumento, Integer sucursalId) {
        try {
            Objeto rta = new Objeto();
            String sql = "";
            sql += "SELECT * FROM [hipotecarioTAS].[dbo].[TASAdministradores] ";
            sql += "WHERE NumeroDocumento = ? ";
            sql += "AND SucursalId = ?";
            rta = Sql.select(contexto, "hipotecariotas", sql, numeroDocumento, sucursalId);
            if (rta.isEmpty())
                return RespuestaTAS.sinResultados(contexto, "TASSqlUsuariosAdministradores - obtenerAdministrador()");
            return rta;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto,
                    "[TASAdminAdministradores - obtenerAdministrador()] - " + e.getMessage());
        }
    }

    // sobrecarga de metodo 3: Administrador especifico tipo y nro Documento
    public static Objeto obtenerAdministrador(ContextoTAS contexto, String numeroDocumento, String tipoDocumento) {
        try {
            String sql = "";
            sql += "SELECT * FROM [hipotecarioTAS].[dbo].[TASAdministradores] ";
            sql += "WHERE NumeroDocumento = ? ";
            sql += "AND TipoDocumento = ? ";
            Objeto rta = Sql.select(contexto, "hipotecariotas", sql, numeroDocumento, tipoDocumento);
            if (rta.isEmpty())
                return RespuestaTAS.sinResultados(contexto, "TASSqlUsuariosAdministradores - obtenerAdministrador()");
            return rta;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto,
                    "[TASAdminAdministradores - obtenerAdministrador()] - " + e.getMessage());
        }
    }

    // verifico si existe sesion de Administrador
    public static Boolean existeSesionAdministador(ContextoTAS contexto, String nroDoc) {
        Objeto rta = new Objeto();
        boolean existeSession = false;
        String sql = "";
        sql += "SELECT TOP 1 DATEDIFF(ss, inicio_sesion, ?) ";
        sql += "as diferencia, inicio_sesion, fin_sesion ";
        sql += "FROM [hipotecarioTAS].[dbo].[TASsesionCliente] ";
        sql += "WHERE id = ?";
        Date fin_sesion = new Date();
        rta = Sql.select(contexto, "hipotecariotas", sql, fin_sesion, nroDoc);
        if(rta.isEmpty()) return false;
        Objeto sesion = rta.objetos().get(0);
        String diferencia = sesion.get("diferencia").toString();
        Long dif = Long.valueOf(diferencia);
        if (sesion.get("fin_sesion") == null && rta.objetos().get(0) != null && dif < 30) {
            existeSession = true;
        }
        return existeSession;
    }
}
