package ar.com.hipotecario.canal.rewards.modulos.login.controller;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.rewards.modulos.login.services.RWLoginService;

public class RWLoginController {

    public static boolean checkSession(ContextoRewards contexto) {
        String usr = contexto.parametros.string("userLogged");
        String usrSession = contexto.sesion().usr;
        return usr.equals(usrSession);
    }

    public static Objeto login(ContextoRewards contexto) {
        String usr = contexto.parametros.string("usuario");
        String clave = contexto.parametros.string("clave");

        return RWLoginService.login(contexto, usr, clave);
    }

    public static Objeto usuario(ContextoRewards contexto) {
        String usr = contexto.sesion().usr;
        String rol = contexto.sesion().rol;
        Objeto respuesta = new Objeto();
        if (usr == null) {
            respuesta.set("usuario", "Sin Sesion");
            return respuesta;
        }
        respuesta.set("usuario", usr);
        respuesta.set("rol", rol);

        return respuesta;
    }

    public static Objeto logOut(ContextoRewards contexto) {
        Objeto respuesta = new Objeto();
        contexto.eliminarSesion();

        respuesta.set("goodbye", true);
        return respuesta;
    }
}
