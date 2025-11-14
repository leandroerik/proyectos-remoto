package ar.com.hipotecario.canal.buhobank.transmit;

import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.buhobank.ContextoBB;
import ar.com.hipotecario.canal.libreriariesgofraudes.application.dto.RecommendationDTO;
import ar.com.hipotecario.canal.libreriariesgofraudes.audit.model.AuditLogReport;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.mb.OnboardingMBBMBankProcess;

public class TransmitBB extends Transmit {

    public static RecommendationDTO recomendacionOnboarding(ContextoBB contexto, OnboardingMBBMBankProcess onboardingMBBMBankProcess, AuditLogReport auditLogReport) {
        try {
            loguearInfo("recomendacionOnboarding", "onboardingMBBMBankProcess");
            RecommendationDTO r = recomendacion( onboardingMBBMBankProcess,auditLogReport).tryGet();
            if (r != null)
                loguearInfo("recomendacionOnboarding", r.getRecommendationType() + "- " + r.getRiskScore());
            return r;
        } catch (Exception e) {
            loguearError("recomendacionOnboarding", "error al tener la recomendacion LOGIN");
            throw new RuntimeException(e);
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
