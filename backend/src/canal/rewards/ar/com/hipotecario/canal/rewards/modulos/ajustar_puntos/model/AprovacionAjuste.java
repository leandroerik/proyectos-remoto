package ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.model;

import java.util.List;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class AprovacionAjuste extends ApiObjeto {
    private List<AjusteModificaciones> modificaciones;

    private String usuario;

    public AprovacionAjuste() {
    }

    public AprovacionAjuste(List<AjusteModificaciones> modificaciones, String usuario) {
        this.modificaciones = modificaciones;
        this.usuario = usuario;
    }

    public List<AjusteModificaciones> getModificaciones() {
        return modificaciones;
    }

    public void setModificaciones(List<AjusteModificaciones> modificaciones) {
        this.modificaciones = modificaciones;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

}
