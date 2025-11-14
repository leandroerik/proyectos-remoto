package ar.com.hipotecario.mobile.negocio.cuentainversor.cuentacomitente.CNV.account;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoPropietario {
    private Propietario propietario; //owner
    private Boolean propietarioPrincipal; // primaryOwner

    public CoPropietario() {
    }

    public CoPropietario(Propietario propietario, Boolean propietarioPrincipal) {
        this.propietario = propietario;
        this.propietarioPrincipal = propietarioPrincipal;
    }

    public Propietario getPropietario() {
        return propietario;
    }

    public void setPropietario(Propietario propietario) {
        this.propietario = propietario;
    }

    public Boolean getPropietarioPrincipal() {
        return propietarioPrincipal;
    }

    public void setPropietarioPrincipal(Boolean propietarioPrincipal) {
        this.propietarioPrincipal = propietarioPrincipal;
    }
}

