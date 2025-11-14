package ar.com.hipotecario.canal.rewards.middleware.models.negocio;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RWRechazo  extends ApiObjeto{
    private String programa;
    private String entidad;
    private String tipoNovedad;
    private String lote;
    private String categoria;
    private String tipoDocumento;
    private String nombreYApellido;
    private String nombre;
    private String apellido;
    private String domicilioCompleto;
    private String calle;
    private String numero;
    private String piso;
    private String localidad;
    private String codigoPostal;
    private String prefijoTelefono;
    private String telefono;
    private String fechaNacimiento;
    private String sexo;
    private String estadoCivil;
    private String email;
    private String tipoTarjeta;
    private String actividadProfesional;
    private String nombreTarjeta;
    private String numeroSocio;
    private String numeroTarjeta;
    private String codigoRechazo;
    private String idCliente;
    private String numeroCuenta;
    private String estado;
    private String fechaEnvio;
    private String fechaRespuesta;
    private String editables;
    private String numeroNovedad;
    private String provincia;
    private String numeroDocumento;
    private String nivel;

    public RWRechazo() {
    }

    public RWRechazo(Objeto obj) {
        this.programa = obj.string("programa", "").trim();
        this.entidad = obj.string("entidad","").trim();
        this.tipoNovedad = obj.string("tipoNovedad","").trim();
        this.lote = obj.string("lote","").trim();
        this.categoria = obj.string("categoria","").trim();
        this.tipoDocumento = obj.string("tipoDocumento","").trim();
        this.nombreYApellido = obj.string("nombreYApellido","").trim();
        this.nombre = obj.string("nombre","").trim();
        this.apellido = obj.string("apellido","").trim();
        this.domicilioCompleto = obj.string("domicilioCompleto","").trim();
        this.calle = obj.string("calle","").trim();
        this.numero = obj.string("numero","").trim();
        this.piso = obj.string("piso;","").trim(); // Ojo: campo mal nombrado, corregir si es "piso"
        this.localidad = obj.string("localidad","").trim();
        this.codigoPostal = obj.string("codigoPostal","").trim();
        this.prefijoTelefono = obj.string("prefijoTelefono","").trim();
        this.telefono = obj.string("telefono","").trim();
        this.fechaNacimiento = this.parsearFecha(obj.string("fechaNacimiento","").trim());
        this.sexo = obj.string("sexo","").trim();
        this.estadoCivil = obj.string("estadoCivil","").trim();
        this.email = obj.string("email","").trim();
        this.tipoTarjeta = obj.string("tipoTarjeta","").trim();
        this.actividadProfesional = obj.string("actividadProfesional","").trim();
        this.nombreTarjeta = obj.string("nombreTarjeta","").trim();
        this.numeroSocio = obj.string("numeroSocio","").trim();
        this.numeroTarjeta = obj.string("numeroTarjeta","").trim();
        this.codigoRechazo = obj.string("codigoRechazo","").trim();
        this.idCliente = obj.string("idCliente","").trim();
        this.numeroCuenta = obj.string("numeroCuenta","").trim();
        this.estado = obj.string("estado","").trim();
        this.fechaEnvio = obj.string("fechaEnvio","").trim();
        this.fechaRespuesta = obj.string("fechaRespuesta","").trim();
        this.editables = obj.string("editables","").trim();
        this.numeroNovedad = obj.string("numeroNovedad","").trim();
        this.provincia = obj.string("provincia","").trim();
        this.numeroDocumento = obj.string("numeroDocumento","").trim();
        this.nivel = "";
    }

    private static String parsearFecha(String fechaNacimiento){
        if (fechaNacimiento.contains("/")) {
            return fechaNacimiento;
        }
        else{
            LocalDate fecha = LocalDate.parse(fechaNacimiento);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return fecha.format(formatter);
        }
    }

    public String toStringGestionRechazo()
    {
        //AGREGAR ULTIMO CAMPO 'NIVEL' EN BASE AL ROL EN LA SESION DEL USUARIO
        //EL CAMPO 'EDITABLES' NO MANDARLO
        // MANTENER EL ORDEN DE LOS PARAMAETROS, YA QUE LO MAPEA ASI DESDE EL SP.
        return  (this.programa+"|"+this.entidad+"|"+this.tipoNovedad+"|"+this.lote+"|"+this.numeroNovedad+"|"+
                this.categoria+"|"+this.tipoDocumento+"|"+this.numeroDocumento+"|"+this.nombreYApellido+"|"+this.nombre+"|"+
                this.apellido+"|"+this.domicilioCompleto+"|"+this.calle+"|"+this.numero+"|"+this.piso+"|"+this.localidad+"|"+
                this.provincia+"|"+this.codigoPostal+"|"+this.prefijoTelefono+"|"+this.telefono+"|"+this.fechaNacimiento+"|"+
                this.sexo+"|"+this.estadoCivil+"|"+this.email+"|"+this.tipoTarjeta+"|"+this.actividadProfesional+"|"+
                this.nombreTarjeta+"|"+this.numeroSocio+"|"+this.numeroTarjeta+"|"+this.codigoRechazo+"|"+this.idCliente+"|"+
                this.numeroCuenta+"|"+this.estado+"|"+this.fechaEnvio+"|"+this.fechaRespuesta+'|'+this.nivel);
    }

    // Getters y Setters
    public String getPrograma() {
        return programa;
    }

    public void setPrograma(String programa) {
        this.programa = programa;
    }
    public String getNivel() {
        return entidad;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public String getTipoNovedad() {
        return tipoNovedad;
    }

    public void setTipoNovedad(String tipoNovedad) {
        this.tipoNovedad = tipoNovedad;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNombreYApellido() {
        return nombreYApellido;
    }

    public void setNombreYApellido(String nombreYApellido) {
        this.nombreYApellido = nombreYApellido;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDomicilioCompleto() {
        return domicilioCompleto;
    }

    public void setDomicilioCompleto(String domicilioCompleto) {
        this.domicilioCompleto = domicilioCompleto;
    }

    public String getCalle() {
        return calle;
    }

    public void setCalle(String calle) {
        this.calle = calle;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getPiso() {
        return piso;
    }

    public void setPiso(String piso) {
        this.piso = piso;
    }

    public String getLocalidad() {
        return localidad;
    }

    public void setLocalidad(String localidad) {
        this.localidad = localidad;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getPrefijoTelefono() {
        return prefijoTelefono;
    }

    public void setPrefijoTelefono(String prefijoTelefono) {
        this.prefijoTelefono = prefijoTelefono;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(String estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTipoTarjeta() {
        return tipoTarjeta;
    }

    public void setTipoTarjeta(String tipoTarjeta) {
        this.tipoTarjeta = tipoTarjeta;
    }

    public String getActividadProfesional() {
        return actividadProfesional;
    }

    public void setActividadProfesional(String actividadProfesional) {
        this.actividadProfesional = actividadProfesional;
    }

    public String getNombreTarjeta() {
        return nombreTarjeta;
    }

    public void setNombreTarjeta(String nombreTarjeta) {
        this.nombreTarjeta = nombreTarjeta;
    }

    public String getNumeroSocio() {
        return numeroSocio;
    }

    public void setNumeroSocio(String numeroSocio) {
        this.numeroSocio = numeroSocio;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getCodigoRechazo() {
        return codigoRechazo;
    }

    public void setCodigoRechazo(String codigoRechazo) {
        this.codigoRechazo = codigoRechazo;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(String fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(String fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public String getEditables() {
        return editables;
    }

    public void setEditables(String editables) {
        this.editables = editables;
    }

    public String getNumeroNovedad() {
        return numeroNovedad;
    }

    public void setNumeroNovedad(String numeroNovedad) {
        this.numeroNovedad = numeroNovedad;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }
}
