package ar.com.hipotecario.canal.tas.modulos.plazosfijos.services;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;

public class TASSqlPlazosFijos {

  public static Objeto getTerminosYCondiciones(ContextoTAS contexto, Date fechaConstitucion){

    try {
            SimpleDateFormat spd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String fechaConsFormat = spd.format(fechaConstitucion);
            String sql = "";
            sql += "SELECT data FROM pf_meta_tyc ";
            sql += "WHERE vigencia_desde <= ? ";
            sql += "AND vigencia_hasta >= ?";
            Objeto rtaSql = Sql.select(contexto, "hipotecariotas", sql, fechaConsFormat, fechaConsFormat);
            Objeto response = new Objeto();
            response.set("estado", "OK");
            response.set("respuesta", rtaSql);
            return response;
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            LogTAS.error(contexto, e);
            return error;
        }
  }
  
}
