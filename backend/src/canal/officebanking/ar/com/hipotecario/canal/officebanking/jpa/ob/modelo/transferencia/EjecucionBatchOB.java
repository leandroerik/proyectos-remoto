package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Batch")
@NamedQueries({ @NamedQuery(name = "EjecucionBatchOB.buscaPorCron", query = "SELECT b FROM EjecucionBatchOB b WHERE b.cron = :cron AND b.fechaEjecucion = :fecha_ejecucion ") })
public class EjecucionBatchOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Integer id;

	@Column(name = "fecha_ejecucion", nullable = false)
	public LocalDate fechaEjecucion;

	@Column(name = "cron", nullable = false, length = 50)
	public String cron;

	@Column(name = "row_num_ult_novedad", nullable = false)
	public Integer ultimaNovedad;

}