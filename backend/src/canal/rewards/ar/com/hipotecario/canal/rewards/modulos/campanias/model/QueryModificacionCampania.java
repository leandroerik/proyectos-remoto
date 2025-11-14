package ar.com.hipotecario.canal.rewards.modulos.campanias.model;

public class QueryModificacionCampania {
    private String secuencial;
    private String estado;
    private String usuario;

    public void setSecuencial(String secuencial) {
        this.secuencial = secuencial;
    }

    public String getSecuencial() {
        return secuencial;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEstado() {
        return estado;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getUsuario() {
        return usuario;
    }
}
