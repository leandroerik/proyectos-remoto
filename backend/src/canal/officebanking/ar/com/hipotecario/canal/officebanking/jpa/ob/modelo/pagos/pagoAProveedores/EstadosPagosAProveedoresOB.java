package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Estados_Pagos_A_Proveedores")
public class EstadosPagosAProveedoresOB {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	public Integer id;

	@Column(name = "descripcion", nullable = false)
	public String descripcion;
}
