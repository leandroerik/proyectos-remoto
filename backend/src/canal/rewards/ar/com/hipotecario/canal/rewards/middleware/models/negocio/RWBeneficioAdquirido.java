package ar.com.hipotecario.canal.rewards.middleware.models.negocio;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class RWBeneficioAdquirido extends ApiObjeto {
    private String programa;
    private String cuenta;
    private String fechaAlta;
    private String descTipoEstado;
    private String tipoTarjeta;
    private String numeroSocio;
    private String fechaBaja;

    public RWBeneficioAdquirido() {
    }

    public String getPrograma() {
        return programa;
    }

    public void setPrograma(String programa) {
        this.programa = programa;
    }

    public String getCuenta() {
        return cuenta;
    }

    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    public String getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(String fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public String getDescTipoEstado() {
        return descTipoEstado;
    }

    public void setDescTipoEstado(String descTipoEstado) {
        this.descTipoEstado = descTipoEstado;
    }

    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public String getNumeroSocio() {
        return numeroSocio;
    }

    public void setNumeroSocio(String numeroSocio) {
        this.numeroSocio = numeroSocio;
    }

    public String getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(String fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

}
