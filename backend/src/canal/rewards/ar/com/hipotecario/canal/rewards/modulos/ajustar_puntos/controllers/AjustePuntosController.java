package ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.controllers;

import java.util.ArrayList;
import java.util.List;

import com.azure.security.keyvault.secrets.implementation.models.Error;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.rewards.core.RespuestaRW;
import ar.com.hipotecario.canal.rewards.core.ValidacionPermisos;
import ar.com.hipotecario.canal.rewards.core.ValidacionPermisos.Permisos;
import ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.model.AjusteModificaciones;
import ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.model.AprovacionAjuste;
import ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.model.QueryConsultaAjustes;
import ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.services.RWAjustePuntosService;

public class AjustePuntosController {

    static public Objeto consultarAjuste(ContextoRewards contexto) {

        String fechaDesde = contexto.parametros.string("fechaDesde");
        String fechaHasta = contexto.parametros.string("fechaHasta");
        String estado = contexto.parametros.string("estado");
        String usuario = contexto.parametros.string("usuario", "");

        if (fechaDesde.isEmpty() && fechaHasta.isEmpty()
                && estado.isEmpty()
                && usuario.isEmpty()) {
            return RespuestaRW.sinParametros(contexto, "Ingrese parametros para la busqueda");
        }

        QueryConsultaAjustes query = new QueryConsultaAjustes();

        query.setEstado(estado);
        query.setFechaDesde(fechaDesde);
        query.setFechaHasta(fechaHasta);
        query.setUsuario(!usuario.isEmpty() ? usuario : null);

        Objeto response = RWAjustePuntosService.consultarAjustes(contexto, query);
        return response;

    }

    static public Objeto solicitarAjuste(ContextoRewards contexto) {

        if (!ValidacionPermisos.validarPermisos(contexto, Permisos.SOLICITAR_AJUSTE)) {
            Objeto respuesta = new Objeto();
            respuesta.set("status", 401);
            respuesta.set("message", "Sin permisos para realizar la operacion");
            respuesta.set("error", RespuestaRW.error(contexto,
                    "RWAjustePuntosService.postSolicitarAjuste - ApiException",
                    new Exception("Sin permisos para realizar la operacion"), "ERROR_PERMISOS_SOLICITAR_AJUSTE"));
            return respuesta;
        }

        String cliente = contexto.parametros.string("cliente");
        Integer puntos = contexto.parametros.integer("puntos");
        String observacion = contexto.parametros.string("observacion");

        Objeto response = RWAjustePuntosService.postSolicitarAjuste(contexto, cliente, puntos, observacion);
        return response;

    }

    static public Objeto autorizarAjuste(ContextoRewards contexto) {
        // if (!contexto.sesion().rol.equals("H_REWARDS_AUTORIZADOR")) {
        if (!ValidacionPermisos.validarPermisos(contexto, Permisos.AUTORIZAR_AJUSTE)) {
            Objeto respuesta = new Objeto();
            respuesta.set("status", 403);
            respuesta.set("message", "Sin permisos para realizar la operacion");
            respuesta.set("error", RespuestaRW.error(contexto,
                    "RWAjustePuntosService.postSolicitarAjuste - ApiException",
                    new Exception("Sin permisos para realizar la operacion"), "ERROR_PERMISOS_AUTORIZAR_AJUSTE"));
            return respuesta;
        }

        String usuario = contexto.parametros.string("usuario");
        List<AjusteModificaciones> modificaciones = contexto.parametros.objetos("modificaciones",
                AjusteModificaciones.class);

        AprovacionAjuste aprobacion = new AprovacionAjuste(modificaciones, usuario);
        return RWAjustePuntosService.postAprovarAjuste(contexto, aprobacion);
    }
}
