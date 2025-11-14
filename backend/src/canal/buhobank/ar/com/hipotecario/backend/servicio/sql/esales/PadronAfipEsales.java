package ar.com.hipotecario.backend.servicio.sql.esales;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.PadronAfipEsales.PadronAfip;

import java.math.BigDecimal;

public class PadronAfipEsales extends SqlObjetos<PadronAfip> {
    public static class PadronAfip extends SqlObjeto {
        public String id;
        public String cuit;
        public String imp_ganancias;
        public String imp_iva;
        public String monotributo;
        public String integrante_sociedad;
        public String empleador;
        public BigDecimal sin_uso;
    }

    public static PadronAfip get(Contexto contexto, String cuit) {
        String sql = "";
        sql += "SELECT * FROM padronAfip ";
        sql += "WHERE cuit = ? ";

        Objeto datos = Sql.select(contexto, SqlEsales.SQL, sql, cuit);
        return map(datos, PadronAfipEsales.class, PadronAfip.class).first();
    }
}
