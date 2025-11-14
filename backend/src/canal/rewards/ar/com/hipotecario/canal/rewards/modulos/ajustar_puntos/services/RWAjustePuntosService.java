package ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.services;

import java.util.Date;
import java.util.List;

import org.bouncycastle.util.Exceptions;

import com.azure.security.keyvault.secrets.implementation.models.Error;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.rewards.core.RespuestaRW;
import ar.com.hipotecario.canal.rewards.middleware.apis.RWApiRewards;
import ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.model.Ajuste;
import ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.model.AprovacionAjuste;
import ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.model.QueryConsultaAjustes;
import ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.model.ResponseAprobarAjuste;
import ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.model.SolicitudAjuste;

public class RWAjustePuntosService {
    static public Objeto postSolicitarAjuste(ContextoRewards contexto, String cliente, Integer puntos,
            String observacion) {
        try {

            String hoy = new Fecha(new Date()).string(
                    "yyyy-MM-dd");

            SolicitudAjuste solicitud = new SolicitudAjuste();
            solicitud.setCliente(Integer.parseInt(cliente));
            solicitud.setIdcliente(cliente);
            solicitud.setPuntos(Math.abs(puntos));
            solicitud.setObservacion(observacion);
            solicitud.setSigno(puntos >= 0 ? "+" : "-");
            solicitud.setFecha(hoy);
            solicitud.setOperacion("I");
            solicitud.setUsuarioIngreso(contexto.sesion().usr);

            Objeto response = new Objeto();

            ApiResponse resultados = RWApiRewards.postAjustePuntosSolicitud(contexto, solicitud);

            response.set("status", resultados.codigoHttp == 200 ? 0 : 1);
            response.set("data",
                    resultados.codigoHttp == 200
                            ? "Ajuste enviado ok, debe aguardar autorizacion del supervisor"
                            : "Error al enviar ajuste");
            return response;
        } catch (ApiException e) {
            return RespuestaRW.error(contexto,
                    "RWAjustePuntosService.postSolicitarAjuste - ApiException", e, "ERROR_SOLICITAR_AJUSTE");

        } catch (Exception e) {
            return RespuestaRW.error(contexto,
                    "RWAjustePuntosService.postSolicitarAjuste - Exception", e, "ERROR_SOLICITAR_AJUSTE");
        }

    }

    static public Objeto postAprovarAjuste(ContextoRewards contexto, AprovacionAjuste aprobacion) {
        Objeto response = new Objeto();

        try {
            ApiResponse resultados = RWApiRewards.postAjustePuntosAprobar(contexto, aprobacion);
            ResponseAprobarAjuste respuestaAjuste = ResponseAprobarAjuste.fromJson(resultados.body);

            // if (respuestaAjuste.getCodigoerror() != "0" ||
            // respuestaAjuste.getDescripcionerror() != "") {
            // throw new Exception("ERRRO:" + respuestaAjuste.getCodigoerror() + "mensaje:"
            // + respuestaAjuste.getDescripcionerror());
            // }

            response.set("status",
                    resultados.codigoHttp == 200 || !respuestaAjuste.getCodigoerror().equals("0") ? 0 : 1);
            response.set("data", respuestaAjuste);
            return response;
        } catch (ApiException e) {
            response.set("error", RespuestaRW.error(contexto,
                    "RWAjustePuntosService.postAprovarAjuste - ApiException", e, ""));
            return response;

        } catch (Exception e) {
            response.set("error", RespuestaRW.error(contexto,
                    "RWAjustePuntosService.postAprovarAjuste - Exception", e, ""));
            return response;
        }

    }

    static public Objeto consultarAjustes(ContextoRewards contexto, QueryConsultaAjustes query) {
        Objeto response = new Objeto();

        try {
            List<Ajuste> resultados = RWApiRewards.getConsultaAjustes(contexto, query);

            response.set("status", 0);
            response.set("data", resultados);
            return response;
        } catch (ApiException e) {
            response.set("error",
                    RespuestaRW.error(contexto, "RWAjustePuntosService.consultarAjustes - ApiException", e, ""));
            return response;

        } catch (Exception e) {
            response.set("error",
                    RespuestaRW.error(contexto, "RWAjustePuntosService.consultarAjustes - Exception", e, ""));
            return response;
        }

    }
}