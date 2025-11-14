package ar.com.hipotecario.backend.servicio.sql.hb_be;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;

public class LogsCrm extends SqlObjetos<LogsCrm.LogCrm> {


    public static class LogCrm extends SqlObjeto {
        public String id;
        public Fecha momento;
        public int empresa;
        public int usuario;
        public String operacion;
        public String usuario_crm;

    }

    public static Boolean post(Contexto contexto, int empresa, int usuario, String operacion,String usuario_crm) {
        String sql = "";
        sql += "INSERT INTO [hb_be].[dbo].[log_crm] ";
        sql += "([fecha],[empresa],[usuario],[operacion],[usuario_crm]) ";
        sql += "VALUES (GETDATE(), ?, ?, ?, ?)";
        Object[] parametros = new Object[4];
        parametros[0] = empresa;
        parametros[1] = usuario;
        parametros[2] = operacion;
        parametros[3] = usuario_crm;
        return Sql.update(contexto, "hb_be", sql, parametros) == 1;
    }

    public static LogsCrm selectPorFecha(Contexto contexto, String fecha1,String fecha2, String cuitEmpresa ) {
        String sql = "";
        sql += "SELECT * ";
        sql += "FROM [HB_BE].[dbo].[log_crm] WHERE ";
        sql += "fecha BETWEEN ? AND ?";
        Objeto datos = new Objeto();
        if(cuitEmpresa!=null){
            sql += "AND empresa= ? ";
            datos = Sql.select(contexto, "hb_be", sql, fecha1, fecha2,cuitEmpresa);
        }else{
            datos = Sql.select(contexto, "hb_be", sql, fecha1, fecha2);
        }


        return map(datos, LogsCrm.class, LogsCrm.LogCrm.class);
    }
}
