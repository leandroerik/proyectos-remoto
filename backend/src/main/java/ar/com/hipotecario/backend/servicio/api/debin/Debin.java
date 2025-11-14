package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import java.math.BigDecimal;

public class Debin extends ApiObjeto {
    public String cantidad;
    public Comprador comprador;
    public String concepto;
    public Creacion creacion;
    public String detalle;
    public Estado estado;
    public String fechaAlta;
    public String fechaExpiracion;
    public String id;
    public BigDecimal importe;
    public Moneda moneda;
    public BigDecimal montoPeriodo;
    public BigDecimal montoXDebin;
    public String periodo;
    public String prestacion;
    public String referencia;
    public String tipo;
    public Vendedor vendedor;

    public Debin(Creacion creacion) {
        this.creacion = creacion;
    }

	public Debin(String id, BigDecimal importe, Estado estado, Comprador comprador) {
		this.id = id;
		this.importe = importe;
		this.estado = estado;
		this.comprador = comprador;
	}

    public Debin() {
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }

    public Comprador getComprador() {
        return comprador;
    }

    public void setComprador(Comprador comprador) {
        this.comprador = comprador;
    }

    public String getConcepto() {
        return concepto;
    }

    public void setConcepto(String concepto) {
        this.concepto = concepto;
    }

    public Creacion getCreacion() {
        return creacion;
    }

    public void setCreacion(Creacion creacion) {
        this.creacion = creacion;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public Estado getEstado() {
        return estado;
    }
    
    public String getEstadoCodigo() {
        return estado.codigo;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public String getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(String fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public String getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(String fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public BigDecimal getMontoPeriodo() {
        return montoPeriodo;
    }

    public void setMontoPeriodo(BigDecimal montoPeriodo) {
        this.montoPeriodo = montoPeriodo;
    }

    public BigDecimal getMontoXDebin() {
        return montoXDebin;
    }

    public void setMontoXDebin(BigDecimal montoXDebin) {
        this.montoXDebin = montoXDebin;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public String getPrestacion() {
        return prestacion;
    }

    public void setPrestacion(String prestacion) {
        this.prestacion = prestacion;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Vendedor getVendedor() {
        return vendedor;
    }

    public void setVendedor(Vendedor vendedor) {
        this.vendedor = vendedor;
    }
}
