package ar.com.hipotecario.canal.tas;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.exception.SqlException;

import java.sql.SQLException;

public class RespuestaTAS extends Objeto {

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

    public RespuestaTAS() {
    }

    public RespuestaTAS(EstadosRespuesta descripcion) {
        this.descripcion = descripcion;
    }

    public RespuestaTAS(EstadosRespuesta descripcion, Objeto valor) {
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

    public static Objeto sinResultados(ContextoTAS contexto, String descripcion) {
        Objeto rta = new Objeto();
        rta.set("estado", EstadosRespuesta.SIN_RESULTADOS.getDescripcion());
        rta.set("descripcion", descripcion);
        return rta;
    }

    public static Objeto error(ContextoTAS contexto, String descripcion) {
        Objeto rta = new Objeto();
        rta.set("estado", EstadosRespuesta.ERROR_GENERICO.getDescripcion());
        rta.set("descripcion", descripcion);
        LogTAS.evento(contexto, "ERROR_GENERICO", rta);
        return rta;
    }
    public static Objeto errorLoginTS(ContextoTAS contexto, String msg){
        Objeto rta = new Objeto();
        rta.set("estado", EstadosRespuesta.ERROR_GENERICO.getDescripcion());
        if(msg.contains("USER_LOCKED")){
            rta.set("codigo", "USER_LOCKED");
        } else if(msg.contains("INVALID_RESPONSE")){
            rta.set("codigo", "INVALID_RESPONSE");
        } else if(msg.contains("PASSWORD_EXPIRED")){
            rta.set("codigo", "PASSWORD_CHANGE_REQUIRED");
        } else if(msg.contains("STATE_ID")){
            rta.set("codigo", "El stateId es Requerido");
        }
        rta.set("mensaje", msg.substring(msg.indexOf("-") + 2));
        LogTAS.evento(contexto,rta.string("tipo"), rta);
        return rta;
    }

    public static Objeto error(ContextoTAS contexto, String tipoMensaje, String mensaje, String motivo) {
        Objeto rta = new Objeto();
        rta.set("estado", EstadosRespuesta.ERROR_GENERICO.getDescripcion());
        rta.set("tipo", tipoMensaje);
        rta.set("mensaje", mensaje);
        rta.set("motivo", motivo);
        LogTAS.evento(contexto,rta.string("tipo"), rta);
        return rta;
    }

    public static Objeto error(ContextoTAS contexto, String descripcion, Exception exceptionError) {
        Objeto rta = new Objeto();
        if (exceptionError instanceof ApiException) {
            ApiException apiException = (ApiException) exceptionError;
            rta.set("estado", EstadosRespuesta.ERROR_API.getDescripcion());
            rta.set("codigoHttp", apiException.response.codigoHttp);
            rta.set("codigo", apiException.response.get("codigo"));
            rta.set("mensajeAlUsuario", apiException.response.get("mensajeAlUsuario"));
            rta.set("masInformacion", apiException.response.get("masInformacion"));
            rta.set("ubicacion", apiException.response.get("ubicacion"));
            rta.set("descripcion", descripcion);
            LogTAS.error(contexto, apiException);
            return rta;
        }
        if (exceptionError instanceof SqlException) {
            SqlException sqlException =  (SqlException) exceptionError;
            rta.set("estado", EstadosRespuesta.ERROR_SQL.getDescripcion());
            rta.set("query", sqlException.query);
            rta.set("causa", sqlException.codigoError);
            rta.set("mensaje", sqlException.getMessage());
            rta.set("ubicacion", descripcion);
            LogTAS.error(contexto, sqlException);
            return rta;
        }
        if (exceptionError instanceof SQLException) {
            SQLException sqlException =  (SQLException) exceptionError;
            rta.set("estado", EstadosRespuesta.ERROR_SQL.getDescripcion());
            rta.set("codigo", sqlException.getErrorCode());
            rta.set("causa", sqlException.getCause());
            rta.set("mensaje", sqlException.getMessage());
            rta.set("ubicacion", descripcion);
            LogTAS.error(contexto, sqlException);
            return rta;
        }
        rta.set("estado", EstadosRespuesta.ERROR.getDescripcion());
        rta.set("causa", exceptionError.getCause());
        rta.set("mensaje", exceptionError.getMessage());
        rta.set("ubicacion", descripcion);
        LogTAS.error(contexto, exceptionError);
        return rta;
    }

    public static Objeto parametrosIncorrectos(ContextoTAS contexto, String descripcion) {
        Objeto rta = new Objeto();
        rta.set("estado", EstadosRespuesta.PARAMETROS_INCORRECTOS.getDescripcion());
        rta.set("descripcion", descripcion);
        return rta;
    }

    public static Objeto sinParametros(ContextoTAS contexto, String descripcion) {
        Objeto rta = new Objeto();
        rta.set("estado", EstadosRespuesta.SIN_PARAMETROS.getDescripcion());
        rta.set("descripcion", descripcion);
        return rta;
    }

    public static Objeto sesionActiva(ContextoTAS contexto, String descripcion) {
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
