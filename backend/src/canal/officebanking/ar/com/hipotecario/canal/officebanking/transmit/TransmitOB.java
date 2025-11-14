package ar.com.hipotecario.canal.officebanking.transmit;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Servidor;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.libreriariesgofraudes.application.dto.RecommendationDTO;
import ar.com.hipotecario.canal.libreriariesgofraudes.audit.model.AuditLogReport;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.config.TransmitGatewayAdapters;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.BankProcess;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.challenge.ChallengeResult;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.challenge.ChallengeResultType;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.challenge.ChallengeType;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;

public class TransmitOB extends Transmit {
    public static  AuditLogReport auditLogReport=null;

    public static  void setAuditLogReport(ContextoOB contexto) {
        auditLogReport =  new AuditLogReport(
        contexto.canal == null ? "" : contexto.canal,
                contexto.subCanal() == null ? "" : contexto.subCanal(),
                contexto.idSesion() == null ? "" : contexto.idSesion(),
                contexto.idSesion() == null ? "" : contexto.idSesion(),
                contexto.idSesion() == null ? "" : contexto.idSesion(),
                contexto.config.string("backend_api_auditor"),
                contexto.sesion().ip == null ? "": contexto.sesion().ip);
    }


    private static void comprobarConexion(){
            Transmit.iniciarTransmit(new Contexto(), "OB");
            Servidor.recommendationService = TransmitGatewayAdapters.getRecommendationService();
        if(Servidor.recommendationService == null) {
            System.out.println("conexion a transmit invalida OB");
        }
    }

    public static RecommendationDTO obtenerRecomendacion(ContextoOB contexto, BankProcess bankProcess) {
        try {
            LogOB.evento(contexto,"obtenerRecomendacion","pedir recomendacion");
            if(Servidor.recommendationService == null) {
                comprobarConexion();
            }
            RecommendationDTO r = recomendacion(bankProcess,auditLogReport).tryGet();

            if (r != null){
                LogOB.evento(contexto,"obtenerRecomendacion",r.getRecommendationType() + "- " + r.getRiskScore());
            }else{
                r =  new RecommendationDTO();
                r.setRecommendationType("ALLOW");
                LogOB.evento(contexto,"obtenerRecomendacion","http 500 error : ALLOW");
            }
                return r;
        } catch (Exception e) {
            LogOB.evento(contexto,"obtenerRecomendacion","error al tener la recomendacion LOGIN");
            RecommendationDTO r = null;
            r =  new RecommendationDTO();
            r.setRecommendationType("ALLOW");
            return r;
        }
    }
    public static void confirmarChallenge(ContextoOB contexto, RecommendationDTO recomendacion) {
        try {
            LogOB.evento(contexto,"confirmarChallenge","entra a confirmar");
            if(Servidor.recommendationService == null) {
                comprobarConexion();
            }
            ChallengeResult challengeResult = ChallengeResult.builder()
                    .action_token(recomendacion.getActionToken())
                    .result(ChallengeResultType.success)
                    .user_id(contexto.sesion().empresaOB.idCobis)
                    .private_user_identifier(null)
                    .challenge_type(ChallengeType.sms_otp)
                    .build();
            String response =  Servidor.recommendationService.reportActionResult(challengeResult);
            LogOB.evento(contexto,"confirmarChallenge",response);
        } catch (Exception e) {

        }
    }
    public static Boolean isErrorRecommendationOnboarding(RecommendationDTO recommendationDTO) {
        try {
            Boolean r = isErrorRecommendation(recommendationDTO).tryGet();
            return r;
        } catch (Exception e) {
            loguearError("isErrorRecommendation", "error al tener la isErrorRecommendation LOGIN");
            throw new RuntimeException(e);
        }
    }

}
