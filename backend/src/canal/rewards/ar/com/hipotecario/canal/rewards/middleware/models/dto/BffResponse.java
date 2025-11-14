package ar.com.hipotecario.canal.rewards.middleware.models.dto;

import ar.com.hipotecario.backend.base.Objeto;

public class BffResponse<T> {
    private int status;
    private String message;
    private T data;
    private Object error; // Para manejar errores opcionales, si es necesario

    // Getters y Setters

    public BffResponse() {
        this.status = 0;
        this.message = "";
        this.data = null;
        this.error = null;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }
}
