package ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.dto;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.*;

import java.util.List;

public class TASPersonaDTO extends Objeto {
    private TASCliente datosCliente;
    private List<TASDomicilio> domicilios;
    private List<TASTelefono> telefonos;
    private List<TASMail> direccionesElectronicas;
    private List<TASEnte> entes;
    private List<TASDocumento> persDoc;
    private Boolean censado;

    public TASCliente getDatosCliente() {
        return datosCliente;
    }

    public void setDatosCliente(TASCliente datosCliente) {
        this.datosCliente = datosCliente;
    }

    public List<TASDomicilio> getDomicilios() {
        return domicilios;
    }

    public void setDomicilios(List<TASDomicilio> domicilios) {
        this.domicilios = domicilios;
    }

    public List<TASTelefono> getTelefonos() {
        return telefonos;
    }

    public void setTelefonos(List<TASTelefono> telefonos) {
        this.telefonos = telefonos;
    }

    public List<TASMail> getDireccionesElectronicas() {
        return direccionesElectronicas;
    }

    public void setDireccionesElectronicas(List<TASMail> direccionesElectronicas) {
        this.direccionesElectronicas = direccionesElectronicas;
    }

    public List<TASEnte> getEntes() {
        return entes;
    }

    public void setEntes(List<TASEnte> entes) {
        this.entes = entes;
    }

    public List<TASDocumento> getPersDoc() {
        return persDoc;
    }

    public void setPersDoc(List<TASDocumento> persDoc) {
        this.persDoc = persDoc;
    }

    public Boolean getCensado() {
        return censado;
    }

    public void setCensado(Boolean censado) {
        this.censado = censado;
    }
}
