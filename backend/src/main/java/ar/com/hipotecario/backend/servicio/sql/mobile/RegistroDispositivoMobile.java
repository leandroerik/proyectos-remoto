package ar.com.hipotecario.backend.servicio.sql.mobile;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.mobile.RegistroDispositivoMobile.RegistroDispositivo;

public class RegistroDispositivoMobile extends SqlObjetos<RegistroDispositivo> {

    /* ========== ATRIBUTOS ========== */
    public static class RegistroDispositivo extends SqlObjeto {
        public int Id;
        public String IdDispositivo;
        public String Alias;
        public Fecha FechaAlta;
        public String DireccionIp;
    }

    /* ========== SERVICIO ========== */
    public static Boolean deshabilitarRegistroDispositivo(Contexto contexto, String idDispositivo, String id) {
        String sql = "UPDATE [Mobile].[dbo].[registro_dispositivo] SET id_dispositivo = ? WHERE id = ?";
        Object[] parametros = new Object[2];
        parametros[0] = idDispositivo;
        parametros[1] = id;
        return Sql.update(contexto, "mobile", sql, parametros) == 1;
    }

    public static RegistroDispositivo obtenerUltimoRegistroPorCobis(Contexto contexto, String idCobis) {
        String sql = "SELECT TOP 1 id AS Id, id_dispositivo AS IdDispositivo, alias AS Alias, fecha_alta AS FechaAlta, direccion_ip AS DireccionIp FROM [Mobile].[dbo].[registro_dispositivo] WHERE [id_cobis] = ? ORDER BY [fecha_alta] DESC";
        Objeto datos = Sql.select(contexto, "mobile", sql, idCobis);
        return map(datos, RegistroDispositivoMobile.class, RegistroDispositivo.class).first();
    }

    public static Boolean insertRegistroDispositivo(Contexto contexto, String cobis, String idDispositivo, String alias) {
        String sql = "";
        sql += "INSERT INTO [Mobile].[dbo].[registro_dispositivo] ";
        sql += "([id_cobis], [id_dispositivo], [alias], [direccion_ip]) ";
        sql += "VALUES ( ?, ?, ?, ? ) ";

        return Sql.update(contexto, "esales", sql, cobis, idDispositivo, alias, contexto.ip()) > 0;
    }
}