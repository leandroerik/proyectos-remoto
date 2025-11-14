package ar.com.hipotecario.canal.rewards.modulos.posicion_consolidada.controllers;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.rewards.middleware.models.dto.BffResponse;
import ar.com.hipotecario.canal.rewards.middleware.models.negocio.RWBeneficioAdherido;
import ar.com.hipotecario.canal.rewards.modulos.posicion_consolidada.services.PosicionConsolidadaService;
import java.util.List;

public class PosicionConsolidadaController {
    public static Objeto getBeneficiosAdheridosByCobis(ContextoRewards contexto) {
        String cobis = contexto.parametros.string("idCobis");

        Objeto rta = new Objeto();
        rta = PosicionConsolidadaService.getBeneficiosAdheridosByCobis(contexto, cobis);

        return rta;
    }

    public static Objeto getBeneficiosAdheridosMensualByCobis(
            ContextoRewards contexto) {

        String cobis = contexto.parametros.string("id"); // requerido
        String fechaDesde = contexto.parametros.string("fechaDesde"); // solo requerido si ultimoAnio = false
        String fechaHasta = contexto.parametros.string("fechaHasta"); // solo requerido si ultimoAnio = false
        Boolean ultimoAnio = contexto.parametros.bool("ultimoanio");
        int pagina = contexto.parametros.integer("siguiente");

        Objeto response = PosicionConsolidadaService
                .getBeneficiosAdheridosMensualByCobis(contexto, cobis, fechaDesde,
                        fechaHasta, ultimoAnio, pagina);

        return response;
    }

    public static Objeto getHistoricoNovedades(ContextoRewards contexto) {
        String cobis = contexto.parametros.string("id"); // requerido
        String fechaDesde = contexto.parametros.string("fechaDesde"); // solo requerido si ultimoAnio = false
        String fechaHasta = contexto.parametros.string("fechaHasta"); // solo requerido si ultimoAnio = false

        Objeto response = PosicionConsolidadaService
                .getHistoricoNovedades(contexto, cobis, fechaDesde, fechaHasta);
        return response;
    }
}
