package ar.com.hipotecario.canal.rewards.modulos.lotes.services;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.rewards.core.RespuestaRW;
import ar.com.hipotecario.canal.rewards.middleware.apis.RWApiRewards;
import ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.model.SolicitudAjuste;
import ar.com.hipotecario.canal.rewards.modulos.lotes.models.SolicitudLotes;

import java.util.Date;

public class RWLotesService {
    static public Objeto postLotesTotales(ContextoRewards contexto,
                                          String codigoMovimiento,
                                          String fechaDesde,
                                          String fechaHasta,
                                          String novedadAlta,
                                          String novedadBaja,
                                          String novedadDatosFiliatorios,
                                          String novedadMora,
                                          String producto,
                                          String secuencial,
                                          String tipoFecha) {

        Objeto response = new Objeto();

        try {

            SolicitudLotes solicitud = new SolicitudLotes();

            solicitud.setCodigoMovimiento(codigoMovimiento);
            solicitud.setFechaDesde(fechaDesde);
            solicitud.setFechaHasta(fechaHasta);
            solicitud.setNovedadAlta(Boolean.parseBoolean(novedadAlta));
            solicitud.setNovedadBaja(Boolean.parseBoolean(novedadBaja));
            solicitud.setNovedadDatosFiliatorios(Boolean.parseBoolean(novedadDatosFiliatorios));
            solicitud.setNovedadMora(Boolean.parseBoolean(novedadMora));
            solicitud.setSecuencial(Integer.parseInt(secuencial));
            solicitud.setTipoFecha(Integer.parseInt(tipoFecha));
            if(producto != null && producto != ""){
                solicitud.setProducto(producto);
            }

            ApiResponse resultados = RWApiRewards.postLotesTotales(contexto, solicitud);

            response.set("status", resultados.codigoHttp == 200 ? 0 : 1);
            response.set("data",resultados);
            return response;
        } catch (ApiException e) {
            return RespuestaRW.error(contexto,
                    "RWAjustePuntosService.postSolicitarAjuste - ApiException", e, "ERROR_SOLICITAR_AJUSTE");

        } catch (Exception e) {
            return RespuestaRW.error(contexto,
                    "RWAjustePuntosService.postSolicitarAjuste - Exception", e, "ERROR_SOLICITAR_AJUSTE");
        }

    }
}
