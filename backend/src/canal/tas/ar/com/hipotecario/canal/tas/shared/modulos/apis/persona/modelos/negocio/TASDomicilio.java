package ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;


public class TASDomicilio extends ApiObjeto {

    private Long 	id;
    private String 	idTipoDomicilio;
    private String 	calle;
    private Integer numero;
    private String 	piso;
    private String 	departamento;
    private String 	calleEntre1;
    private String 	calleEntre2;
    private String 	idCodigoPostal;
    private String 	codigoPostalAmpliado;
    private Integer idCiudad;
    private Integer idProvincia;
    private Integer idPais;
    private String 	ubicacion;
    private Integer idCore;
    private String 	barrio;
    private String 	latitud;
    private String 	longitud;
    private String 	canalModificacion;
    private String    fechaCreacion;
    private String 	usuarioModificacion;
    private String 	fechaModificacion;
    private Integer eTag;
    private String 	provincia;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdTipoDomicilio() {
        return idTipoDomicilio;
    }

    public void setIdTipoDomicilio(String idTipoDomicilio) {
        this.idTipoDomicilio = idTipoDomicilio;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public String getPiso() {
        return piso;
    }

    public void setPiso(String piso) {
        this.piso = piso;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getCalleEntre1() {
        return calleEntre1;
    }

    public void setCalleEntre1(String calleEntre1) {
        this.calleEntre1 = calleEntre1;
    }

    public String getCalleEntre2() {
        return calleEntre2;
    }

    public void setCalleEntre2(String calleEntre2) {
        this.calleEntre2 = calleEntre2;
    }

    public String getIdCodigoPostal() {
        return idCodigoPostal;
    }

    public void setIdCodigoPostal(String idCodigoPostal) {
        this.idCodigoPostal = idCodigoPostal;
    }

    public String getCodigoPostalAmpliado() {
        return codigoPostalAmpliado;
    }

    public void setCodigoPostalAmpliado(String codigoPostalAmpliado) {
        this.codigoPostalAmpliado = codigoPostalAmpliado;
    }

    public Integer getIdCiudad() {
        return idCiudad;
    }

    public void setIdCiudad(Integer idCiudad) {
        this.idCiudad = idCiudad;
    }

    public Integer getIdProvincia() {
        return idProvincia;
    }

    public void setIdProvincia(Integer idProvincia) {
        this.idProvincia = idProvincia;
    }

    public Integer getIdPais() {
        return idPais;
    }

    public void setIdPais(Integer idPais) {
        this.idPais = idPais;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Integer getIdCore() {
        return idCore;
    }

    public void setIdCore(Integer idCore) {
        this.idCore = idCore;
    }

    public String getBarrio() {
        return barrio;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
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

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }
}
