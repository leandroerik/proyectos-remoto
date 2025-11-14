package ar.com.hipotecario.canal.tas.shared.modulos.apis.notificaciones.controllers;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.notificaciones.servicios.TASRestNotificaciones;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.notificaciones.modelos.TASNotificacion;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.notificaciones.modelos.TASNotificacionResponse;

import java.util.List;

public class TASNotificacionesController {
    public static Objeto getNotificaciones(ContextoTAS contexto) {
        try {
            String idCliente = contexto.parametros.string("idCliente");
            boolean priorizada = contexto.parametros.bool("priorizada", true);
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            List<TASNotificacion> response = TASRestNotificaciones.getNotificaciones(contexto, idCliente, priorizada);
            if (response == null)
                return RespuestaTAS.sinResultados(contexto, "No existen datos para la consulta solicitada");
            return new Objeto().set("notificaciones", response);
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TasNotificacionesController - getNotificaciones()", e);
        }
    }

    public static Objeto postNotificaciones(ContextoTAS contexto) {
        try {
            String idCliente = contexto.parametros.string("idCliente");
            if (idCliente.isEmpty() || !idCliente.contentEquals(contexto.sesion().clienteTAS.getIdCliente()))
                return RespuestaTAS.sinParametros(contexto, "idCliente vacio");
            int idAlerta = contexto.parametros.integer("idAlerta");
            String textoAlerta = contexto.parametros.string("textoAlerta");
            String textoRespuesta = contexto.parametros.string("textoRespuesta");
            String tipoRespuesta = contexto.parametros.string("tipoRespuesta");
            TASNotificacionResponse notificacionResponse = new TASNotificacionResponse(idAlerta, textoAlerta,
                    textoRespuesta, tipoRespuesta);
            boolean response = TASRestNotificaciones.postNotificaciones(contexto, idCliente, notificacionResponse);
            return new Objeto().set("alerta", response);
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TasNotificacionesController - getNotificaciones()", e);
        }
    }

    public static Objeto envioMail(ContextoTAS contexto, Objeto datos){
        try {
            Objeto datosAPI = generarDatosAPI(datos);
            Objeto response = TASRestNotificaciones.postEnviarMail(contexto, datosAPI);
            Objeto resp = new Objeto();
            resp.set("estado", "OK");
            resp.set("response", response);
            return resp;
        }catch (Exception e){
            Objeto error = new Objeto();
            error.set("estado", "ERROR");
            error.set("error", e);
            return error;
        }
    }

    private static Objeto generarDatosAPI(Objeto datos){
        Objeto datosApi = new Objeto();
        datosApi.set("asunto", datos.string("asunto"));
        datosApi.set("async", true);
        datosApi.set("de", "info@mail-hipotecario.com.ar");
        datosApi.set("nombreDe", datos.string("nombreRemitente"));
        datosApi.set("para", datos.string("correo"));
        datosApi.set("plantilla", datos.string("plantilla"));
        datosApi.set("base64", datos.string("file"));
        datosApi.set("nombreArchivo", datos.string("nombre"));
        datosApi.set("subject", datos.string("asunto"));
        datosApi.set("body", "");
        return datosApi;
    }
}
