package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Items_Concepto_Comex")
public class ItemsConceptoOB {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	public Integer id;
	
	@Column(name = "descripcion", nullable = false)
	@Lob
	public String descripcion;
	
	public ItemsConceptoOB() {
    }
	
	public ItemsConceptoOB(int id, String descripcion) {
		this.id = id;
		this.descripcion = descripcion;
	}
}
