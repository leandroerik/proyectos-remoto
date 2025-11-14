package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Estados_DebinRecibidas")
public class EstadoDebinRecibidasOB {
	@Id
	@Column(name = "id")
	public Integer id;

	@Column(name = "descripcion", length = 255)
	public String descripcion;
}
