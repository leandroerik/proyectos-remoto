package ar.com.hipotecario.canal.rewards.modulos.campanias.model;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class ActualizarCampania extends ApiObjeto {
    private String codigoRegistro;
    private String estado;
    private String usuario;

    public String getCodigoRegistro() {
        return codigoRegistro;
    }

    public void setCodigoRegistro(String codigoRegistro) {
        this.codigoRegistro = codigoRegistro;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
