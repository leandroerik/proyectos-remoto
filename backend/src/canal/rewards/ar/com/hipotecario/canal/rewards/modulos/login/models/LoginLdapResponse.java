package ar.com.hipotecario.canal.rewards.modulos.login.models;

import java.util.List;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class LoginLdapResponse extends ApiObjeto {

    private boolean autenticado;
    private String nombre;
    private String usuario;
    private List<String> roles;

    public boolean isAutenticado() {
        return autenticado;
    }

    public void setAutenticado(boolean autenticado) {
        this.autenticado = autenticado;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "LoginLdapResponse [autenticado=" + autenticado + ", nombre=" + nombre + ", usuario=" + usuario
                + ", roles=" + roles + "]";
    }

}
