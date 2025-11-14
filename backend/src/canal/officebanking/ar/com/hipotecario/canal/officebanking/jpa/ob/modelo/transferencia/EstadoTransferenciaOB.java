package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Estados_TRN")
public class EstadoTransferenciaOB {
	@Id
	@Column(name = "id")
	public Integer id;

	@Column(name = "desc_corta", length = 45)
	public String descripcionCorta;

	@Column(name = "desc_larga", length = 255)
	public String descripcionLarga;

}