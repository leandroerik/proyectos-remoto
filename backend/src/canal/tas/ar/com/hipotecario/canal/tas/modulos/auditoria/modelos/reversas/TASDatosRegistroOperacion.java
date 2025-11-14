package ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.reversas;

import ar.com.hipotecario.backend.base.Objeto;

import java.util.Date;

public class TASDatosRegistroOperacion extends Objeto {

    private String importe;
    private String moneda;
    private String producto;
    private String sucursal;
    private String tas;
    private String nroCuenta;
    private String precinto;
    private String lote;
    private String numeroTicket;
    private String reversa;
    private String idProcessReversado;
    private String codigoError;
    private String idReferenciaOrigen;
    private String codigoCliente;
    private String processId;
    private Date fecha;

    public TASDatosRegistroOperacion() {
    }

    public String getImporte() {
        return importe;
    }

    public void setImporte(String importe) {
        this.importe = importe;
    }

    public String getProducto() {
        return producto;
    }

    public void setProducto(String producto) {
        this.producto = producto;
    }

    public String getTas() {
        return tas;
    }

    public void setTas(String tas) {
        this.tas = tas;
    }

    public String getNroCuenta() {
        return nroCuenta;
    }

    public void setNroCuenta(String nroCuenta) {
        this.nroCuenta = nroCuenta;
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

    public String getReversa() {
        return reversa;
    }

    public void setReversa(String reversa) {
        this.reversa = reversa;
    }

    public String getIdProcessReversado() {
        return idProcessReversado;
    }

    public void setIdProcessReversado(String idProcessReversado) {
        this.idProcessReversado = idProcessReversado;
    }

    public String getCodigoError() {
        return codigoError;
    }

    public void setCodigoError(String codigoError) {
        this.codigoError = codigoError;
    }

    public String getIdReferenciaOrigen() {
        return idReferenciaOrigen;
    }

    public void setIdReferenciaOrigen(String idReferenciaOrigen) {
        this.idReferenciaOrigen = idReferenciaOrigen;
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }
}
