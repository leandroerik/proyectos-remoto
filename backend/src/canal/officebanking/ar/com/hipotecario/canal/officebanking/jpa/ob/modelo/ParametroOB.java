package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Parametro")
public class ParametroOB {

	@Id
	@Column(name = "par_nombre", nullable = false)
	public String clave;

	@Column(name = "par_valor", nullable = false)
	public String valor;

}