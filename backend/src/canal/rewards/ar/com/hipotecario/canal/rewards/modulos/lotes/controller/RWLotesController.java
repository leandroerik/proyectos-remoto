package ar.com.hipotecario.canal.rewards.modulos.lotes.controller;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.rewards.core.RespuestaRW;
import ar.com.hipotecario.canal.rewards.modulos.lotes.services.RWLotesService;

public class RWLotesController
{
    static public Objeto LotesTotales(ContextoRewards contexto) {

        //QUE PARAMETROS SON OBLIGATORIOS?
        String codigoMovimiento = contexto.parametros.string("codigoMovimiento");
        //String estado = contexto.parametros.string("estado");
        String fechaDesde = contexto.parametros.string("fechaDesde","");
        String fechaHasta = contexto.parametros.string("fechaHasta", "");
        String novedadAlta = contexto.parametros.string("novedadAlta", "false");
        String novedadBaja = contexto.parametros.string("novedadBaja", "false");
        String novedadDatosFiliatorios = contexto.parametros.string("novedadDatosFiliatorios", "false");
        String novedadMora = contexto.parametros.string("novedadMora", "false");
        String producto = contexto.parametros.string("producto", "");
        String secuencial = contexto.parametros.string("secuencial", "0");
        String tipoFecha = contexto.parametros.string("tipoFecha", "0");

        if (fechaDesde.isEmpty() && fechaHasta.isEmpty()
                && codigoMovimiento.isEmpty()) {
            return RespuestaRW.sinParametros(contexto, "Ingrese parametros para la busqueda");
        }

        Objeto response = RWLotesService.postLotesTotales(contexto,
                                                         codigoMovimiento,
                                                         fechaDesde,
                                                         fechaHasta,
                                                         novedadAlta,
                                                         novedadBaja,
                                                         novedadDatosFiliatorios,
                                                         novedadMora,
                                                         producto,
                                                         secuencial,
                                                         tipoFecha );
        return response;
    }
}
