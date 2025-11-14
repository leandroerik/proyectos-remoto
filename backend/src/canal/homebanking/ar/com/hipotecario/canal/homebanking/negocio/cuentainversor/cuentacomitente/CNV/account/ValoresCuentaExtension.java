package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.CNV.account;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValoresCuentaExtension {
    public Boolean requiereAprobacionEntregas; //deliveriesReqApproval
        public Boolean declaracionTrimestralPorPublicacion; //quarterlyStatementByPost
    public Boolean valoresPrestamo; //securitiesLending

    public ValoresCuentaExtension() {
    }

    public ValoresCuentaExtension(boolean requiereAprobacionEntregas, boolean declaracionTrimestralPorPublicacion, boolean valoresPrestamo) {
        this.requiereAprobacionEntregas = requiereAprobacionEntregas;
        this.declaracionTrimestralPorPublicacion = declaracionTrimestralPorPublicacion;
        this.valoresPrestamo = valoresPrestamo;
    }

    public Boolean getRequiereAprobacionEntregas() {
        return requiereAprobacionEntregas;
    }

    public void setRequiereAprobacionEntregas(Boolean requiereAprobacionEntregas) {
        this.requiereAprobacionEntregas = requiereAprobacionEntregas;
    }

    public Boolean getDeclaracionTrimestralPorPublicacion() {
        return declaracionTrimestralPorPublicacion;
    }

    public void setDeclaracionTrimestralPorPublicacion(Boolean declaracionTrimestralPorPublicacion) {
        this.declaracionTrimestralPorPublicacion = declaracionTrimestralPorPublicacion;
    }

    public Boolean getValoresPrestamo() {
        return valoresPrestamo;
    }

    public void setValoresPrestamo(Boolean valoresPrestamo) {
        this.valoresPrestamo = valoresPrestamo;
    }
}
