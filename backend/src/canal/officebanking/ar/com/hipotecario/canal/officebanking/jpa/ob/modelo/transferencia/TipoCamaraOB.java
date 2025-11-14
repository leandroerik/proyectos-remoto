package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Camaras")
public class TipoCamaraOB {

	@Id
	@Column(name = "id")
	public Integer id;

	@Column(name = "descripcion", nullable = false)
	public String descripcion;

}