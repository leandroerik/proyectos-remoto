package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Estados_Inversion")
public class EstadoSolicitudInversionOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_estado", nullable = false)
	public Integer id;

	@Column(name = "descripcion", nullable = false)
	public String descripcion;

}