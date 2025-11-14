package ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.reversas;

import ar.com.hipotecario.backend.base.Objeto;

import java.math.BigDecimal;
import java.util.Date;

public class TASDatosPagoPrestamo extends Objeto {

    private String nroPrestamo;
    private String importe;
    private String precinto;
    private String lote;
    private String tas;
    private String sucursalId;
    private String numeroTicket;
    private String codigoCliente;
    private Date fecha;
    private String processId;

    public TASDatosPagoPrestamo() {
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

    public String getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(String sucursalId) {
        this.sucursalId = sucursalId;
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

    public String getNumeroTicket() {
        return numeroTicket;
    }

    public void setNumeroTicket(String numeroTicket) {
        this.numeroTicket = numeroTicket;
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }
}
