package ar.com.hipotecario.backend.servicio.sql.homebanking;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.homebanking.AccesoBiometriaHB.AccesoBiometria;
import org.apache.commons.lang3.StringUtils;

public class AccesoBiometriaHB extends SqlObjetos<AccesoBiometria> {

    /* ========== ATRIBUTOS ========== */
    public static class AccesoBiometria extends SqlObjeto {
        public String idCobis;
        public String Token;
    }

    /* ========== SERVICIO ========== */
    public static AccesoBiometria obtenerAccesosBiometria(Contexto contexto, String idCobis, String dispositivo) {
        String sql = "SELECT TOP 1 [id_cobis] AS IdCobis, [access_token] AS Token FROM [Homebanking].[dbo].[usuarios_biometria_b] WHERE [id_cobis] = ?";
        Object[] parametros = new Object[2];
        parametros[0] = idCobis;

        if (StringUtils.isNotBlank(dispositivo)) {
            sql += " AND [id_dispositivo] = ?";
            parametros[1] = dispositivo;
        } else
            sql += " ORDER BY [fecha_disp_registrado] DESC";

        Objeto datos = Sql.select(contexto, "homebanking", sql, idCobis);
        return map(datos, AccesoBiometriaHB.class, AccesoBiometria.class).first();
    }

    public static Boolean borrarAccesosBiometria(Contexto contexto, String idCobis) {
        String sql = "UPDATE [homebanking].[dbo].[usuarios_biometria_b] SET [fecha_disp_registrado] =  GETDATE(), [disp_registrado] = 0, [biometria_activa] = 0, [fecha_biometria_activa] = GETDATE(), [buhoFacil_activo] = 0, [fecha_buhoFacil_activo] = GETDATE() WHERE [id_cobis] = ?";
        return Sql.update(contexto, "homebanking", sql, idCobis) == 1;
    }
}
