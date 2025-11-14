package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model;



import ar.com.hipotecario.backend.conector.api.ApiObjeto;

import java.math.BigDecimal;

public class TASTarjetasDebitoPConsV4 extends ApiObjeto {

    public Boolean activacionTemprana;
    public Boolean adicionales;
    public String descEstado;
    public String descMoneda;
    public String descSucursal;
    public String descTipoTitularidad;
    public String estado;
    public String estadoTD;
    public String fechaAlta;
    public String fechaBaja;
    public String fechaVencimiento;
    public Integer idDomicilio;
    public Integer idPaquete;
    public String idProducto;
    public BigDecimal limiteExtraccionMonto;
    public String moneda;
    public Boolean muestraPaquete;
    public String nroSolicitud;
    public String numeroProducto;
    public String numeroTD;
    public Integer sucursal;
    public String tipoOperacion;
    public String tipoProducto;
    public String tipoTitularidad;
    public Boolean virtual;

    public boolean errorApi;
    public Exception errorDesc;

    public TASTarjetasDebitoPConsV4() {
    }

    public Boolean isActivacionTemprana() {
        return activacionTemprana;
    }

    public void setActivacionTemprana(Boolean activacionTemprana) {
        this.activacionTemprana = activacionTemprana;
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

    public String getEstadoTD() {
        return estadoTD;
    }

    public void setEstadoTD(String estadoTD) {
        this.estadoTD = estadoTD;
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

    public BigDecimal getLimiteExtraccionMonto() {
        return limiteExtraccionMonto;
    }

    public void setLimiteExtraccionMonto(BigDecimal limiteExtraccionMonto) {
        this.limiteExtraccionMonto = limiteExtraccionMonto;
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

    public String getNroSolicitud() {
        return nroSolicitud;
    }

    public void setNroSolicitud(String nroSolicitud) {
        this.nroSolicitud = nroSolicitud;
    }

    public String getNumeroProducto() {
        return numeroProducto;
    }

    public void setNumeroProducto(String numeroProducto) {
        this.numeroProducto = numeroProducto;
    }

    public String getNumeroTD() {
        return numeroTD;
    }

    public void setNumeroTD(String numeroTD) {
        this.numeroTD = numeroTD;
    }

    public Integer getSucursal() {
        return sucursal;
    }

    public void setSucursal(Integer sucursal) {
        this.sucursal = sucursal;
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

    public Boolean isVirtual() {
        return virtual;
    }

    public void setVirtual(Boolean virtual) {
        this.virtual = virtual;
    }

    public Boolean getActivacionTemprana() {
        return activacionTemprana;
    }

    public Boolean getAdicionales() {
        return adicionales;
    }

    public Boolean getMuestraPaquete() {
        return muestraPaquete;
    }

    public Boolean getVirtual() {
        return virtual;
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
