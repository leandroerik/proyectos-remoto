package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

import java.util.List;

public class Cliente extends ApiObjeto {
    public String apellido;
    public Boolean cliente;
    public Contacto contacto;
    public Cuenta cuenta;
    public List<Cuenta> cuentas;
    public String fechaNacimiento;
    public String idCliente;
    public String idTributario;
    public Integer loteId;
    public String nombre;
    public String nombreCompleto;
    public String nombre_fantasia;
    public String numeroDocumento;
    public String prestacion;
    public String razonSocial;
    public String rferencia;
    public String rubro;
    public String sexo;
    public String terminal;
    public String tipoDeDocumento;
    public String tipoIdTributario;
    public String titular;

    public Cliente(String apellido, Boolean cliente, Contacto contacto, Cuenta cuenta, List<Cuenta> cuentas, String fechaNacimiento, String idCliente, String idTributario, Integer loteId, String nombre, String nombreCompleto, String nombre_fantasia, String numeroDocumento, String prestacion, String razonSocial, String rferencia, String rubro, String sexo, String terminal, String tipoDeDocumento, String tipoIdTributario, String titular) {
        this.apellido = apellido;
        this.cliente = cliente;
        this.contacto = contacto;
        this.cuenta = cuenta;
        this.cuentas = cuentas;
        this.fechaNacimiento = fechaNacimiento;
        this.idCliente = idCliente;
        this.idTributario = idTributario;
        this.loteId = loteId;
        this.nombre = nombre;
        this.nombreCompleto = nombreCompleto;
        this.nombre_fantasia = nombre_fantasia;
        this.numeroDocumento = numeroDocumento;
        this.prestacion = prestacion;
        this.razonSocial = razonSocial;
        this.rferencia = rferencia;
        this.rubro = rubro;
        this.sexo = sexo;
        this.terminal = terminal;
        this.tipoDeDocumento = tipoDeDocumento;
        this.tipoIdTributario = tipoIdTributario;
        this.titular = titular;
    }

    public Cliente(String idTributario, Cuenta cuenta) {
        this.idTributario = idTributario;
        this.cuenta = cuenta;
    }
    
    public Cliente(String idTributario, String nombreCompleto, Cuenta cuenta) {
        this.idTributario = idTributario;
        this.nombreCompleto = nombreCompleto;
        this.cuenta = cuenta;
    }

    public Cliente(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public Cliente() {
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public Boolean getCliente() {
        return cliente;
    }

    public void setCliente(Boolean cliente) {
        this.cliente = cliente;
    }

    public Contacto getContacto() {
        return contacto;
    }

    public void setContacto(Contacto contacto) {
        this.contacto = contacto;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public List<Cuenta> getCuentas() {
        return cuentas;
    }

    public void setCuentas(List<Cuenta> cuentas) {
        this.cuentas = cuentas;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente;
    }

    public String getIdTributario() {
        return idTributario;
    }

    public void setIdTributario(String idTributario) {
        this.idTributario = idTributario;
    }

    public Integer getLoteId() {
        return loteId;
    }

    public void setLoteId(Integer loteId) {
        this.loteId = loteId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getNombre_fantasia() {
        return nombre_fantasia;
    }

    public void setNombre_fantasia(String nombre_fantasia) {
        this.nombre_fantasia = nombre_fantasia;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getPrestacion() {
        return prestacion;
    }

    public void setPrestacion(String prestacion) {
        this.prestacion = prestacion;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getRferencia() {
        return rferencia;
    }

    public void setRferencia(String rferencia) {
        this.rferencia = rferencia;
    }

    public String getRubro() {
        return rubro;
    }

    public void setRubro(String rubro) {
        this.rubro = rubro;
    }

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public String getTipoDeDocumento() {
        return tipoDeDocumento;
    }

    public void setTipoDeDocumento(String tipoDeDocumento) {
        this.tipoDeDocumento = tipoDeDocumento;
    }

    public String getTipoIdTributario() {
        return tipoIdTributario;
    }

    public void setTipoIdTributario(String tipoIdTributario) {
        this.tipoIdTributario = tipoIdTributario;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }
}
