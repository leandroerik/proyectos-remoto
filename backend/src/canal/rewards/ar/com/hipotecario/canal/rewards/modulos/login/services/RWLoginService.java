package ar.com.hipotecario.canal.rewards.modulos.login.services;

import java.util.List;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.util.LoginLDAP;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.rewards.core.RespuestaRW;
import ar.com.hipotecario.canal.rewards.modulos.login.models.LoginLdapResponse;
import ar.com.hipotecario.canal.rewards.modulos.menu.services.RWMenuService;

public class RWLoginService {
    public static Objeto login(ContextoRewards contexto, String usr, String clave) {
        Objeto response = new Objeto();

        try {
            LoginLdapResponse ldap = LoginLDAP.loginLdpaRW(contexto, usr, clave);
            Boolean loginExitoso = ldap.isAutenticado();

            response.set("autenticado", false);
            response.set("check1", loginExitoso.toString());

            if (loginExitoso) {

                List<String> listaRoles = buscarGrupos(ldap.getRoles());
                String rolLdap = listaRoles.get(0);

                if (rolLdap == null) {
                    response.set("autenticado", false);
                    return response;
                }

                contexto.sesion().usr = usr;
                contexto.sesion().idCobis = usr;
                contexto.sesion().rol = rolLdap;
                Objeto menu = RWMenuService.getMenu(contexto);

                response.set("menu", menu);
                response.set("rol", rolLdap);
                response.set("autenticado", true);
                response.set("nombre", ldap.getNombre());
                response.set("usuario", ldap.getUsuario());
            }

            Objeto responseData = new Objeto();
            responseData.set("status", 0);
            responseData.set("data", response);

            return responseData;
        } catch (Exception e) {
            response.set("error", RespuestaRW.error(contexto,
                    "RWLoginService.login - Exception", e, "ERROR_LOGIN"));
            return response;
        }
    }

    private static List<String> buscarGrupos(List<String> strings) {
        final Config config = new Config();
        String entorno = config.string("rewards_entorno");
        String autorizador = config.string("rewards_rol_autorizador");
        String operador = config.string("rewards_rol_operador");
        return strings.stream()
                .filter(s -> s.contains(operador + "_" + entorno)
                        || s.contains(autorizador + "_" + entorno))
                .map(s -> {
                    if (s.equals(operador + "_" + entorno))
                        return operador;

                    if (s.equals(autorizador + "_" + entorno))
                        return autorizador;

                    return null;
                })
                .collect(Collectors.toList());
    }

}
