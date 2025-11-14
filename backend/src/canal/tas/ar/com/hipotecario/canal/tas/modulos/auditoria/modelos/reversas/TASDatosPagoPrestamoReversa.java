package ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.reversas;

import ar.com.hipotecario.backend.base.Objeto;

import java.util.Date;

public class TASDatosPagoPrestamoReversa extends Objeto {

    private String idReversa;
    private String codRespuesta;

    private String nroPrestamo;
    private String importe;
    private String precinto;
    private String lote;
    private String tas;
    private String sucursalId;
    private String numeroTicket;
    private Date fecha;

    public TASDatosPagoPrestamoReversa() {
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

    public String getNroPrestamo() {
        return nroPrestamo;
    }

    public void setNroPrestamo(String nroPrestamo) {
        this.nroPrestamo = nroPrestamo;
    }

    public String getImporte() {
        return importe;
    }

    public void setImporte(String importe) {
        this.importe = importe;
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

    public String getTas() {
        return tas;
    }

    public void setTas(String tas) {
        this.tas = tas;
    }

    public String getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(String sucursalId) {
        this.sucursalId = sucursalId;
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
