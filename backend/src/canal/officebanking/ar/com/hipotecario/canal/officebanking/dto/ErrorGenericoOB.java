package ar.com.hipotecario.canal.officebanking.dto;

import ar.com.hipotecario.backend.base.Objeto;
public class ErrorGenericoOB {
    public String titulo;
    public String descripcion;

    public Objeto setErrores(String titulo, String descripcion) {
        Objeto error = new Objeto();
        error.set("estado", "ERROR");

        Objeto datos = new Objeto();
        datos.set("titulo", titulo);
        datos.set("descripcion", descripcion);
        error.set("datos", datos);

        return error;
    }

    public ErrorGenericoOB(String titulo, String descripcion) {
        this.titulo = titulo;
        this.descripcion = descripcion;
    }

    public ErrorGenericoOB() {
    }
}
