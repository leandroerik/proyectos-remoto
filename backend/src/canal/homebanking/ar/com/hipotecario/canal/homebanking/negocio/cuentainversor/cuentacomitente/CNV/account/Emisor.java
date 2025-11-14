package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.CNV.account;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Emisor {
    private String paisEmision; //countryOfIssuance
    private String fechaVencimiento; //expiryDate
    private String identificador; //identifier
    private String entidadEmisora; //issuingEntity
    private String fechaRegistro; //registrationDate
    private String tipo; //type

    public Emisor() {
    }

    public Emisor(String paisEmision, String fechaVencimiento, String identificador, String entidadEmisora, String fechaRegistro, String tipo) {
        this.paisEmision = paisEmision;
        this.fechaVencimiento = fechaVencimiento;
        this.identificador = identificador;
        this.entidadEmisora = entidadEmisora;
        this.fechaRegistro = fechaRegistro;
        this.tipo = tipo;
    }
    public String getPaisEmision() {
        return paisEmision;
    }
    public void setPaisEmision(String paisEmision) {
        this.paisEmision = paisEmision;
    }
    public String getFechaVencimiento() {
        return fechaVencimiento;
    }
    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }
    public String getIdentificador() {
        return identificador;
    }
    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }
    public String getEntidadEmisora() {
        return entidadEmisora;
    }
    public void setEntidadEmisora(String entidadEmisora) {
        this.entidadEmisora = entidadEmisora;
    }
    public String getFechaRegistro() {
        return fechaRegistro;
    }
    public void setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
