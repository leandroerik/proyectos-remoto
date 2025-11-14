package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;

public class SqlRegistroDispositivo {

    private static final String CONSULTAR_REGISTRO_DISPOSITIVO_ULTIMAS24HS_POR_COBIS = "SELECT * FROM [Mobile].[dbo].[registro_dispositivo] WHERE [id_cobis] = ? AND [fecha_alta] > GETDATE() - 1";

    /**
     * Busca si el usuario registró dispositivo en las últimas 24 horas.
     *
     * @param idCobis
     * @return response
     */
    public static SqlResponse obtenerRegistroDispositivoUltimas24hsPorCobis(String idCobis) {
        SqlRequest sqlRequest = Sql.request("ConsultarRegistroDispositivoUltimas24hs", "mobile");
        sqlRequest.sql = CONSULTAR_REGISTRO_DISPOSITIVO_ULTIMAS24HS_POR_COBIS;
        sqlRequest.add(idCobis);

        return Sql.response(sqlRequest);
    }

}
