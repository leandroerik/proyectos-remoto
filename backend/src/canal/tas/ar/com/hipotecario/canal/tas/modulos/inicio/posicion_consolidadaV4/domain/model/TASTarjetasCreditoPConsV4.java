package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

import java.math.BigDecimal;

public class TASTarjetasCreditoPConsV4 extends ApiObjeto {

    public String altaPuntoVenta;
    public String cierreActual;
    public String cuenta;
    public BigDecimal debitosEnCursoDolares;
    public BigDecimal debitosEnCursoPesos;
    public String denominacionTarjeta;
    public String descEstado;
    public String descModeloLiquidacion;
    public String descSucursal;
    public String descTipoTarjeta;
    public String descTipoTitularidad;
    public String estado;
    public String fechaAlta;
    public String fechaBaja;
    public String fechaProximoCierre;
    public String fechaVencActual;
    public String formaPago;
    public Integer idPaquete;
    public String modeloLiquidacion;
    public Boolean muestraPaquete;
    public String numero;
    public Integer sucursal;
    public String tipoTarjeta;
    public String tipoTitularidad;

    public TASTarjetasCreditoPConsV4() {
    }

    public String getAltaPuntoVenta() {
        return altaPuntoVenta;
    }

    public void setAltaPuntoVenta(String altaPuntoVenta) {
        this.altaPuntoVenta = altaPuntoVenta;
    }

    public String getCierreActual() {
        return cierreActual;
    }

    public void setCierreActual(String cierreActual) {
        this.cierreActual = cierreActual;
    }

    public String getCuenta() {
        return cuenta;
    }

    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    public BigDecimal getDebitosEnCursoDolares() {
        return debitosEnCursoDolares;
    }

    public void setDebitosEnCursoDolares(BigDecimal debitosEnCursoDolares) {
        this.debitosEnCursoDolares = debitosEnCursoDolares;
    }

    public BigDecimal getDebitosEnCursoPesos() {
        return debitosEnCursoPesos;
    }

    public void setDebitosEnCursoPesos(BigDecimal debitosEnCursoPesos) {
        this.debitosEnCursoPesos = debitosEnCursoPesos;
    }

    public String getDenominacionTarjeta() {
        return denominacionTarjeta;
    }

    public void setDenominacionTarjeta(String denominacionTarjeta) {
        this.denominacionTarjeta = denominacionTarjeta;
    }

    public String getDescEstado() {
        return descEstado;
    }

    public void setDescEstado(String descEstado) {
        this.descEstado = descEstado;
    }

    public String getDescModeloLiquidacion() {
        return descModeloLiquidacion;
    }

    public void setDescModeloLiquidacion(String descModeloLiquidacion) {
        this.descModeloLiquidacion = descModeloLiquidacion;
    }

    public String getDescSucursal() {
        return descSucursal;
    }

    public void setDescSucursal(String descSucursal) {
        this.descSucursal = descSucursal;
    }

    public String getDescTipoTarjeta() {
        return descTipoTarjeta;
    }

    public void setDescTipoTarjeta(String descTipoTarjeta) {
        this.descTipoTarjeta = descTipoTarjeta;
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

    public String getFechaProximoCierre() {
        return fechaProximoCierre;
    }

    public void setFechaProximoCierre(String fechaProximoCierre) {
        this.fechaProximoCierre = fechaProximoCierre;
    }

    public String getFechaVencActual() {
        return fechaVencActual;
    }

    public void setFechaVencActual(String fechaVencActual) {
        this.fechaVencActual = fechaVencActual;
    }

    public String getFormaPago() {
        return formaPago;
    }

    public void setFormaPago(String formaPago) {
        this.formaPago = formaPago;
    }

    public Integer getIdPaquete() {
        return idPaquete;
    }

    public void setIdPaquete(Integer idPaquete) {
        this.idPaquete = idPaquete;
    }

    public String getModeloLiquidacion() {
        return modeloLiquidacion;
    }

    public void setModeloLiquidacion(String modeloLiquidacion) {
        this.modeloLiquidacion = modeloLiquidacion;
    }

    public Boolean isMuestraPaquete() {
        return muestraPaquete;
    }

    public void setMuestraPaquete(Boolean muestraPaquete) {
        this.muestraPaquete = muestraPaquete;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Integer getSucursal() {
        return sucursal;
    }

    public void setSucursal(Integer sucursal) {
        this.sucursal = sucursal;
    }

    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public String getTipoTitularidad() {
        return tipoTitularidad;
    }

    public void setTipoTitularidad(String tipoTitularidad) {
        this.tipoTitularidad = tipoTitularidad;
    }
}
