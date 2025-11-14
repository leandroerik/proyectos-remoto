package ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.model;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class SolicitudAjuste extends ApiObjeto {
    private Integer cliente;
    private String fecha;
    private String observacion;
    private String operacion;
    private Integer puntos;
    private String signo;
    private String usuarioIngreso;
    private String idcliente;

    public SolicitudAjuste() {
    }

    public Integer getCliente() {
        return cliente;
    }

    public void setCliente(Integer cliente) {
        this.cliente = cliente;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public String getOperacion() {
        return operacion;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
    }

    public Integer getPuntos() {
        return puntos;
    }

    public void setPuntos(Integer puntos) {
        this.puntos = puntos;
    }

    public String getSigno() {
        return signo;
    }

    public void setSigno(String signo) {
        this.signo = signo;
    }

    public String getUsuarioIngreso() {
        return usuarioIngreso;
    }

    public void setUsuarioIngreso(String usuarioIngreso) {
        this.usuarioIngreso = usuarioIngreso;
    }

    public String getIdcliente() {
        return idcliente;
    }

    public void setIdcliente(String idcliente) {
        this.idcliente = idcliente;
    }

    @Override
    public String toString() {
        return "SolicitudAjuste [cliente=" + cliente + ", fecha=" + fecha + ", observacion=" + observacion
                + ", operacion=" + operacion + ", puntos=" + puntos + ", signo=" + signo + ", usuarioIngreso="
                + usuarioIngreso + ", idcliente=" + idcliente + "]";
    }

}
