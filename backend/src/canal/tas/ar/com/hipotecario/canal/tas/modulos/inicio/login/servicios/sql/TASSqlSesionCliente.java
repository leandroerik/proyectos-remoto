package ar.com.hipotecario.canal.tas.modulos.inicio.login.servicios.sql;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.exception.SqlException;

import java.util.Date;

public class TASSqlSesionCliente {
        public static Objeto inicioSesion(Contexto contexto, String nroDoc, String idTas){
        Objeto rta = new Objeto();
        Objeto resp = new Objeto();
        Integer rtaSql = 0;
        Date inicio_sesion = new Date();
        String sql = "";
        sql+="SELECT TOP 1 inicio_sesion, fin_sesion ";
        sql+="FROM [hipotecarioTAS].[dbo].[TASsesionCliente] ";
        sql+="WHERE id = ?";
        rta = Sql.select(contexto, "hipotecariotas", sql, nroDoc);
        Objeto sesion = rta.isEmpty() ? null : rta.objetos().get(0);
        if(sesion != null){
            String sqlUpd = "";
            sqlUpd+="UPDATE [hipotecarioTAS].[dbo].[TASsesionCliente] ";
            sqlUpd+="SET inicio_sesion=?, fin_sesion=?, idKiosco=? ";
            sqlUpd+="WHERE id=?";
            inicio_sesion = new Date();
            rtaSql = Sql.update(contexto, "hipotecariotas", sqlUpd,inicio_sesion ,null, idTas, nroDoc);
            SqlException.throwIf("ERROR_Parametros", rtaSql==0);
            resp = rtaSql != null && rtaSql != 0 ? resp.set("update_sesion",true) : resp.set("update_sesion", false);
            if(rtaSql != null && rtaSql != 0)resp.set("hora_inicio", inicio_sesion);
        }else{
                String sqlIns = "";
                sqlIns+="INSERT INTO [hipotecarioTAS].[dbo].[TASsesionCliente] ";
                sqlIns+="(inicio_sesion, id, idKiosco) VALUES (?,?,?)";
                inicio_sesion = new Date();
                rtaSql = Sql.update(contexto, "hipotecariotas", sqlIns, inicio_sesion, nroDoc, idTas);
                SqlException.throwIf("ERROR_Parametros", rtaSql==0);
                resp = rtaSql != null && rtaSql != 0 ? resp.set("update_sesion",true) : resp.set("update_sesion", false);
                 if(rtaSql != null && rtaSql != 0)resp.set("hora_inicio", inicio_sesion);
        }
        return resp;
    }
    public static boolean cierreSesion(Contexto contexto, String nroDoc, String idTas){
        Integer rtaSql = 0;
        Date fin_sesion = new Date();
        String sql = "";
        sql+="UPDATE [hipotecarioTAS].[dbo].[TASsesionCliente] ";
        sql+="SET fin_sesion=? ";
        sql+="WHERE id=? AND idKiosco=?";
        rtaSql = Sql.update(contexto, "hipotecariotas", sql, fin_sesion, nroDoc, idTas);
        SqlException.throwIf("Error al cerrar sesion", rtaSql == null);
        return rtaSql != null && rtaSql != 0 ? true : false;
    }

    public static Objeto getUltimaSesion(Contexto contexto, String nroDoc){
        try {
            String sql = "";
        sql += "SELECT * FROM [hipotecarioTAS].[dbo].[TASsesionCliente] ";
        sql += "WHERE id = ? ";
        Objeto rta = Sql.select(contexto,"hipotecariotas",sql, nroDoc);

        return rta.isEmpty() ? new Objeto().set("estado", "PRIMER_SESION") : new Objeto().set("estado", rta);
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }     
    }

    public static Boolean existeSesion(Contexto contexto, String nroDoc, String idTas ){
        Objeto rta = new Objeto();
        boolean existeSession = false;
        String sql = "";
        sql+="SELECT TOP 1 DATEDIFF(ss, inicio_sesion, ?) ";
        sql+="as diferencia, inicio_sesion, fin_sesion ";
        sql+="FROM [hipotecarioTAS].[dbo].[TASsesionCliente] ";
        sql+="WHERE id = ?";
        Date fin_sesion = new Date();
        rta = Sql.select(contexto, "hipotecariotas", sql, fin_sesion, nroDoc);
        SqlException.throwIf("NRO_DOC Invalido", rta.isEmpty());
        Objeto sesion = rta.objetos().get(0);
        String diferencia = sesion.get("diferencia").toString();
        Long dif = Long.valueOf(diferencia);
        if(sesion.get("fin_sesion") == null && rta.objetos().get(0) != null && dif < 300 ){
            existeSession =true;
            if(idTas != null){
                Objeto rta2 = new Objeto();
                String sql2 = "";
                sql2+="select TOP 1 datediff(ss, fecha, ?) ";
                sql2+="as diferencia ";
                sql2+="FROM [hipotecarioTAS].[dbo].[TASEstadosOperativo] ";
                sql2+="WHERE KioscoId = ?";
                Date fechaCompare = new Date();
                rta2 = Sql.select(contexto, "hipotecariotas", sql2, fechaCompare, idTas);
                SqlException.throwIf("ID_TAS Invalido", rta2.isEmpty());
                Objeto sesionTas = rta2.objetos().get(0);
                String diferenciaTAS = sesionTas.get("diferencia").toString();
                Long difTas = Long.valueOf(diferenciaTAS);
                if(rta2.objetos().get(0) != null && difTas < 60 ){
                    existeSession = false;
                }
            }
        }
        return existeSession;
    }
    public static boolean resetClientsTerminal(Contexto contexto, Integer idTas){
            int rtaSql = 0;
            Date fin_sesion = new Date();
            String sql="";
            sql+="UPDATE [hipotecarioTAS].[dbo].[TASsesionCliente] ";
            sql+="SET fin_sesion=? ";
            sql+="WHERE idKiosco=?";
            rtaSql = Sql.update(contexto, "hipotecariotas", sql, fin_sesion, idTas);
            return rtaSql != 0;
    }

}
