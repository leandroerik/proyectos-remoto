package ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class TASTelefono extends ApiObjeto {

    private Long 	id;
    private String 	idTipoTelefono;
    private String 	codigoPais;
    private String 	codigoArea;
    private String 	prefijo;
    private String 	caracteristica;
    private String 	numero;
    private String 	interno;
    private String 	idTelefonoPertenencia;
    private Integer idCore;
    private Integer prioridad;
    private Boolean esListaNegra;
    private String 	numeroNormalizado;
    private String 	canalModificacion;
    private String 	fechaCreacion;
    private String 	usuarioModificacion;
    private String fechaModificacion;
    private Boolean esParaRecibirSMS;
    private Integer eTag;
    private Integer idDireccion;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdTipoTelefono() {
        return idTipoTelefono;
    }

    public void setIdTipoTelefono(String idTipoTelefono) {
        this.idTipoTelefono = idTipoTelefono;
    }

    public String getCodigoPais() {
        return codigoPais;
    }

    public void setCodigoPais(String codigoPais) {
        this.codigoPais = codigoPais;
    }

    public String getCodigoArea() {
        return codigoArea;
    }

    public void setCodigoArea(String codigoArea) {
        this.codigoArea = codigoArea;
    }

    public String getPrefijo() {
        return prefijo;
    }

    public void setPrefijo(String prefijo) {
        this.prefijo = prefijo;
    }

    public String getCaracteristica() {
        return caracteristica;
    }

    public void setCaracteristica(String caracteristica) {
        this.caracteristica = caracteristica;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getInterno() {
        return interno;
    }

    public void setInterno(String interno) {
        this.interno = interno;
    }

    public String getIdTelefonoPertenencia() {
        return idTelefonoPertenencia;
    }

    public void setIdTelefonoPertenencia(String idTelefonoPertenencia) {
        this.idTelefonoPertenencia = idTelefonoPertenencia;
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

    public Boolean getEsListaNegra() {
        return esListaNegra;
    }

    public void setEsListaNegra(Boolean esListaNegra) {
        this.esListaNegra = esListaNegra;
    }

    public String getNumeroNormalizado() {
        return numeroNormalizado;
    }

    public void setNumeroNormalizado(String numeroNormalizado) {
        this.numeroNormalizado = numeroNormalizado;
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

    public Boolean getEsParaRecibirSMS() {
        return esParaRecibirSMS;
    }

    public void setEsParaRecibirSMS(Boolean esParaRecibirSMS) {
        this.esParaRecibirSMS = esParaRecibirSMS;
    }

    public Integer geteTag() {
        return eTag;
    }

    public void seteTag(Integer eTag) {
        this.eTag = eTag;
    }

    public Integer getIdDireccion() {
        return idDireccion;
    }

    public void setIdDireccion(Integer idDireccion) {
        this.idDireccion = idDireccion;
    }
}
