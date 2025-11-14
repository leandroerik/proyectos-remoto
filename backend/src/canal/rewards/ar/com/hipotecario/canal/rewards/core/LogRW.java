package ar.com.hipotecario.canal.rewards.core;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Texto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.rewards.SesionRewards;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.tas.SesionTAS;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;

import java.sql.SQLException;

public class LogRW extends Modulo {

    public static void error(ContextoRewards contexto, Exception e, String eventoDesc) {
        try {
            SesionRewards sesion = contexto.sesion();
            String usuario = sesion.usuario;
            if (usuario != null) {
                Throwable t = getCause(e);
                StackTraceElement st = stackTraceElement(t);
                String exception = t.getClass().getSimpleName();
                String message = t.getMessage();

                String endpoint = contexto.path();
                String evento = eventoDesc != "" ? eventoDesc : "ERROR_CRITICO";
                String datos = Texto.stackTrace(e).substring(0, 500) + " ...continua";
                String error = message != null ? String.format("%s: %s", exception, message) : exception;

                LogRW.insert(contexto, usuario, endpoint, evento, datos, error);
            } else {
                Throwable t = getCause(e);
                StackTraceElement st = stackTraceElement(t);
                String exception = t.getClass().getSimpleName();
                String message = t.getMessage();

                String userParams = contexto.parametros.string("usuario", null);
                String endpoint = contexto.path();
                String evento = eventoDesc != "" ? eventoDesc : "ERROR_CRITICO";
                String datos = Texto.stackTrace(e).substring(0, 500) + " ...continua";
                String error = message != null ? String.format("%s: %s", exception, message) : exception;
                LogRW.insert(contexto, userParams, endpoint, evento, datos, error);
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    public static Futuro<Boolean> insert(ContextoRewards contexto, String usuario,
            String endpoint, String evento, String datos, String error) {
        try {
            String sql = "";
            sql += "INSERT INTO [HipotecarioTAS].[dbo].[RWlog] ";
            sql += "([fecha],[usuario],[endpoint],[evento],[datos],[error],[ip]) ";
            sql += "VALUES (GETDATE(), ?, ?, ?, ?, ?, ?) ";
            Object[] parametros = new Object[6];
            parametros[0] = usuario;
            parametros[1] = endpoint;
            parametros[2] = evento;
            parametros[3] = datos;
            parametros[4] = error;
            parametros[5] = contexto.ip();
            String sqlFinal = sql;
            return new Futuro<>(() -> Sql.update(contexto, "hipotecariotas", sqlFinal, parametros) == 1);
        } catch (Exception e) {
            throw e;
        }
    }
}
