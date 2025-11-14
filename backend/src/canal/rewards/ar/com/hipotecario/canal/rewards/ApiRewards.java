package ar.com.hipotecario.canal.rewards;

import ar.com.hipotecario.backend.Servidor;
import ar.com.hipotecario.canal.rewards.modulos.RWAplicacion;
import ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.controllers.AjustePuntosController;
import ar.com.hipotecario.canal.rewards.modulos.buscar_cliente.controller.RWBuscarClienteController;
import ar.com.hipotecario.canal.rewards.modulos.campanias.controllers.RWCampaniasController;
import ar.com.hipotecario.canal.rewards.modulos.login.controller.RWLoginController;
import ar.com.hipotecario.canal.rewards.modulos.lotes.controller.RWLotesController;
import ar.com.hipotecario.canal.rewards.modulos.menu.controller.RWMenuController;
import ar.com.hipotecario.canal.rewards.modulos.posicion_consolidada.controllers.PosicionConsolidadaController;
import ar.com.hipotecario.canal.rewards.modulos.rechazos.controllers.RWRechazosController;

public class ApiRewards extends CanalRewards {

    public static void main(String[] args) throws Exception {
        Servidor.main(args);
    }

    public static void iniciar() {

        get("/rewards/api/health", contexto -> RWAplicacion.health(contexto), SEGURIDAD_INVITADO);
        post("/rewards/api/login", contexto -> RWLoginController.login(contexto),
                SEGURIDAD_INVITADO);
        get("/rewards/api/checkSession", contexto -> RWLoginController.checkSession(contexto));
        post("/rewards/api/logout", contexto -> RWLoginController.logOut(contexto));
        get("/rewards/api/usuario", contexto -> RWLoginController.usuario(contexto));

        // Menu
        get("/rewards/api/menu", contexto -> RWMenuController.getMenuUsuarioByUser(contexto));

        // Cliente
        get("/rewards/api/cliente", contexto -> RWBuscarClienteController.buscarCliente(contexto));
        get("rewards/api/beneficios",
                contexto -> PosicionConsolidadaController.getBeneficiosAdheridosByCobis(contexto));
        get("rewards/api/beneficios-mensual", contexto -> PosicionConsolidadaController
                .getBeneficiosAdheridosMensualByCobis(contexto));
        get("rewards/api/historico-novedades",
                contexto -> PosicionConsolidadaController.getHistoricoNovedades(contexto));

        // Ajuste
        get("rewards/api/ajuste-puntos",
                contexto -> AjustePuntosController.consultarAjuste(contexto));
        post("rewards/api/ajuste-puntos/solicitud",
                contexto -> AjustePuntosController.solicitarAjuste(contexto));
        post("rewards/api/ajuste-puntos/modificar",
                contexto -> AjustePuntosController.autorizarAjuste(contexto));

        // Campañascd
        get("rewards/api/campanias",
                contexto -> RWCampaniasController.consultarCampanias(contexto));
        get("rewards/api/campanias/detalle",
                contexto -> RWCampaniasController.consultarCampaniaDetalle(contexto));
        patch("rewards/api/campanias",
                contexto -> RWCampaniasController.actualizarEstadoCampania(contexto));

        // Lotes
        post("rewards/api/lotes/totales", contexto -> RWLotesController.LotesTotales(contexto));

        //Codigos Rechazos
        get("/rewards/api/codigos/rechazos_ed",
                contexto -> RWRechazosController.getRechazosEd(contexto));
        get("/rewards/api/codigos/rechazos_ar",
                contexto -> RWRechazosController.getRechazosAr(contexto));

        get("/rewards/api/codigos/documentosrewards",
                contexto -> RWRechazosController.getDocumentosRewards(contexto));
        get("/rewards/api/codigos/actividades",
                contexto -> RWRechazosController.getActividades(contexto));
        get("/rewards/api/codigos/estadosciviles",
                contexto -> RWRechazosController.getEstadosCiviles(contexto));
        get("/rewards/api/codigos/sexos", contexto -> RWRechazosController.getSexos(contexto));
        get("rewards/api/rechazos", contexto -> RWRechazosController.getRechazosClientes(contexto));
        post("rewards/api/rechazos", contexto -> RWRechazosController.postRechazos(contexto));

    }
}
