package ar.com.hipotecario.canal.rewards.modulos.campanias.model;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class CampaniaDetalle extends ApiObjeto {

    private String fechaIn;
    private String cliente;
    private String nombre;
    private String signo;
    private String puntos;
    private String codMov;
    private String usuario;
    private String hora;
    private String observ;
    private String codigoRegistroTotales;
    private String secuencial;

    public String getFechaIn() {
        return fechaIn;
    }

    public void setFechaIn(String fechaIn) {
        this.fechaIn = fechaIn;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSigno() {
        return signo;
    }

    public void setSigno(String signo) {
        this.signo = signo;
    }

    public String getPuntos() {
        return puntos;
    }

    public void setPuntos(String puntos) {
        this.puntos = puntos;
    }

    public String getCodMov() {
        return codMov;
    }

    public void setCodMov(String codMov) {
        this.codMov = codMov;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getObserv() {
        return observ;
    }

    public void setObserv(String observ) {
        this.observ = observ;
    }

    public String getCodigoRegistroTotales() {
        return codigoRegistroTotales;
    }

    public void setCodigoRegistroTotales(String codigoRegistroTotales) {
        this.codigoRegistroTotales = codigoRegistroTotales;
    }

    public String getSecuencial() {
        return secuencial;
    }

    public void setSecuencial(String secuencial) {
        this.secuencial = secuencial;
    }
}
