package ar.com.hipotecario.canal.rewards.modulos.buscar_cliente.controller;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.rewards.core.RespuestaRW;
import ar.com.hipotecario.canal.rewards.modulos.buscar_cliente.services.RWBuscarClienteService;

public class RWBuscarClienteController {
    public static Objeto buscarCliente(ContextoRewards contexto) {
        String cobis = contexto.parametros.string("idCobis", "");
        String documento = contexto.parametros.string("doc", "");
        if (documento.isEmpty() && cobis.isEmpty())
            return RespuestaRW.sinParametros(contexto, "Ingrese parametros para la busqueda");

        return RWBuscarClienteService.buscarCliente(contexto, cobis, documento);

    }

}
