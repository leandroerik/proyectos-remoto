package ar.com.hipotecario.canal.homebanking.negocio.recaptcha;

public class RecaptchaEventRequest {
    private RecaptchaRequest event;

    public RecaptchaRequest getEvent() {
        return event;
    }

    public void setEvent(RecaptchaRequest event) {
        this.event = event;
    }

    public RecaptchaEventRequest() {
    }

    public RecaptchaEventRequest(RecaptchaRequest event) {
        this.event = event;
    }
}
