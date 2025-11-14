package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.backend.servicio.api.transmit.LibreriaFraudes.UsuarioLibreriaRequest;
import ar.com.hipotecario.backend.servicio.api.transmit.LibreriaFraudes.UsuarioLibreriaResponse;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.libreriariesgofraudes.application.dto.RecommendationDTO;
import ar.com.hipotecario.canal.libreriariesgofraudes.audit.model.AuditLogReport;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.BankProcess;
import spark.utils.StringUtils;

public class TransmitHB extends Transmit {
    public static final String REASON_TRANSFERENCIA = "transferencia";
    public static final String REASON_VENTA_DOLARES = "ventaDolares";
    public static final String REASON_DEBIN = "debin";
    public static final String REASON_RESCATE = "rescate";
    public static final String REASON_VENTA_BONOS = "bonos";
    public static final String REASON_VENTA_ACCIONES = "acciones";
    public static final String CANAL = "HB";

    public static boolean isChallengeOtp(ContextoHB contexto, String funcionalidad) {
        return contexto.sesion.isChallengeOtp(funcionalidad);
    }

    public static Respuesta recomendacionTransmit(ContextoHB contexto, BankProcess bankProcess, String funcionalidad) {
        try {
            AuditLogReport auditLogReport = new AuditLogReport(
                    contexto.canal == null ? "" : contexto.canal,
                    contexto.subCanal() == null ? "" : contexto.subCanal(),
                    contexto.idSesion() == null ? "" : contexto.idSesion(),
                    Util.idProceso(),
                    "",
                    ConfigHB.string("api_url_auditor", ""),
                    contexto.ip() == null ? "" : contexto.ip());

            RecommendationDTO recommendationDTO = recomendacion(contexto, bankProcess, auditLogReport).tryGet();
            loguearInfo("recommendationDTO", recommendationDTO.getRecommendationType());
            switch (recommendationDTO.getRecommendationType()) {
                case Transmit.DENY:
                    contexto.limpiarSegundoFactor();
                    // return Respuesta.estado(Transmit.getErrorDeny());
                    contexto.sesion.setChallengeOtp(true);
                    contexto.sesion.setFuncionalidadChallengeOtp(funcionalidad);
                    return Respuesta.estado(Transmit.getErrorChallenge());
                case Transmit.CHALLENGE:
                    contexto.limpiarSegundoFactor();
                    contexto.sesion.setChallengeOtp(true);
                    contexto.sesion.setFuncionalidadChallengeOtp(funcionalidad);
                    return Respuesta.estado(Transmit.getErrorChallenge());
                default:
                    return Respuesta.exito();
            }
        } catch (Exception e) {
            loguearError("", "error al tener la recomendacion LOGIN");
            return Respuesta.exito();
        }
    }

    public static boolean validarCsmTransaccion(ContextoHB contexto, JourneyTransmitEnum journeyTransmitEnum, String idCobis) {
        UsuarioLibreriaRequest usuarioLibreriaRequest = new UsuarioLibreriaRequest(
                "",
                "",
                StringUtils.isNotBlank(contexto.idCobis()) ? contexto.idCobis() : idCobis,
                "",
                "",
                contexto.canal(),
                contexto.subCanal(),
                contexto.persona().cuit(),
                contexto.ip(),
                journeyTransmitEnum,
                contexto.sesion().idSesion,
                StringUtils.isNotBlank(contexto.idCobis()) ? contexto.idCobis() : idCobis,
                contexto.parametros.string("csmId"),
                contexto.parametros.string("checksum"),
                false
        );
        return validarCsm(contexto, usuarioLibreriaRequest);
    }

}
