package ar.com.hipotecario.canal.rewards.modulos.posicion_consolidada.services;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.rewards.core.RespuestaRW;
import ar.com.hipotecario.canal.rewards.middleware.apis.RWApiRewards;
import ar.com.hipotecario.canal.rewards.middleware.models.dto.BffResponse;
import ar.com.hipotecario.canal.rewards.middleware.models.negocio.RWBeneficioAdherido;
import ar.com.hipotecario.canal.rewards.middleware.models.negocio.RWBeneficioAdquirido;
import ar.com.hipotecario.canal.rewards.middleware.models.negocio.RWBeneficioNovedad;

public class PosicionConsolidadaService {
    public static Objeto getBeneficiosAdheridosByCobis(ContextoRewards context, String idCobis) {
        try {
            List<RWBeneficioAdquirido> resultados = new ArrayList<>();
            resultados = RWApiRewards.getBeneficionsRewardsByIdCobis(context, idCobis);
            Objeto resultadosResponse = new Objeto();

            for (RWBeneficioAdquirido obj : resultados) {
                resultadosResponse.add(obj.objeto());
            }

            Objeto response = new Objeto();
            response.set("status", 0);
            response.set("data", resultadosResponse);
            return response;

        } catch (ApiException e) {
            return (!e.response.codigoHttp.equals(204))
                    ? RespuestaRW.error(context, "SIN_DATOS")
                    : RespuestaRW.error(context,
                            "PosicionConsolidadaService.getBeneficiosAdheridosByCobis - ApiException", e, "");

        } catch (Exception e) {
            return RespuestaRW.error(context,
                    "PosicionConsolidadaService.getBeneficiosAdheridosByCobis - Exception", e, "");
        }
    }

    public static Objeto getBeneficiosAdheridosMensualByCobis(ContextoRewards context,
            String idCobis,
            String fechaDesde,
            String fechaHasta, Boolean ultimoAnio, int pagina) {

        Objeto response = new Objeto();

        try {
            List<RWBeneficioAdherido> resultados = new ArrayList<>();
            Objeto resultadosResponse = new Objeto();

            resultados = RWApiRewards.getBeneficionsRewardsMensualByIdCobis(context, idCobis, fechaDesde, fechaHasta,
                    ultimoAnio, pagina);

            for (RWBeneficioAdherido obj : resultados) {
                resultadosResponse.add(obj.objeto());
            }

            response.set("status", 0);
            response.set("data", resultadosResponse);

            return response;

        } catch (ApiException e) {
            if (e.response.codigoHttp.equals(204)) {
                response.set("error", RespuestaRW.error(context, "SIN_DATOS"));
            } else {
                response.set("error", RespuestaRW.error(context,
                        "PosicionConsolidadaService.getBeneficiosAdheridosMensualByCobis - ApiException", e, ""));
            }

            return response;
        } catch (Exception e) {
            response.set("error", RespuestaRW.error(context,
                    "PosicionConsolidadaService.getBeneficiosAdheridosMensualByCobis - Exception", e, ""));

            return response;
        }
    }

    public static Objeto getHistoricoNovedades(ContextoRewards context,
            String idCobis,
            String fechaDesde,
            String fechaHasta) {
        Objeto response = new Objeto();

        try {
            List<RWBeneficioNovedad> resultados = new ArrayList<>();
            Objeto resultadosResponse = new Objeto();

            resultados = RWApiRewards.getBeneficiosHistoricoNovedades(context, idCobis, fechaDesde, fechaHasta);

            for (RWBeneficioNovedad obj : resultados) {
                resultadosResponse.add(obj.objeto());
            }

            response.set("status", 0);
            response.set("data", resultadosResponse);

            return response;

        } catch (ApiException e) {
            if (e.response.codigoHttp.equals(204)) {
                response.set("error", RespuestaRW.error(context, "SIN_DATOS"));
            } else {
                response.set("error", RespuestaRW.error(context,
                        "PosicionConsolidadaService.getHistoricoNovedades - ApiException", e, ""));
            }

            return response;
        } catch (Exception e) {
            response.set("error", RespuestaRW.error(context,
                    "PosicionConsolidadaService.getHistoricoNovedades - Exception", e, ""));

            return response;
        }
    }
}
