package ar.com.hipotecario.canal.tas.shared.modulos.kiosco.servicios;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKiosco;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKioscoEstadoOperativo;
import ar.com.hipotecario.canal.tas.ContextoTAS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class TASSqlKiosco {

    public static boolean datosBD(ContextoTAS contexto){
        try{
            String sql = "";
            sql += "SELECT TOP (2) [KioscoId] FROM [hipotecarioTAS].[dbo].[TASKioscos]";
            Objeto rtaSql = Sql.select(contexto,"hipotecariotas",sql);
            SqlException.throwIf("DireccionIP Invalido", rtaSql.isEmpty());
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public static TASKiosco completarDatosKioscoById(ContextoTAS contexto, String id){
        try{
            Objeto kiosco = TASSqlKiosco.obtenerKioscoById(contexto,id);
            String kioscoId = kiosco.objetos().size() > 0 ? kiosco.objetos().get(0).string("KioscoId") : null;
            Objeto kioscoDbHelper = TASSqlKiosco.obtenerDatosKiosco(contexto,kioscoId);
            return TASKiosco.armarModeloKiosco(kiosco,kioscoDbHelper);
        }catch(Exception e){
            TASKiosco error = new TASKiosco();
            error.set("ERROR", e);
            return error;
        }
    }

    public static TASKiosco completarDatosKioscoByIp(ContextoTAS contexto, String ip){
        try{
            Objeto kiosco = TASSqlKiosco.obtenerKioscoByIp(contexto,ip);
            String kioscoId = kiosco.objetos().size() > 0 ? kiosco.objetos().get(0).string("KioscoId") : null;
            Objeto kioscoDbHelper = TASSqlKiosco.obtenerDatosKiosco(contexto,kioscoId);
            return TASKiosco.armarModeloKiosco(kiosco,kioscoDbHelper);
        }catch(Exception e){
            TASKiosco error = new TASKiosco();
            error.set("ERROR", e);
            return error;
        }
    }

    public static Objeto obtenerKioscoByIp(ContextoTAS contexto, String ipKiosco){
        Objeto rtaSql = new Objeto();
        String sql = "";
        sql += "SELECT * FROM [hipotecarioTAS].[dbo].[TASKioscos] ";
        sql += "WHERE DireccionIP = ? ";
        sql += "AND FlagHabilitado = 1";
        rtaSql = Sql.select(contexto,"hipotecariotas",sql, ipKiosco);
        SqlException.throwIf("DireccionIP Invalido", rtaSql.isEmpty());
        return rtaSql;
    }

    public static Objeto obtenerKioscoById(ContextoTAS contexto, String idKiosco){
        Objeto rtaSql = new Objeto();
        String sql = "";
        sql += "SELECT * FROM [hipotecarioTAS].[dbo].[TASKioscos] ";
        sql += "WHERE KioscoId = ? ";
        sql += "AND FlagHabilitado = 1";
        rtaSql = Sql.select(contexto,"hipotecariotas",sql, idKiosco);
        SqlException.throwIf("DireccionIP Invalido", rtaSql.isEmpty());
        return rtaSql;
    }

    public static Objeto obtenerDatosKiosco(ContextoTAS contexto, String kioscoId){
        Objeto response = new Objeto();
        Objeto datosKiosco = new Objeto();
        String sql = "";
        sql += "SELECT TV.Nombre, TV.ClaseJava, KV.Valor  ";
        sql += "FROM [hipotecarioTAS].[dbo].[TASKioscosVariables] AS KV ";
        sql += "INNER JOIN [hipotecarioTAS].[dbo].[TASTiposVariable] AS TV ";
        sql += "ON KV.TipoVariable = TV.TipoVariable ";
        sql += "WHERE (KV.KioscoId = ?)";
        Objeto rtaSql = Sql.select(contexto, "hipotecariotas", sql, kioscoId);
        SqlException.throwIf("ID_KIOSCO Invalido", rtaSql.isEmpty());
        response = rtaSql.objetos().get(0);
        datosKiosco.set("Hora.Inicio", response.string("Valor"));
        response = rtaSql.objetos().get(1);
        datosKiosco.set("Hora.Fin", response.string("Valor"));
        rtaSql = null;
        response = null;
        sql = "";
        sql += "SELECT K.SucursalId, K.UbicacionId, U.Nombre, U.UbicacionId, U.Telefono, U.Direccion1, U.Direccion2, U.Direccion3 ";
        sql += "FROM [hipotecarioTAS].[dbo].[TASKioscos] AS K ";
        sql += "INNER JOIN [hipotecarioTAS].[dbo].[TASUbicaciones] AS U ";
        sql += "ON K.UbicacionId = U.UbicacionId ";
        sql += "AND K.SucursalId = U.SucursalId ";
        sql += "WHERE (K.KioscoId = ?)";
        rtaSql = Sql.select(contexto, "hipotecariotas", sql, kioscoId);
        SqlException.throwIf("ID_KIOSCO Invalido", rtaSql.isEmpty());
        response = rtaSql.objetos().get(0);
        datosKiosco.set("Nombre", response.string("Nombre"));
        datosKiosco.set("Telefono",response.string("Telefono"));
        datosKiosco.set("Direccion1",response.string("Direccion1"));
        datosKiosco.set("Direccion2",response.string("Direccion1"));
        datosKiosco.set("Direccion3",response.string("Direccion1"));
        return datosKiosco;
    }

    public static Objeto guardarEstadoOperativo(ContextoTAS contexto,TASKioscoEstadoOperativo estadoOperativo){
        Connection conn = null;
        Objeto obj = new Objeto();
        try {
            conn = contexto.dataSource("hipotecariotas").getConnection();
            conn.setAutoCommit(false);

            Integer rta = 0;
            String idTas = estadoOperativo.getKioscoId().toString();
            String initOperational = estadoOperativo.getInitOperational();
            String buzon = estadoOperativo.getBuzon();
            String impresora = estadoOperativo.getImpresora();
            String msr = estadoOperativo.getMsr();
            String cim = estadoOperativo.getCim();
            Date fecha = estadoOperativo.getFecha();
            Timestamp fechaSql = new Timestamp(fecha.getTime());

            String sqlDel = "";
            sqlDel += "DELETE FROM [hipotecarioTAS].[dbo].[TASEstadosOperativo] ";
            sqlDel += "WHERE KioscoId = ?";

            PreparedStatement psDel = conn.prepareStatement(sqlDel);
            psDel.setString(1, idTas);
            Integer rtaDel = psDel.executeUpdate();
            if(rtaDel == 0 ) return obj.set("estado", "false");


            String sql = "";
            sql += "INSERT INTO [hipotecarioTAS].[dbo].[TASEstadosOperativo] ";
            sql += "(KioscoId, InitOperational, Impresora, Buzon, MSR, Fecha, CIM) VALUES (?,?,?,?,?,?,?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, idTas);
            ps.setString(2, initOperational);
            ps.setString(3, impresora);
            ps.setString(4, buzon);
            ps.setString(5, msr);
            ps.setTimestamp(6, fechaSql);
            ps.setString(7, cim);

            rta = ps.executeUpdate();
            conn.commit();

            return obj.set("estado", rta != 0? "true" : "false");
        }catch (SQLException e){
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                obj.set("estado", "ERROR");
                obj.set("error", e);
                return obj;
            }
            obj.set("estado", "ERROR");
            obj.set("error", e);
            return obj;
        }finally {
            try {
                conn.close();
            } catch (SQLException e) {
                obj.set("estado", "ERROR");
                obj.set("error", e);
                return obj;
            }
        }
    }

   public static Objeto obtenerFlagsKiosco(ContextoTAS contexto, String tasIp){
        String sql = "";
        sql += "SELECT tbf.Nombre,tkf.estado AS Valor,tbf.Descripcion,tbf.Comentario ";
        sql += "FROM [hipotecarioTAS].[dbo].[TASBancoFlags] AS tbf, ";
        sql += "[hipotecarioTAS].[dbo].[TASKioscoFlags] AS tkf, ";
        sql += "[hipotecarioTAS].[dbo].[TASKioscos] AS tk ";
        sql += "WHERE tbf.FlagId=tkf.flagId and tk.groupBotonId=tkf.groupBotonId and tk.DireccionIP=? ";
        sql += "ORDER BY tbf.FlagId";
        Objeto rta = Sql.select(contexto,"hipotecariotas",sql, tasIp);
        SqlException.throwIf("DireccionIP Invalido", rta.isEmpty());
        return rta;
    }


}
