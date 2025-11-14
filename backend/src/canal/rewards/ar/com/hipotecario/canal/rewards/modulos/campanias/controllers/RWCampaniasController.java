package ar.com.hipotecario.canal.rewards.modulos.campanias.controllers;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.rewards.core.RespuestaRW;
import ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.services.RWAjustePuntosService;
import ar.com.hipotecario.canal.rewards.modulos.campanias.model.QueryConsultaCampaniaDetalle;
import ar.com.hipotecario.canal.rewards.modulos.campanias.model.QueryConsultaCampanias;
import ar.com.hipotecario.canal.rewards.modulos.campanias.model.QueryModificacionCampania;
import ar.com.hipotecario.canal.rewards.modulos.campanias.services.RWCampaniasService;
import ar.com.hipotecario.canal.rewards.core.ValidacionPermisos;
import ar.com.hipotecario.canal.rewards.core.ValidacionPermisos.Permisos;

import java.util.List;

public class RWCampaniasController {

    static public Objeto consultarCampanias(ContextoRewards contexto) {

        String fechaDesde = contexto.parametros.string("fechaDesde");
        String fechaHasta = contexto.parametros.string("fechaHasta");
        String estado = contexto.parametros.string("estado");
        String secuencial = contexto.parametros.string("secuencial", "");
        String usuario = contexto.parametros.string("usuario", "");

        if (fechaDesde.isEmpty() && fechaHasta.isEmpty()
                && estado.isEmpty()) {
            return RespuestaRW.sinParametros(contexto, "Ingrese parametros para la busqueda");
        }

        QueryConsultaCampanias query = new QueryConsultaCampanias();

        query.setEstado(estado);
        query.setFechaDesde(fechaDesde);
        query.setFechaHasta(fechaHasta);
        query.setSecuencial(secuencial);
        query.setUsuario(!usuario.isEmpty() ? usuario : null);

        Objeto response = RWCampaniasService.consultarCampanias(contexto, query);
        return response;

    }

    static public Objeto consultarCampaniaDetalle(ContextoRewards contexto) {

        String codigo = contexto.parametros.string("codigo");
        String secuencial = contexto.parametros.string("secuencial", "");
        String usuario = contexto.parametros.string("usuario", "");

        if (codigo.isEmpty()) {
            return RespuestaRW.sinParametros(contexto, "Falta el parametro codigo");
        }

        QueryConsultaCampaniaDetalle query = new QueryConsultaCampaniaDetalle();

        query.setCodigo(codigo);
        query.setSecuencial(secuencial);
        query.setUsuario(!usuario.isEmpty() ? usuario : null);

        Objeto response = RWCampaniasService.consultarCampaniaDetalle(contexto, query);
        return response;

    }

    public static Object actualizarEstadoCampania(ContextoRewards contexto) {

        if (!ValidacionPermisos.validarPermisos(contexto, ValidacionPermisos.Permisos.MODIFICAR_CAMPANIA)) {
            Objeto respuesta = new Objeto();
            respuesta.set("status", 403);
            respuesta.set("message", "Sin permisos para realizar la operacion");
            respuesta.set("error", RespuestaRW.error(contexto,
                    "RWAjustePuntosService.postSolicitarAjuste - ApiException",
                    new Exception("Sin permisos para realizar la operacion"), "ERROR_PERMISOS_AUTORIZAR_AJUSTE"));
            return respuesta;
        }

        String secuencial = contexto.parametros.string("secuencial");
        String estado = contexto.parametros.string("estado"); //A - R
        String usuario = contexto.parametros.string("usuario", "");

        if (!(estado.equals("A") || estado.equals("R"))){
            Objeto respuesta = new Objeto();
            respuesta.set("status", 403);
            respuesta.set("message", "Ingrese un estado correcto (A - R)");
            respuesta.set("error", RespuestaRW.error(contexto,
                    "RWCampaniasService.actualizarEstadoCampania - ApiException",
                    new Exception("Estado incorrecto"), "ERROR_ESTADO_CAMPANIA"));
            return respuesta;
        }

        QueryModificacionCampania query = new QueryModificacionCampania();

        query.setSecuencial(secuencial);
        query.setEstado(estado);
        query.setUsuario(!usuario.isEmpty() ? usuario : null);

        return RWCampaniasService.actualizarEstadoCampania(contexto, query);
    }
}
