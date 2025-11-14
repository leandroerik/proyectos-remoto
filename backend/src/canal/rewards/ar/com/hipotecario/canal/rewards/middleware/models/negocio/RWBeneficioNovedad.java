package ar.com.hipotecario.canal.rewards.middleware.models.negocio;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class RWBeneficioNovedad extends ApiObjeto {
    private String cuenta;
    private String descMotivoRechazo;
    private String descTipoEstado;
    private String fechaAlta;
    private String fechaBaja;
    private String fechaNovedad;
    private String fechaRespuesta;
    private String numeroSocio;
    private String programa;
    private String tipoNovedad;
    private String tipoTarjeta;

    // Constructor vacío
    public RWBeneficioNovedad() {

    }

    // Constructor basado en un objeto
    public RWBeneficioNovedad(Objeto obj) {
        this.cuenta = obj.string("cuenta");
        this.descMotivoRechazo = obj.string("descMotivoRechazo");
        this.descTipoEstado = obj.string("descTipoEstado");
        this.fechaAlta = obj.string("fechaAlta");
        this.fechaBaja = obj.string("fechaBaja");
        this.fechaNovedad = obj.string("fechaNovedad");
        this.fechaRespuesta = obj.string("fechaRespuesta");
        this.numeroSocio = obj.string("numeroSocio");
        this.programa = obj.string("programa");
        this.tipoNovedad = obj.string("tipoNovedad");
        this.tipoTarjeta = obj.string("tipoTarjeta");
    }

    // Getters y Setters

    public String getCuenta() {
        return cuenta;
    }

    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    public String getDescMotivoRechazo() {
        return descMotivoRechazo;
    }

    public void setDescMotivoRechazo(String descMotivoRechazo) {
        this.descMotivoRechazo = descMotivoRechazo;
    }

    public String getDescTipoEstado() {
        return descTipoEstado;
    }

    public void setDescTipoEstado(String descTipoEstado) {
        this.descTipoEstado = descTipoEstado;
    }

    public String getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(String fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public String getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(String fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public String getFechaNovedad() {
        return fechaNovedad;
    }

    public void setFechaNovedad(String fechaNovedad) {
        this.fechaNovedad = fechaNovedad;
    }

    public String getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(String fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public String getNumeroSocio() {
        return numeroSocio;
    }

    public void setNumeroSocio(String numeroSocio) {
        this.numeroSocio = numeroSocio;
    }

    public String getPrograma() {
        return programa;
    }

    public void setPrograma(String programa) {
        this.programa = programa;
    }

    public String getTipoNovedad() {
        return tipoNovedad;
    }

    public void setTipoNovedad(String tipoNovedad) {
        this.tipoNovedad = tipoNovedad;
    }

    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }
}
