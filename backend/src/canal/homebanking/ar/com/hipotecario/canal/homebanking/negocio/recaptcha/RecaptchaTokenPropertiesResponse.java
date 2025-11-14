package ar.com.hipotecario.canal.homebanking.negocio.recaptcha;

public class RecaptchaTokenPropertiesResponse {
    private boolean valid;
    private String hostname;
    private String action;
    private String createTime;
    private String invalidReason;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }

    public RecaptchaTokenPropertiesResponse() {
    }

    public RecaptchaTokenPropertiesResponse(boolean valid, String hostname, String action, String createTime, String invalidReason) {
        this.valid = valid;
        this.hostname = hostname;
        this.action = action;
        this.createTime = createTime;
        this.invalidReason = invalidReason;
    }
}
