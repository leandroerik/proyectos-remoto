package ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class TASMail extends ApiObjeto {

    private Long 	id;
    private String 	idTipoMail;
    private String 	direccion;
    private Integer idCore;
    private Integer prioridad;
    private Boolean esDeclarado;
    private String 	canalModificacion;
    private String 	fechaCreacion;
    private String 	usuarioModificacion;
    private String fechaModificacion;
    private Integer eTag;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdTipoMail() {
        return idTipoMail;
    }

    public void setIdTipoMail(String idTipoMail) {
        this.idTipoMail = idTipoMail;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Integer getIdCore() {
        return idCore;
    }

    public void setIdCore(Integer idCore) {
        this.idCore = idCore;
    }

    public Integer getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(Integer prioridad) {
        this.prioridad = prioridad;
    }

    public Boolean getEsDeclarado() {
        return esDeclarado;
    }

    public void setEsDeclarado(Boolean esDeclarado) {
        this.esDeclarado = esDeclarado;
    }

    public String getCanalModificacion() {
        return canalModificacion;
    }

    public void setCanalModificacion(String canalModificacion) {
        this.canalModificacion = canalModificacion;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public void setUsuarioModificacion(String usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }

    public String getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(String fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public Integer geteTag() {
        return eTag;
    }

    public void seteTag(Integer eTag) {
        this.eTag = eTag;
    }
}
