package ar.com.hipotecario.backend.servicio.sql.mobile;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.mobile.SoftTokenMobile.SoftToken;

public class SoftTokenMobile extends SqlObjetos<SoftToken> {

    /* ========== ATRIBUTOS ========== */
    public static class SoftToken extends SqlObjeto {
        public long Id;
        public String IdCobis;
        public Fecha FechaAlta;
        public String Estado;
    }

    /* ========== SERVICIO ========== */
    public static SoftToken obtenerUltimaAltaSoftTokenActiva(Contexto contexto, String idCobis) {
        String sql = "SELECT id AS Id, id_cobis AS IdCobis, fecha_alta AS FechaAlta, estado AS Estado FROM [Mobile].[dbo].[soft_token_alta] WHERE estado = 'ACTIVO' and id_cobis = ?";
        Objeto datos = Sql.select(contexto, "mobile", sql, idCobis);
        return map(datos, SoftTokenMobile.class, SoftToken.class).first();
    }

    public static Boolean deshabilitarSoftToken(Contexto contexto, String id) {
        String sql = "UPDATE [Mobile].[dbo].[soft_token_alta] SET estado = 'INACTIVO' WHERE id = ?";
        return Sql.update(contexto, "mobile", sql, id) == 1;
    }
}
