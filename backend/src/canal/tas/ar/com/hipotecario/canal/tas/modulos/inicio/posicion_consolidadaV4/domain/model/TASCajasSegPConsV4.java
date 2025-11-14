package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class TASCajasSegPConsV4 extends ApiObjeto {

    public Boolean adicionales;
    public String descEstado;
    public String descMoneda;
    public String descSucursal;
    public String descTipoTitularidad;
    public String estado;
    public String estadoCajaSeguridad;
    public String fechaAlta;
    public String fechaBaja;
    public String fechaVencimiento;
    public Integer idDomicilio;
    public Integer idPaquete;
    public String idProducto;
    public String moneda;
    public Boolean muestraPaquete	;
    public String numeroProducto;
    public Integer sucursal;
    public String tamanio;
    public String tipoOperacion;
    public String tipoProducto;
    public String tipoTitularidad;

    public boolean errorApi;
    public Exception errorDesc;

    public TASCajasSegPConsV4() {
    }

    public Boolean isAdicionales() {
        return adicionales;
    }

    public void setAdicionales(Boolean adicionales) {
        this.adicionales = adicionales;
    }

    public String getDescEstado() {
        return descEstado;
    }

    public void setDescEstado(String descEstado) {
        this.descEstado = descEstado;
    }

    public String getDescMoneda() {
        return descMoneda;
    }

    public void setDescMoneda(String descMoneda) {
        this.descMoneda = descMoneda;
    }

    public String getDescSucursal() {
        return descSucursal;
    }

    public void setDescSucursal(String descSucursal) {
        this.descSucursal = descSucursal;
    }

    public String getDescTipoTitularidad() {
        return descTipoTitularidad;
    }

    public void setDescTipoTitularidad(String descTipoTitularidad) {
        this.descTipoTitularidad = descTipoTitularidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEstadoCajaSeguridad() {
        return estadoCajaSeguridad;
    }

    public void setEstadoCajaSeguridad(String estadoCajaSeguridad) {
        this.estadoCajaSeguridad = estadoCajaSeguridad;
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

    public String getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public Integer getIdDomicilio() {
        return idDomicilio;
    }

    public void setIdDomicilio(Integer idDomicilio) {
        this.idDomicilio = idDomicilio;
    }

    public Integer getIdPaquete() {
        return idPaquete;
    }

    public void setIdPaquete(Integer idPaquete) {
        this.idPaquete = idPaquete;
    }

    public String getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(String idProducto) {
        this.idProducto = idProducto;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public Boolean isMuestraPaquete() {
        return muestraPaquete;
    }

    public void setMuestraPaquete(Boolean muestraPaquete) {
        this.muestraPaquete = muestraPaquete;
    }

    public String getNumeroProducto() {
        return numeroProducto;
    }

    public void setNumeroProducto(String numeroProducto) {
        this.numeroProducto = numeroProducto;
    }

    public Integer getSucursal() {
        return sucursal;
    }

    public void setSucursal(Integer sucursal) {
        this.sucursal = sucursal;
    }

    public String getTamanio() {
        return tamanio;
    }

    public void setTamanio(String tamanio) {
        this.tamanio = tamanio;
    }

    public String getTipoOperacion() {
        return tipoOperacion;
    }

    public void setTipoOperacion(String tipoOperacion) {
        this.tipoOperacion = tipoOperacion;
    }

    public String getTipoProducto() {
        return tipoProducto;
    }

    public void setTipoProducto(String tipoProducto) {
        this.tipoProducto = tipoProducto;
    }

    public String getTipoTitularidad() {
        return tipoTitularidad;
    }

    public void setTipoTitularidad(String tipoTitularidad) {
        this.tipoTitularidad = tipoTitularidad;
    }

    public Boolean getAdicionales() {
        return adicionales;
    }

    public Boolean getMuestraPaquete() {
        return muestraPaquete;
    }

    public boolean isErrorApi() {
        return errorApi;
    }

    public void setErrorApi(boolean errorApi) {
        this.errorApi = errorApi;
    }

    public Exception getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(Exception errorDesc) {
        this.errorDesc = errorDesc;
    }
}
