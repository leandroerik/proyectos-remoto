package ar.com.hipotecario.canal.homebanking.negocio.recaptcha;

import java.math.BigDecimal;
import java.util.List;

public class RecaptchaRiskAnaysisResponse {
    private float score;
    private List<String> reasons;

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public RecaptchaRiskAnaysisResponse() {
    }

    public RecaptchaRiskAnaysisResponse(float score, List<String> reasons) {
        this.score = score;
        this.reasons = reasons;
    }
}
