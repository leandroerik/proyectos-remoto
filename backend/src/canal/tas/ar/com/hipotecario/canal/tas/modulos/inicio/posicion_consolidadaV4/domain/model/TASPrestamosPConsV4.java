package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

import java.math.BigDecimal;

public class TASPrestamosPConsV4 extends ApiObjeto {

    public Boolean adicionales;
    public Integer cantCuotasMora;
    public String categoria;
    public String codigoProducto;
    public Integer cuotaActual;
    public String descEstado;
    public String descMoneda;
    public String descSucursal;
    public String descTipoProducto;
    public String descTipoTitularidad;
    public String esPrecodeu;
    public String esProCrear;
    public String estado;
    public String fechaAlta;
    public String fechaBaja;
    public String fechaProxVencimiento;
    public String formaPago;
    public String hipotecarioNSP;
    public String idDomicilio;
    public Integer idPaquete;
    public String idProducto;
    public String moneda;
    public BigDecimal montoAprobado;
    public BigDecimal montoCuotaActual;
    public Boolean muestraPaquete;
    public String numeroProducto;
    public Integer plazoOriginal;
    public Integer plazoRestante;
    public Integer sucursal;
    public String tipoOperacion;
    public String tipoProducto;
    public String tipoTitularidad;

    public boolean errorApi;
    public Exception errorDesc;

    public TASPrestamosPConsV4() {
    }
    public Boolean isAdicionales() {
        return adicionales;
    }

    public void setAdicionales(Boolean adicionales) {
        this.adicionales = adicionales;
    }

    public Integer getCantCuotasMora() {
        return cantCuotasMora;
    }

    public void setCantCuotasMora(Integer cantCuotasMora) {
        this.cantCuotasMora = cantCuotasMora;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public Integer getCuotaActual() {
        return cuotaActual;
    }

    public void setCuotaActual(Integer cuotaActual) {
        this.cuotaActual = cuotaActual;
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

    public String getDescTipoProducto() {
        return descTipoProducto;
    }

    public void setDescTipoProducto(String descTipoProducto) {
        this.descTipoProducto = descTipoProducto;
    }

    public String getDescTipoTitularidad() {
        return descTipoTitularidad;
    }

    public void setDescTipoTitularidad(String descTipoTitularidad) {
        this.descTipoTitularidad = descTipoTitularidad;
    }

    public String getEsPrecodeu() {
        return esPrecodeu;
    }

    public void setEsPrecodeu(String esPrecodeu) {
        this.esPrecodeu = esPrecodeu;
    }

    public String getEsProCrear() {
        return esProCrear;
    }

    public void setEsProCrear(String esProCrear) {
        this.esProCrear = esProCrear;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    public String getFechaProxVencimiento() {
        return fechaProxVencimiento;
    }

    public void setFechaProxVencimiento(String fechaProxVencimiento) {
        this.fechaProxVencimiento = fechaProxVencimiento;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public String getHipotecarioNSP() {
        return hipotecarioNSP;
    }

    public void setHipotecarioNSP(String hipotecarioNSP) {
        this.hipotecarioNSP = hipotecarioNSP;
    }

    public String getIdDomicilio() {
        return idDomicilio;
    }

    public void setIdDomicilio(String idDomicilio) {
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

    public BigDecimal getMontoAprobado() {
        return montoAprobado;
    }

    public void setMontoAprobado(BigDecimal montoAprobado) {
        this.montoAprobado = montoAprobado;
    }

    public BigDecimal getMontoCuotaActual() {
        return montoCuotaActual;
    }

    public void setMontoCuotaActual(BigDecimal montoCuotaActual) {
        this.montoCuotaActual = montoCuotaActual;
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

    public Integer getPlazoOriginal() {
        return plazoOriginal;
    }

    public void setPlazoOriginal(Integer plazoOriginal) {
        this.plazoOriginal = plazoOriginal;
    }

    public Integer getPlazoRestante() {
        return plazoRestante;
    }

    public void setPlazoRestante(Integer plazoRestante) {
        this.plazoRestante = plazoRestante;
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
