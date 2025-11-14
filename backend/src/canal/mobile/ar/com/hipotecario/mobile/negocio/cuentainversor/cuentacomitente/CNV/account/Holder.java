package ar.com.hipotecario.mobile.negocio.cuentainversor.cuentacomitente.CNV.account;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Holder {
    private String identificador; //identifier
    private String tipo; // type

    public Holder() {
    }

    public Holder(String identificador, String tipo) {
        this.identificador = identificador;
        this.tipo = tipo;
    }

    public String getIdentificador() {
        return identificador;
    }

    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
