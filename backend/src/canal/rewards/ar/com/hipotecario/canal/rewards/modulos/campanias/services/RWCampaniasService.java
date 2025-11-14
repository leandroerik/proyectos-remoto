package ar.com.hipotecario.canal.rewards.modulos.campanias.services;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.rewards.core.RespuestaRW;
import ar.com.hipotecario.canal.rewards.middleware.apis.RWApiRewards;
import ar.com.hipotecario.canal.rewards.modulos.campanias.model.*;
import java.util.List;

public class RWCampaniasService {

    static public Objeto consultarCampanias(ContextoRewards contexto, QueryConsultaCampanias query) {
        Objeto response = new Objeto();
        try {
            List<Campania> resultados = RWApiRewards.getConsultaCampanias(contexto, query);
            response.set("status", 0);
            response.set("data", resultados);
            return response;
        } catch (ApiException e) {
            response.set("error",
                    RespuestaRW.error(contexto, "RWCampaniasService.consultarCampanias - ApiException", e, ""));
            return response;
        } catch (Exception e) {
            response.set("error",
                    RespuestaRW.error(contexto, "RWCampaniasService.consultarCampanias - Exception", e, ""));
            return response;
        }

    }
    static public Objeto consultarCampaniaDetalle(ContextoRewards contexto, QueryConsultaCampaniaDetalle query) {
        Objeto response = new Objeto();
        try {
            List<CampaniaDetalle> resultados = RWApiRewards.getConsultaCampaniaDetalle(contexto, query);
            response.set("status", 0);
            response.set("data", resultados);
            return response;
        } catch (ApiException e) {
            response.set("error",
                    RespuestaRW.error(contexto, "RWCampaniasService.consultarCampaniaDetalle - ApiException", e, ""));
            return response;

        } catch (Exception e) {
            response.set("error",
                    RespuestaRW.error(contexto, "RWCampaniasService.consultarCampaniaDetalle - Exception", e, ""));
            return response;
        }
    }

    public static Object actualizarEstadoCampania(ContextoRewards contexto, QueryModificacionCampania query) {
        try {
            ActualizarCampania actualizarCampania = new ActualizarCampania();
            actualizarCampania.setCodigoRegistro(query.getSecuencial());
            actualizarCampania.setEstado(query.getEstado());
            actualizarCampania.setUsuario(query.getUsuario());

            ApiResponse resultados = RWApiRewards.patchActualizarEstadoCampania(contexto, actualizarCampania);

            Objeto response = new Objeto();
            response.set("status", resultados.codigoHttp == 200 ? 0 : 1);
            response.set("data",
                    resultados.codigoHttp == 200
                            ? "Ajuste enviado ok, debe aguardar autorizacion del supervisor"
                            : "Error al enviar ajuste");
            return response;
        } catch (ApiException e) {
            return RespuestaRW.error(contexto,
                    "RWCampaniasService.actualizarEstadoCampania - ApiException", e, "ERROR_SOLICITAR_AJUSTE");

        } catch (Exception e) {
            return RespuestaRW.error(contexto,
                    "RWCampaniasService.actualizarEstadoCampania - Exception", e, "ERROR_SOLICITAR_AJUSTE");
        }
    }
}