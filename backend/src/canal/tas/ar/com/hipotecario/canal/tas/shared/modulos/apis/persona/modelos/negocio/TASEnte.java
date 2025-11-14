package ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;


public class TASEnte extends ApiObjeto {
    private String 	 idCliente;
    private String 	 tipoDocumento;
    private String 	 numeroDocumento;
    private String 	 tipoIdentificacionTributaria;
    private String 	 numeroIdentificacionTributaria;
    private String 	 apellido;
    private String 	 nombre;
    private String 	 sexo;
    private String fechaNacimiento;
    private String fechaModificacion;
    private Boolean  alarmaDireccion;
    private Boolean  cliente;
    private Boolean  DDJJPEP;
    private String fechaDDJJPEP;
    private Boolean  marcaPEP;
    private Boolean  padronPEP;
    private String   perfilCliente;
    private String fechaPerfilCliente;

    private String tipoPersona;

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTipoIdentificacionTributaria() {
        return tipoIdentificacionTributaria;
    }

    public void setTipoIdentificacionTributaria(String tipoIdentificacionTributaria) {
        this.tipoIdentificacionTributaria = tipoIdentificacionTributaria;
    }

    public String getNumeroIdentificacionTributaria() {
        return numeroIdentificacionTributaria;
    }

    public void setNumeroIdentificacionTributaria(String numeroIdentificacionTributaria) {
        this.numeroIdentificacionTributaria = numeroIdentificacionTributaria;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(String fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public Boolean getAlarmaDireccion() {
        return alarmaDireccion;
    }

    public void setAlarmaDireccion(Boolean alarmaDireccion) {
        this.alarmaDireccion = alarmaDireccion;
    }

    public Boolean getCliente() {
        return cliente;
    }

    public void setCliente(Boolean cliente) {
        this.cliente = cliente;
    }

    public Boolean getDDJJPEP() {
        return DDJJPEP;
    }

    public void setDDJJPEP(Boolean DDJJPEP) {
        this.DDJJPEP = DDJJPEP;
    }

    public String getFechaDDJJPEP() {
        return fechaDDJJPEP;
    }

    public void setFechaDDJJPEP(String fechaDDJJPEP) {
        this.fechaDDJJPEP = fechaDDJJPEP;
    }

    public Boolean getMarcaPEP() {
        return marcaPEP;
    }

    public void setMarcaPEP(Boolean marcaPEP) {
        this.marcaPEP = marcaPEP;
    }

    public Boolean getPadronPEP() {
        return padronPEP;
    }

    public void setPadronPEP(Boolean padronPEP) {
        this.padronPEP = padronPEP;
    }

    public String getPerfilCliente() {
        return perfilCliente;
    }

    public void setPerfilCliente(String perfilCliente) {
        this.perfilCliente = perfilCliente;
    }

    public String getFechaPerfilCliente() {
        return fechaPerfilCliente;
    }

    public void setFechaPerfilCliente(String fechaPerfilCliente) {
        this.fechaPerfilCliente = fechaPerfilCliente;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getTipoPersona() {
        return tipoPersona;
    }

    public void setTipoPersona(String tipoPersona) {
        this.tipoPersona = tipoPersona;
    }
}
