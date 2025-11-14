package ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;

import java.util.ArrayList;
import java.util.List;

public class TASClientePersona extends ApiObjeto {

    public String idCliente;
    public String tipoDocumento;
    public String idTipoDocumento;
    public String numeroDocumento;
    public String numeroIdentificacionTributaria;
    public String apellido;
    public String nombre;
    public String sexo;
    public String fechaNacimiento;
    public String tipoPersona;

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

    public String getIdTipoDocumento() {
        return idTipoDocumento;
    }

    public void setIdTipoDocumento(String idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
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

    public static TASClientePersona fillDatos(Objeto obj){
        TASClientePersona pers = new TASClientePersona();
        pers.idCliente = obj.string("idCliente");
        pers.tipoDocumento = obj.string("tipoDocumento");
        pers.numeroDocumento = obj.string("numeroDocumento");
        pers.numeroIdentificacionTributaria = obj.string("numeroIdentificacionTributaria");
        pers.apellido = obj.string("apellido");
        pers.nombre = obj.string("nombre");
        pers.sexo = obj.string("sexo");
        pers.fechaNacimiento = obj.string("fechaNacimiento");
        pers.tipoPersona = obj.string("tipoPersona");

        return pers;
    }

    public List<TASClientePersona> toClienteList(Objeto obj){
        List <TASClientePersona> listPers = new ArrayList<>();
        for(Objeto ob : obj.objetos()){
            listPers.add(ob.toClass(TASClientePersona.class));
        }
        return listPers;
    }
    public static Objeto toObjeto(TASClientePersona cliente){
        Objeto rta = new Objeto();
        rta.set("idCliente", cliente.getIdCliente());
        rta.set("tipoDocumento", cliente.getTipoDocumento());
        rta.set("numeroDocumento", cliente.getNumeroDocumento());
        rta.set("numeroIdentificacionTributaria", cliente.getNumeroIdentificacionTributaria());
        rta.set("apellido", cliente.getApellido());
        rta.set("nombre", cliente.getNombre());
        rta.set("sexo", cliente.getSexo());
        rta.set("fechaNacimiento", cliente.getFechaNacimiento());
        rta.set("tipoPersona", cliente.getTipoPersona());
        return rta;
    }
    //sobrecarga varios resultados en la API
    public static Objeto toObjeto(List <TASClientePersona> list){
        Objeto rta = new Objeto();
        Objeto rtaFinal = new Objeto();
        for(TASClientePersona per : list) {
            rta.set("idCliente", per.idCliente);
            rta.set("tipoDocumento", per.tipoDocumento);
            rta.set("numeroDocumento", per.numeroDocumento);
            rta.set("numeroIdentificacionTributaria", per.numeroIdentificacionTributaria);
            rta.set("apellido", per.apellido);
            rta.set("nombre", per.nombre);
            rta.set("sexo", per.sexo);
            rta.set("fechaNacimiento", per.fechaNacimiento);
            rta.set("tipoPersona", per.tipoPersona);
        }
        return rtaFinal.add(rta);
    }
}
