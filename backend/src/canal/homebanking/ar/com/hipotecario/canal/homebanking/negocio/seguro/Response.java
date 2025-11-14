package ar.com.hipotecario.canal.homebanking.negocio.seguro;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Response {
    @JsonProperty("result")
    private Result result;

    @JsonProperty("resultCode")
    private String resultCode;

    @JsonProperty("resultString")
    private String resultString;

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultString() {
        return resultString;
    }

    public void setResultString(String resultString) {
        this.resultString = resultString;
    }
}
