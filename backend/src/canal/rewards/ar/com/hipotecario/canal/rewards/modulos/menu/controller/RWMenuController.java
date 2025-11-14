package ar.com.hipotecario.canal.rewards.modulos.menu.controller;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.rewards.core.RespuestaRW;
import ar.com.hipotecario.canal.rewards.modulos.menu.services.RWMenuService;

public class RWMenuController {

    public static Objeto getMenuUsuarioByUser(ContextoRewards contexto) {
        try {
            String usr = contexto.parametros.string("usuario", "");

            if (usr.isEmpty())
                return RespuestaRW.parametrosIncorrectos(contexto, "Faltan parametros");

            if (!usr.equals(contexto.sesion().usr))
                return RespuestaRW.parametrosIncorrectos(contexto, "Usuario incorrecto");

            return RWMenuService.getMenu(contexto);
        } catch (Exception e) {
            Objeto response = new Objeto();
            response.set("error", RespuestaRW.error(contexto,
                    "RWLoginService.getMenuUsuarioByUser - Exception", e, "ERROR_MENU"));

            return response;
        }
    }

}
