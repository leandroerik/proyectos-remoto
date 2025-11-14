package ar.com.hipotecario.canal.officebanking.dto;

import ar.com.hipotecario.backend.base.Objeto;

public class ErrorArchivoOB {

	public String titulo;
	public String descripcion;
	public Integer linea;
	public String campo;

	public Objeto setErroresArchivo(String titulo, String descripcion, Integer linea, String campo) {
		Objeto error = new Objeto();
		error.set("estado", "ERROR");

		Objeto datos = new Objeto();
		datos.set("titulo", titulo);
		datos.set("descripcion", descripcion);
		datos.set("linea", linea);
		datos.set("campo", campo);
		error.set("datos", datos);
		return error;
	}

	public ErrorArchivoOB(String titulo, String descripcion, Integer linea, String campo) {
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.linea = linea;
		this.campo = campo;
	}

	public ErrorArchivoOB() {
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Integer getLinea() {
		return linea;
	}

	public void setLinea(Integer linea) {
		this.linea = linea;
	}

	public String getCampo() {
		return campo;
	}

	public void setCampo(String campo) {
		this.campo = campo;
	}
}
