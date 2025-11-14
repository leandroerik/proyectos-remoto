package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_ConceptosDebin")
public class ConceptoDebinOB {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;

	@Column(name = "descripcion", nullable = false)
	public String descripcion;
}
