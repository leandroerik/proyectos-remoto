package ar.com.hipotecario.canal.rewards.core;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.exception.SqlException;
import ar.com.hipotecario.canal.rewards.ContextoRewards;

import java.sql.SQLException;

public class RespuestaRW extends Objeto {

    private EstadosRespuesta descripcion;
    private Objeto valor;

    public enum EstadosRespuesta {
        EXITO("EXITO"),
        SIN_RESULTADOS("SIN_RESULTADOS"),

        ERROR("ERROR"),
        ERROR_GENERICO("ERROR_GENERICO"),
        ERROR_API("ERROR_API"),
        ERROR_SQL("ERROR_SQL"),

        PARAMETROS_INCORRECTOS("PARAMETROS_INCORRECTOS"),
        SIN_PARAMETROS("SIN_PARAMETROS"),
        SESION_ACTIVA("SESION_ACTIVA"),
        DOC_DUP("DUP"),
        DOC_TIP("TIP");

        private final String descripcion;

        EstadosRespuesta(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }

    }

    public RespuestaRW() {
    }

    public RespuestaRW(EstadosRespuesta descripcion) {
        this.descripcion = descripcion;
    }

    public RespuestaRW(EstadosRespuesta descripcion, Objeto valor) {
        this.descripcion = descripcion;
        this.valor = valor;
    }

    public static Objeto exito(String clave, Objeto valor) {
        Objeto rta = new Objeto();
        if (clave != null && clave.equals(EstadosRespuesta.EXITO.getDescripcion())) {
            rta.set("estado", EstadosRespuesta.EXITO.getDescripcion());
            rta.add(valor);
        } else {
            rta.set("estado", EstadosRespuesta.ERROR.getDescripcion());
        }
        return rta;
    }

    public static Objeto exito() {
        Objeto rta = new Objeto();
        rta.set("estado", EstadosRespuesta.EXITO.getDescripcion());
        return rta;
    }

    public static Objeto sinResultados(ContextoRewards contexto, String descripcion) {
        Objeto rta = new Objeto();
        rta.set("estado", EstadosRespuesta.SIN_RESULTADOS.getDescripcion());
        rta.set("descripcion", descripcion);
        return rta;
    }

    public static Objeto error(ContextoRewards contexto, String descripcion) {
        Objeto rta = new Objeto();
        rta.set("estado", EstadosRespuesta.ERROR_GENERICO.getDescripcion());
        rta.set("descripcion", descripcion);
        rta.set("status", 500);
        // LogTAS.evento(contexto, "ERROR_GENERICO", rta);
        return rta;
    }

    public static Objeto error(ContextoRewards contexto, String tipoMensaje, String mensaje, String motivo) {
        Objeto rta = new Objeto();
        rta.set("estado", EstadosRespuesta.ERROR_GENERICO.getDescripcion());
        rta.set("tipo", tipoMensaje);
        rta.set("mensaje", mensaje);
        rta.set("motivo", motivo);
        // LogTAS.evento_validacion_baja_ca(contexto, rta);
        return rta;
    }

    public static Objeto error(ContextoRewards contexto, String descripcion, Exception exceptionError,
            String eventoDesc) {
        Objeto rta = new Objeto();
        if (exceptionError instanceof ApiException) {
            ApiException apiException = (ApiException) exceptionError;
            rta.set("estado", EstadosRespuesta.ERROR_API.getDescripcion());
            rta.set("codigoHttp", apiException.response.codigoHttp);
            rta.set("status", apiException.response.codigoHttp);
            rta.set("codigo", apiException.response.get("codigo"));
            rta.set("mensajeAlUsuario", apiException.response.get("mensajeAlUsuario"));
            rta.set("masInformacion", apiException.response.get("masInformacion"));
            rta.set("ubicacion", apiException.response.get("ubicacion"));
            LogRW.error(contexto, apiException, eventoDesc);
            return rta;
        }
        if (exceptionError instanceof SqlException) {
            SqlException sqlException = (SqlException) exceptionError;
            rta.set("estado", EstadosRespuesta.ERROR_SQL.getDescripcion());
            rta.set("query", sqlException.query);
            rta.set("causa", sqlException.codigoError);
            rta.set("mensaje", sqlException.getMessage());
            rta.set("ubicacion", descripcion);
            // LogTAS.error(contexto, sqlException);
            return rta;
        }
        if (exceptionError instanceof SQLException) {
            SQLException sqlException = (SQLException) exceptionError;
            rta.set("estado", EstadosRespuesta.ERROR_SQL.getDescripcion());
            rta.set("codigo", sqlException.getErrorCode());
            rta.set("causa", sqlException.getCause());
            rta.set("mensaje", sqlException.getMessage());
            rta.set("ubicacion", descripcion);
            // LogTAS.error(contexto, sqlException);
            return rta;
        }

        rta.set("estado", EstadosRespuesta.ERROR.getDescripcion());
        rta.set("causa", exceptionError.getCause());
        rta.set("mensaje", exceptionError.getMessage());
        rta.set("ubicacion", descripcion);
        rta.set("status", 500);
        LogRW.error(contexto, exceptionError, eventoDesc);
        return rta;
    }

    public static Objeto parametrosIncorrectos(ContextoRewards contexto, String descripcion) {
        Objeto rta = new Objeto();
        rta.set("estado", EstadosRespuesta.PARAMETROS_INCORRECTOS.getDescripcion());
        rta.set("descripcion", descripcion);
        return rta;
    }

    public static Objeto sinParametros(ContextoRewards contexto, String descripcion) {
        Objeto rta = new Objeto();
        rta.set("estado", EstadosRespuesta.SIN_PARAMETROS.getDescripcion());
        rta.set("descripcion", descripcion);
        return rta;
    }

    public static Objeto sesionActiva(ContextoRewards contexto, String descripcion) {
        Objeto rta = new Objeto();
        rta.set("estado", EstadosRespuesta.SESION_ACTIVA.getDescripcion());
        rta.set("descripcion", descripcion);
        return rta;
    }

    public static Objeto docDuplicado(String tipo, String descripcion) {
        Objeto rta = new Objeto();
        String estado = tipo.equals("DUP") ? EstadosRespuesta.DOC_DUP.getDescripcion()
                : EstadosRespuesta.DOC_TIP.getDescripcion();
        rta.set("estado", estado);
        rta.set("descripcion", descripcion);
        return rta;
    }

}
