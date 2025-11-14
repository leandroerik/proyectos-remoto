package ar.com.hipotecario.canal.homebanking.negocio.recaptcha;

import ar.com.hipotecario.backend.base.Objeto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RecaptchaErrorResponse {
    private int code;
    private String message;
    private String status;
    private List<Objeto> details;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Objeto> getDetails() {
        return details;
    }

    public void setDetails(List<Objeto> details) {
        this.details = details;
    }

    public RecaptchaErrorResponse() {
    }

    public RecaptchaErrorResponse(int code, String message, String status, List<Objeto> details) {
        this.code = code;
        this.message = message;
        this.status = status;
        this.details = details;
    }
}
