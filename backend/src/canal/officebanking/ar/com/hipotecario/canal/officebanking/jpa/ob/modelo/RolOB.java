package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Roles")
public class RolOB {

	@Id
	public Integer rol_codigo;

	@Column(name = "rol_nombre", nullable = false)
	public String nombre;

	@Column(name = "rol_descripcion", nullable = false)
	public String descripcion;

}