package ar.com.hipotecario.canal.tas.modulos.plazosfijos.modelos;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class TASTiposPF extends ApiObjeto {

    String codigo;
    boolean esUva;
    String descripcion;
    String codigoMoneda;

    boolean esEmpleado;

    public TASTiposPF() {
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public boolean isEsUva() {
        return esUva;
    }

    public void setEsUva(boolean esUva) {
        this.esUva = esUva;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCodigoMoneda() {
        return codigoMoneda;
    }

    public void setCodigoMoneda(String codigoMoneda) {
        this.codigoMoneda = codigoMoneda;
    }

    public boolean isEsEmpleado() {
        return esEmpleado;
    }

    public void setEsEmpleado(boolean esEmpleado) {
        this.esEmpleado = esEmpleado;
    }
}
