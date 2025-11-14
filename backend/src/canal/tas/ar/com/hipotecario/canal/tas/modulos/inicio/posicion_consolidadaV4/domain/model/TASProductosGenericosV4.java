package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

import java.math.BigDecimal;

public class TASProductosGenericosV4 extends ApiObjeto {

    public String codigoPaquete;
    public String codigoProducto;
    public String codigoTitularidad;
    public String descripcionPaquete;
    public String descTitularidad;
    public String estado;
    public String fechaAlta;
    public String muestraPaquete;
    public String numeroProducto;
    public String tipo;
    public String cuentaAsociada;
    public Integer codMoneda;
    public String descProducto;
    public Integer detProducto;
    public BigDecimal importe;
    public String pfFechaVencimiento;

    public TASProductosGenericosV4() {
    }

    public String getCodigoPaquete() {
        return codigoPaquete;
    }

    public void setCodigoPaquete(String codigoPaquete) {
        this.codigoPaquete = codigoPaquete;
    }

    public String getCodigoProducto() {
        return codigoProducto;
    }

    public void setCodigoProducto(String codigoProducto) {
        this.codigoProducto = codigoProducto;
    }

    public String getCodigoTitularidad() {
        return codigoTitularidad;
    }

    public void setCodigoTitularidad(String codigoTitularidad) {
        this.codigoTitularidad = codigoTitularidad;
    }

    public String getDescripcionPaquete() {
        return descripcionPaquete;
    }

    public void setDescripcionPaquete(String descripcionPaquete) {
        this.descripcionPaquete = descripcionPaquete;
    }

    public String getDescTitularidad() {
        return descTitularidad;
    }

    public void setDescTitularidad(String descTitularidad) {
        this.descTitularidad = descTitularidad;
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

    public String getMuestraPaquete() {
        return muestraPaquete;
    }

    public void setMuestraPaquete(String muestraPaquete) {
        this.muestraPaquete = muestraPaquete;
    }

    public String getNumeroProducto() {
        return numeroProducto;
    }

    public void setNumeroProducto(String numeroProducto) {
        this.numeroProducto = numeroProducto;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getCuentaAsociada() {
        return cuentaAsociada;
    }

    public void setCuentaAsociada(String cuentaAsociada) {
        this.cuentaAsociada = cuentaAsociada;
    }

    public Integer getCodMoneda() {
        return codMoneda;
    }

    public void setCodMoneda(Integer codMoneda) {
        this.codMoneda = codMoneda;
    }

    public String getDescProducto() {
        return descProducto;
    }

    public void setDescProducto(String descProducto) {
        this.descProducto = descProducto;
    }

    public Integer getDetProducto() {
        return detProducto;
    }

    public void setDetProducto(Integer detProducto) {
        this.detProducto = detProducto;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public String getPfFechaVencimiento() {
        return pfFechaVencimiento;
    }

    public void setPfFechaVencimiento(String pfFechaVencimiento) {
        this.pfFechaVencimiento = pfFechaVencimiento;
    }
}
