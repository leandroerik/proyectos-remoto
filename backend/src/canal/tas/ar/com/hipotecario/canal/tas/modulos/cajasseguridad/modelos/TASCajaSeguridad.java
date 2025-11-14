package ar.com.hipotecario.canal.tas.modulos.cajasseguridad.modelos;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class TASCajaSeguridad extends ApiObjeto {
     private String idCaja;

    private String cuenta;
    private String estado;
    private String descEstado;
    private String descripcionProducto;
    private String tipoCuenta;
    private String fechaVencimiento;
    private String numeroProducto;
    private boolean renueva;
    private String valorContrato;
    private String periodicidadCobro;

    private Objeto error;

    public TASCajaSeguridad() {
    }

    public String getIdCaja() {
        return idCaja;
    }

    public void setIdCaja(String idCaja) {
        this.idCaja = idCaja;
    }

    public String getCuenta() {
        return cuenta;
    }

    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDescEstado() {
        return descEstado;
    }

    public void setDescEstado(String descEstado) {
        this.descEstado = descEstado;
    }

    public String getDescripcionProducto() {
        return descripcionProducto;
    }

    public void setDescripcionProducto(String descripcionProducto) {
        this.descripcionProducto = descripcionProducto;
    }

    public String getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(String tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getNumeroProducto() {
        return numeroProducto;
    }

    public void setNumeroProducto(String numeroProducto) {
        this.numeroProducto = numeroProducto;
    }

    public boolean isRenueva() {
        return renueva;
    }

    public void setRenueva(boolean renueva) {
        this.renueva = renueva;
    }

    public String getValorContrato() {
        return valorContrato;
    }

    public void setValorContrato(String valorContrato) {
        this.valorContrato = valorContrato;
    }

    public String getPeriodicidadCobro() {
        return periodicidadCobro;
    }

    public void setPeriodicidadCobro(String periodicidadCobro) {
        this.periodicidadCobro = periodicidadCobro;
    }

    public Objeto getError() {
        return error;
    }

    public void setError(Objeto error) {
        this.error = error;
    }
}
