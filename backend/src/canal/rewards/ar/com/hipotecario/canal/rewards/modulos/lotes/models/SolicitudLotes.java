package ar.com.hipotecario.canal.rewards.modulos.lotes.models;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class SolicitudLotes extends ApiObjeto {
    private String codigoMovimiento;
    private String estado;
    private String fechaDesde;
    private String fechaHasta;
    private Boolean novedadAlta;
    private Boolean novedadBaja;
    private Boolean novedadDatosFiliatorios;
    private Boolean novedadMora;
    private String producto;
    private Integer secuencial;
    private Integer tipoFecha;

    public SolicitudLotes() {
    }

    public String getCodigoMovimiento() {
        return codigoMovimiento;
    }

    public void setCodigoMovimiento(String codigoMovimiento) {
        this.codigoMovimiento = codigoMovimiento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(String fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public String getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(String fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public Boolean getNovedadAlta() {
        return novedadAlta;
    }

    public void setNovedadAlta(Boolean novedadAlta) {
        this.novedadAlta = novedadAlta;
    }

    public Boolean getNovedadBaja() {
        return novedadBaja;
    }

    public void setNovedadBaja(Boolean novedadBaja) {
        this.novedadBaja = novedadBaja;
    }

    public Boolean getNovedadDatosFiliatorios() {
        return novedadDatosFiliatorios;
    }

    public void setNovedadDatosFiliatorios(Boolean novedadDatosFiliatorios) {
        this.novedadDatosFiliatorios = novedadDatosFiliatorios;
    }

    public Boolean getNovedadMora() {
        return novedadMora;
    }

    public void setNovedadMora(Boolean novedadMora) {
        this.novedadMora = novedadMora;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public Integer getSecuencial() {
        return secuencial;
    }

    public void setSecuencial(Integer secuencial) {
        this.secuencial = secuencial;
    }

    public Integer getTipoFecha() {
        return tipoFecha;
    }

    public void setTipoFecha(Integer tipoFecha) {
        this.tipoFecha = tipoFecha;
    }

    @Override
    public String toString() {

        if(this.getProducto() != "" && this.getCodigoMovimiento() == "P"){
            return "SolicitudConsulta [codigoMovimiento=" + codigoMovimiento
                    + ", fechaDesde=" + fechaDesde + ", fechaHasta=" + fechaHasta
                    + ", novedadAlta=" + novedadAlta + ", novedadBaja=" + novedadBaja
                    + ", novedadDatosFiliatorios=" + novedadDatosFiliatorios + ", novedadMora=" + novedadMora
                    + ", producto=" + producto + ", secuencial=" + secuencial + ", tipoFecha=" + tipoFecha + "]";
        }else{
            return "SolicitudConsulta [codigoMovimiento=" + codigoMovimiento
                    + ", fechaDesde=" + fechaDesde + ", fechaHasta=" + fechaHasta
                    + ", novedadAlta=" + novedadAlta + ", novedadBaja=" + novedadBaja
                    + ", novedadDatosFiliatorios=" + novedadDatosFiliatorios + ", novedadMora=" + novedadMora
                    + ", secuencial=" + secuencial + ", tipoFecha=" + tipoFecha + "]";
        }
    }
}
