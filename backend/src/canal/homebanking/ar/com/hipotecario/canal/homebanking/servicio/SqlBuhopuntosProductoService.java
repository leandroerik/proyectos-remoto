package ar.com.hipotecario.canal.homebanking.servicio;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;

public class SqlBuhopuntosProductoService {
    private final static String SP_EXEC_CONSULTA_PROPUESTA = "[Mobile].[dbo].[sp_ConsultarPropuestas]";
    private final static String SP_EXEC_CANJEAR_PROPUESTA = "[Mobile].[dbo].[sp_CanjearPropuesta]";
    private final static String SP_EXEC_CANJEAR_CASHBACK = "[Mobile].[dbo].[sp_CanjearCashback]";
    private final static String SP_EXEC_CONSULTA_CASHBACK_CANJEADO = "[Mobile].[dbo].[sp_ConsultarCashback]";
    private final static String SP_EXEC_OBTENER_CASHBACK_IDCANJE = "[Mobile].[dbo].[sp_ObtenerSiguienteIdCanje]";

    public static Integer consultarCashbackIdCanje(ContextoHB contexto) {
        SqlRequest sqlRequest = Sql.request("ConsultarCashbackIdCanje", "mobile");

        sqlRequest.configurarStoredProcedure(SP_EXEC_OBTENER_CASHBACK_IDCANJE);

        SqlResponse sql = Sql.response(sqlRequest);
        Integer idCanje = sql.registros.get(0).integer("id_canje");

        return idCanje;

    }

    public static List<Objeto> consultarCashbackCanjeado(ContextoHB contexto) {
        SqlRequest sqlRequest = Sql.request("ConsultarCashback", "mobile");
        if (contexto.parametros.existe("id_operacion"))
            sqlRequest.configurarStoredProcedure(SP_EXEC_CONSULTA_CASHBACK_CANJEADO, null,
                    contexto.parametros.string("id_operacion"));
        else
            sqlRequest.configurarStoredProcedure(SP_EXEC_CONSULTA_CASHBACK_CANJEADO, contexto.idCobis());

        SqlResponse sql = Sql.response(sqlRequest);
        return sql.registros;
    }

    public static List<Objeto> consultarPropuestas(ContextoHB contexto) {
        SqlRequest sqlRequest = Sql.request("SelectPropuestas", "mobile");
        if (contexto.parametros.integer("id", null) != null) {
            sqlRequest.configurarStoredProcedure(SP_EXEC_CONSULTA_PROPUESTA, null,
                    contexto.parametros.integer("id", null), null);
        } else {
            sqlRequest.configurarStoredProcedure(SP_EXEC_CONSULTA_PROPUESTA, contexto.idCobis(), null, 'V');
        }

        SqlResponse sql = Sql.response(sqlRequest);
        return sql.registros;
    }

    public static List<Objeto> consultarPropuestasCanjeadas(ContextoHB contexto) {
        SqlRequest sqlRequest = Sql.request("SelectPropuestas", "mobile");
        sqlRequest.configurarStoredProcedure(SP_EXEC_CONSULTA_PROPUESTA, contexto.idCobis(), null, 'C');

        SqlResponse sql = Sql.response(sqlRequest);
        return sql.registros;
    }

    public static void canjearPropuestas(ContextoHB contexto) {
        SqlRequest sqlRequest = Sql.request("CanjearPropuestas", "mobile");
        sqlRequest.configurarStoredProcedure(SP_EXEC_CANJEAR_PROPUESTA, contexto.parametros.integer("id", null),
                new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        Sql.response(sqlRequest);
    }

    public static void insertarCashbackCanjeado(ContextoHB contexto, String idOperacion) {
        SqlRequest sqlRequest = Sql.request("CanjearCashback", "mobile");
        sqlRequest.configurarStoredProcedure(SP_EXEC_CANJEAR_CASHBACK,
                contexto.parametros.integer("id", null),
                new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()),
                contexto.idCobis(),
                idOperacion);
        Sql.response(sqlRequest);
    }

}
