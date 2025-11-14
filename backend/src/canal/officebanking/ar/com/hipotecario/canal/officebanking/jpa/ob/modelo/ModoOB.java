package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Modo")
public class ModoOB implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "emp_codigo", nullable = false)
	public Integer empCodigo;

	@Column(name = "alta_comercio", nullable = false)
	public boolean altaComercio;
	
	@Column(name = "fecha_alta", nullable = false)
	public LocalDateTime fechaAlta;
	
	@Column(name = "fecha_modificacion", nullable = true)
	public LocalDateTime fechaModificacion;
	
}