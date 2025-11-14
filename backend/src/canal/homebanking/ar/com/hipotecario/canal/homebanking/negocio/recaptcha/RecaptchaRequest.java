package ar.com.hipotecario.canal.homebanking.negocio.recaptcha;

public class RecaptchaRequest {
    private String token;
    private String siteKey;
    private String userAgent;
    private String userIpAddress;
    private String ja4;
    private String ja3;
    private String expectedAction;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public void setSiteKey(String siteKey) {
        this.siteKey = siteKey;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getUserIpAddress() {
        return userIpAddress;
    }

    public void setUserIpAddress(String userIpAddress) {
        this.userIpAddress = userIpAddress;
    }

    public String getJa4() {
        return ja4;
    }

    public void setJa4(String ja4) {
        this.ja4 = ja4;
    }

    public String getJa3() {
        return ja3;
    }

    public void setJa3(String ja3) {
        this.ja3 = ja3;
    }

    public String getExpectedAction() {
        return expectedAction;
    }

    public void setExpectedAction(String expectedAction) {
        this.expectedAction = expectedAction;
    }

    public RecaptchaRequest() {
    }

    public RecaptchaRequest(String token, String siteKey, String userAgent, String userIpAddress, String expectedAction) {
        this.token = token;
        this.siteKey = siteKey;
        this.userAgent = userAgent;
        this.userIpAddress = userIpAddress;
        this.expectedAction = expectedAction;
    }
}
