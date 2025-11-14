package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Estados_Usuario")
public class EstadoUsuarioOB implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "esu_codigo", nullable = false)
	public Integer codigo;

	@Column(name = "esu_desc", nullable = false)
	public String descripcion;
	
	public EstadoUsuarioOB() {
	}
	
	public EstadoUsuarioOB(int codigo, String descripcion) {
		this.codigo = codigo;
		this.descripcion = descripcion;	
	}

}