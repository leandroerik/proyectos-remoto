package ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.modelos;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;

import java.math.BigDecimal;
import java.util.List;

public class TASCuentasCorriente extends ApiObjeto {
    public String cuenta;
    public String cbu;
    public String descEstado;
    public String moneda;
    public String descMoneda;
    public String tipoTitularidad;
    public String descTipoTitularidad;
    public String usoFirma;
    public String descUsoFirma;
    public String direccionEc;
    public String categoria;
    public String direccionCh;
    public String resumen;
    public String descCategoria;
    public String estado;
    public BigDecimal saldoGirar;
    public BigDecimal disponible;
    public BigDecimal disponibleUltimoDiaHabil;
    public BigDecimal acuerdo;
    public BigDecimal acuerdoDisponible;
    public BigDecimal tasaSobregiro;
    public BigDecimal acuerdoGiro;
    public Integer cantTitulares;
    public Fecha fechaUltimoDiaHabil;
    public Fecha fechaVencimientoAcuerdo;
    public Boolean depositoCheque;
    public Boolean esTransaccional;
    public Boolean bloqValor;
    public Boolean bloqMovim;

    public TASCuentasCorriente() {
    }

    public String getCuenta() {
        return cuenta;
    }

    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    public String getCbu() {
        return cbu;
    }

    public void setCbu(String cbu) {
        this.cbu = cbu;
    }

    public String getDescEstado() {
        return descEstado;
    }

    public void setDescEstado(String descEstado) {
        this.descEstado = descEstado;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getDescMoneda() {
        return descMoneda;
    }

    public void setDescMoneda(String descMoneda) {
        this.descMoneda = descMoneda;
    }

    public String getTipoTitularidad() {
        return tipoTitularidad;
    }

    public void setTipoTitularidad(String tipoTitularidad) {
        this.tipoTitularidad = tipoTitularidad;
    }

    public String getDescTipoTitularidad() {
        return descTipoTitularidad;
    }

    public void setDescTipoTitularidad(String descTipoTitularidad) {
        this.descTipoTitularidad = descTipoTitularidad;
    }

    public String getUsoFirma() {
        return usoFirma;
    }

    public void setUsoFirma(String usoFirma) {
        this.usoFirma = usoFirma;
    }

    public String getDescUsoFirma() {
        return descUsoFirma;
    }

    public void setDescUsoFirma(String descUsoFirma) {
        this.descUsoFirma = descUsoFirma;
    }

    public String getDireccionEc() {
        return direccionEc;
    }

    public void setDireccionEc(String direccionEc) {
        this.direccionEc = direccionEc;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDireccionCh() {
        return direccionCh;
    }

    public void setDireccionCh(String direccionCh) {
        this.direccionCh = direccionCh;
    }

    public String getResumen() {
        return resumen;
    }

    public void setResumen(String resumen) {
        this.resumen = resumen;
    }

    public String getDescCategoria() {
        return descCategoria;
    }

    public void setDescCategoria(String descCategoria) {
        this.descCategoria = descCategoria;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getSaldoGirar() {
        return saldoGirar;
    }

    public void setSaldoGirar(BigDecimal saldoGirar) {
        this.saldoGirar = saldoGirar;
    }

    public BigDecimal getDisponible() {
        return disponible;
    }

    public void setDisponible(BigDecimal disponible) {
        this.disponible = disponible;
    }

    public BigDecimal getDisponibleUltimoDiaHabil() {
        return disponibleUltimoDiaHabil;
    }

    public void setDisponibleUltimoDiaHabil(BigDecimal disponibleUltimoDiaHabil) {
        this.disponibleUltimoDiaHabil = disponibleUltimoDiaHabil;
    }

    public BigDecimal getAcuerdo() {
        return acuerdo;
    }

    public void setAcuerdo(BigDecimal acuerdo) {
        this.acuerdo = acuerdo;
    }

    public BigDecimal getAcuerdoDisponible() {
        return acuerdoDisponible;
    }

    public void setAcuerdoDisponible(BigDecimal acuerdoDisponible) {
        this.acuerdoDisponible = acuerdoDisponible;
    }

    public BigDecimal getTasaSobregiro() {
        return tasaSobregiro;
    }

    public void setTasaSobregiro(BigDecimal tasaSobregiro) {
        this.tasaSobregiro = tasaSobregiro;
    }

    public BigDecimal getAcuerdoGiro() {
        return acuerdoGiro;
    }

    public void setAcuerdoGiro(BigDecimal acuerdoGiro) {
        this.acuerdoGiro = acuerdoGiro;
    }

    public Integer getCantTitulares() {
        return cantTitulares;
    }

    public void setCantTitulares(Integer cantTitulares) {
        this.cantTitulares = cantTitulares;
    }

    public Fecha getFechaUltimoDiaHabil() {
        return fechaUltimoDiaHabil;
    }

    public void setFechaUltimoDiaHabil(Fecha fechaUltimoDiaHabil) {
        this.fechaUltimoDiaHabil = fechaUltimoDiaHabil;
    }

    public Fecha getFechaVencimientoAcuerdo() {
        return fechaVencimientoAcuerdo;
    }

    public void setFechaVencimientoAcuerdo(Fecha fechaVencimientoAcuerdo) {
        this.fechaVencimientoAcuerdo = fechaVencimientoAcuerdo;
    }

    public Boolean getDepositoCheque() {
        return depositoCheque;
    }

    public void setDepositoCheque(Boolean depositoCheque) {
        this.depositoCheque = depositoCheque;
    }

    public Boolean getEsTransaccional() {
        return esTransaccional;
    }

    public void setEsTransaccional(Boolean esTransaccional) {
        this.esTransaccional = esTransaccional;
    }

    public Boolean getBloqValor() {
        return bloqValor;
    }

    public void setBloqValor(Boolean bloqValor) {
        this.bloqValor = bloqValor;
    }

    public Boolean getBloqMovim() {
        return bloqMovim;
    }

    public void setBloqMovim(Boolean bloqMovim) {
        this.bloqMovim = bloqMovim;
    }

    public static Objeto toObjeto(List<TASCuentasCorriente> listaCuentas){
        Objeto response = new Objeto();
        if(listaCuentas.size() > 1){
            for(TASCuentasCorriente cuentas : listaCuentas){
                response.add(cuentas.objeto());
            }
        } else {
            response = listaCuentas.get(0).objeto();
        }
        return response;
    }
}
