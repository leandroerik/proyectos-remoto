package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente;

import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.CNV.account.CoPropietario;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.CNV.account.CuentaInversorCNV;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.CNV.account.GrupoCuenta;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.CNV.account.ValoresCuenta;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.CNV.investor.*;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.ctaunitrade.CuentaComitenteUnitrade;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.util.List;

//Request para alta unitrade + alta investor cnv + alta account cnv | Falta unitrade
public class AltaCuentaComitenteRequestUnificado {
    private List<InversorCNV> inversorCNVList;
    private CuentaInversorCNV cuentaInversorCNV;
    private CuentaComitenteUnitrade cuentaComitente;

    public AltaCuentaComitenteRequestUnificado() {
    }

    public AltaCuentaComitenteRequestUnificado(List<InversorCNV> inversorCNVList, CuentaInversorCNV cuentaInversorCNV, CuentaComitenteUnitrade cuentaComitente) {
        this.inversorCNVList = inversorCNVList;
        this.cuentaInversorCNV = cuentaInversorCNV;
        this.cuentaComitente = cuentaComitente;
    }

    public List<InversorCNV> getInversorCNVList() {
        return inversorCNVList;
    }

    public void setInversorCNVList(List<InversorCNV> inversorCNVList) {
        this.inversorCNVList = inversorCNVList;
    }

    public CuentaInversorCNV getCuentaInversorCNV() {
        return cuentaInversorCNV;
    }

    public void setCuentaInversorCNV(CuentaInversorCNV cuentaInversorCNV) {
        this.cuentaInversorCNV = cuentaInversorCNV;
    }

    public CuentaComitenteUnitrade getCuentaComitente() {
        return cuentaComitente;
    }

    public void setCuentaComitente(CuentaComitenteUnitrade cuentaComitente) {
        this.cuentaComitente = cuentaComitente;
    }

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
