package ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.reversas;

import ar.com.hipotecario.backend.base.Objeto;

import java.util.Date;

public class TASDatosDepositoReversa extends Objeto {

    private String idReversa;
    private String codRespuesta;
    private String desRespuesta;
    private String ssn;
    private String tipoCuenta;
    private String numeroCuenta;
    private String codigoMoneda;
    private String importeTotalEfectivo;
    private String kioscoId;
    private String sucursalId;
    private String precinto;
    private String lote;
    private String numeroTicket;
    private Date fecha;

    public TASDatosDepositoReversa() {
    }

    public String getIdReversa() {
        return idReversa;
    }

    public void setIdReversa(String idReversa) {
        this.idReversa = idReversa;
    }

    public String getCodRespuesta() {
        return codRespuesta;
    }

    public void setCodRespuesta(String codRespuesta) {
        this.codRespuesta = codRespuesta;
    }

    public String getDesRespuesta() {
        return desRespuesta;
    }

    public void setDesRespuesta(String desRespuesta) {
        this.desRespuesta = desRespuesta;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }

    public String getTipoCuenta() {
        return tipoCuenta;
    }

    public void setTipoCuenta(String tipoCuenta) {
        this.tipoCuenta = tipoCuenta;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getCodigoMoneda() {
        return codigoMoneda;
    }

    public void setCodigoMoneda(String codigoMoneda) {
        this.codigoMoneda = codigoMoneda;
    }

    public String getImporteTotalEfectivo() {
        return importeTotalEfectivo;
    }

    public void setImporteTotalEfectivo(String importeTotalEfectivo) {
        this.importeTotalEfectivo = importeTotalEfectivo;
    }

    public String getKioscoId() {
        return kioscoId;
    }

    public void setKioscoId(String kioscoId) {
        this.kioscoId = kioscoId;
    }

    public String getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(String sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getPrecinto() {
        return precinto;
    }

    public void setPrecinto(String precinto) {
        this.precinto = precinto;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getNumeroTicket() {
        return numeroTicket;
    }

    public void setNumeroTicket(String numeroTicket) {
        this.numeroTicket = numeroTicket;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}
