package ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.reversas;

import ar.com.hipotecario.backend.base.Objeto;

import java.util.Date;

public class TASDatosPagoTarjetaReversa extends Objeto {

    private String idReversa;
    private String numeroCuentaTarjeta;
    private String codigoMoneda;
    private String importeTotalEfectivo;
    private String kioscoId;
    private String sucursalId;
    private String precinto;
    private String lote;
    private String numeroTicket;
    private String  tipoTarjeta;
    private Date fecha;
    private String codRespuesta;

    public TASDatosPagoTarjetaReversa() {
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

    public String getNumeroCuentaTarjeta() {
        return numeroCuentaTarjeta;
    }

    public void setNumeroCuentaTarjeta(String numeroCuentaTarjeta) {
        this.numeroCuentaTarjeta = numeroCuentaTarjeta;
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

    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}
