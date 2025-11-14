package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Conceptos")
public class ConceptoOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;

	@Column(name = "descripcion", nullable = false)
	public String descripcion;
	
	@Column(name = "codigo", nullable = false)
	public String codigo;
	
	

}