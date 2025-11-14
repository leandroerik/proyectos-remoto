package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.CNV.account;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Propietario {
    private String identificador; ////
    private String tipo; // tipo
    public Propietario() {
    }
    public Propietario(String identificador, String tipo) {
        this.identificador = identificador;
        this.tipo = tipo;}
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
