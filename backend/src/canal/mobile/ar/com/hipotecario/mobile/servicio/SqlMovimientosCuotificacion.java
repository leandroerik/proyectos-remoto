package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;

import java.sql.Timestamp;
import java.util.Date;

public class SqlMovimientosCuotificacion {

    private static final String DB_HOMEBANKING = "homebanking";

    /* ========== INSERT ========== */
    private static final String SP_EXEC_INSERT = "EXEC [homebanking].[dbo].[insertar_movimiento] @id_cobis = ?, @id_movimiento = ?, @finalizado = ?";
    private static final String INSERT = "InsertMovimiento";
    /* ========== UPDATE ========== */
    private static final String SP_EXEC_UPDATE = "EXEC [homebanking].[dbo].[actualizar_movimiento] @id = ?, @finalizado = ?, @fecha_alta = ?";
    private static final String UPDATE = "UpdateMovimiento";
    /* ========== DELETE ========== */
    private static final String SP_EXEC_DELETE = "EXEC [homebanking].[dbo].[eliminar_movimientos_pendientes] @id_cobis = ?";
    private static final String DELETE = "DeleteMovimiento";
    /* ========== GET ========== */
    private static final String SP_EXEC_GET = "EXEC [homebanking].[dbo].[consultar_movimientos] @id_cobis = ?, @finalizado = ?";
    private static final String GET = "GetMovimientos";

    public static void insertar(String idCobis, String idMovimiento, boolean finalizado) {
        try {
            SqlRequestMB sqlRequest = SqlMB.request(INSERT, DB_HOMEBANKING);

            sqlRequest.sql = SP_EXEC_INSERT;
            sqlRequest.parametros.add(idCobis);
            sqlRequest.parametros.add(idMovimiento);
            sqlRequest.parametros.add(finalizado);

            SqlMB.response(sqlRequest);
        } catch (Exception e) {
        }
    }

    public static void update(String id, boolean finalizado) {
        try {
            SqlRequestMB sqlRequest = SqlMB.request(UPDATE, DB_HOMEBANKING);

            sqlRequest.sql = SP_EXEC_UPDATE;
            sqlRequest.parametros.add(id);
            sqlRequest.parametros.add(finalizado);
            sqlRequest.parametros.add(new Timestamp(new Date().getTime()));

            SqlMB.response(sqlRequest);
        } catch (Exception e) {
        }
    }

    public static SqlResponseMB get(String idCobis, Boolean finalizado) {
        SqlRequestMB sqlRequest = SqlMB.request(GET, DB_HOMEBANKING);

        sqlRequest.sql = SP_EXEC_GET;
        sqlRequest.parametros.add(idCobis);
        if (finalizado != null)
            sqlRequest.parametros.add(finalizado);

        return SqlMB.response(sqlRequest);
    }

    public static void delete(String idCobis) {
        try {
            SqlRequestMB sqlRequest = SqlMB.request(DELETE, DB_HOMEBANKING);

            sqlRequest.sql = SP_EXEC_DELETE;
            sqlRequest.parametros.add(idCobis);

            SqlMB.response(sqlRequest);
        } catch (Exception e) {
        }
    }
}
