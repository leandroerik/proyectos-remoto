package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

import java.math.BigDecimal;
import java.util.Date;

public class TASCuentasPConsV4 extends ApiObjeto {

    public BigDecimal acuerdo;
    public String adelantoCuentaAsociada;
    public BigDecimal adelantoDisponible;
    public BigDecimal adelantoInteresesDevengados;
    public BigDecimal adelantoUtilizado;

    public Boolean adicionales;

    public Boolean bloqueado;
    public Integer cantidadTitulares;
    public String categoria;
    public String categoriaImpositiva;
    public String descEstado;
    public String descMoneda;
    public String descSucursal;
    public String descTipoTitularidad;
    public BigDecimal disponible;
    public String estado;
    public String estadoCuenta;
    public String fechaAlta;
    public Date fechaBaja;//Fecha
    public Integer idDomicilio;
    public Integer idPaquete;
    public String idProducto;
    public String moneda;
    public Boolean muestraPaquete;
    public String numeroProducto;
    public String orden;
    public Boolean paquetizado;
    public Integer sucursal;
    public String tipoOperacion;
    public String tipoProducto;
    public String tipoTitularidad;
    public String valoresEnSuspenso;

    public boolean errorApi;
    public Exception errorDesc;

    public TASCuentasPConsV4() {
    }
    public BigDecimal getAcuerdo() {
        return acuerdo;
    }

    public void setAcuerdo(BigDecimal acuerdo) {
        this.acuerdo = acuerdo;
    }

    public String getAdelantoCuentaAsociada() {
        return adelantoCuentaAsociada;
    }

    public void setAdelantoCuentaAsociada(String adelantoCuentaAsociada) {
        this.adelantoCuentaAsociada = adelantoCuentaAsociada;
    }

    public BigDecimal getAdelantoDisponible() {
        return adelantoDisponible;
    }

    public void setAdelantoDisponible(BigDecimal adelantoDisponible) {
        this.adelantoDisponible = adelantoDisponible;
    }

    public BigDecimal getAdelantoInteresesDevengados() {
        return adelantoInteresesDevengados;
    }

    public void setAdelantoInteresesDevengados(BigDecimal adelantoInteresesDevengados) {
        this.adelantoInteresesDevengados = adelantoInteresesDevengados;
    }

    public BigDecimal getAdelantoUtilizado() {
        return adelantoUtilizado;
    }

    public void setAdelantoUtilizado(BigDecimal adelantoUtilizado) {
        this.adelantoUtilizado = adelantoUtilizado;
    }

    public Boolean isAdicionales() {
        return adicionales;
    }

    public void setAdicionales(Boolean adicionales) {
        this.adicionales = adicionales;
    }

    public Boolean isBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(Boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public Integer getCantidadTitulares() {
        return cantidadTitulares;
    }

    public void setCantidadTitulares(Integer cantidadTitulares) {
        this.cantidadTitulares = cantidadTitulares;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCategoriaImpositiva() {
        return categoriaImpositiva;
    }

    public void setCategoriaImpositiva(String categoriaImpositiva) {
        this.categoriaImpositiva = categoriaImpositiva;
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

    public BigDecimal getDisponible() {
        return disponible;
    }

    public void setDisponible(BigDecimal disponible) {
        this.disponible = disponible;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEstadoCuenta() {
        return estadoCuenta;
    }

    public void setEstadoCuenta(String estadoCuenta) {
        this.estadoCuenta = estadoCuenta;
    }

    public String getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(String fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public Date getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(Date fechaBaja) {
        this.fechaBaja = fechaBaja;
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

    public String getOrden() {
        return orden;
    }

    public void setOrden(String orden) {
        this.orden = orden;
    }

    public Boolean isPaquetizado() {
        return paquetizado;
    }

    public void setPaquetizado(Boolean paquetizado) {
        this.paquetizado = paquetizado;
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

    public String getValoresEnSuspenso() {
        return valoresEnSuspenso;
    }

    public void setValoresEnSuspenso(String valoresEnSuspenso) {
        this.valoresEnSuspenso = valoresEnSuspenso;
    }

    public Boolean getAdicionales() {
        return adicionales;
    }

    public Boolean getBloqueado() {
        return bloqueado;
    }

    public Boolean getMuestraPaquete() {
        return muestraPaquete;
    }

    public Boolean getPaquetizado() {
        return paquetizado;
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
