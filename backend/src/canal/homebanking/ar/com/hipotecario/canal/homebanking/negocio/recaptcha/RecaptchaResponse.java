package ar.com.hipotecario.canal.homebanking.negocio.recaptcha;

public class RecaptchaResponse {
    private RecaptchaTokenPropertiesResponse tokenProperties;
    private RecaptchaRiskAnaysisResponse riskAnalysis;
    private RecaptchaRequest event;
    private String name;

    public RecaptchaTokenPropertiesResponse getTokenProperties() {
        return tokenProperties;
    }

    public void setTokenProperties(RecaptchaTokenPropertiesResponse tokenProperties) {
        this.tokenProperties = tokenProperties;
    }

    public RecaptchaRiskAnaysisResponse getRiskAnalysis() {
        return riskAnalysis;
    }

    public void setRiskAnalysis(RecaptchaRiskAnaysisResponse riskAnalysis) {
        this.riskAnalysis = riskAnalysis;
    }

    public RecaptchaRequest getEvent() {
        return event;
    }

    public void setEvent(RecaptchaRequest event) {
        this.event = event;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RecaptchaResponse() {
    }

    public RecaptchaResponse(RecaptchaTokenPropertiesResponse tokenProperties, RecaptchaRiskAnaysisResponse riskAnalysis, RecaptchaRequest event, String name) {
        this.tokenProperties = tokenProperties;
        this.riskAnalysis = riskAnalysis;
        this.event = event;
        this.name = name;
    }
}

