package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia;

import java.sql.Time;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Horarios_Camara")
public class HorarioCamaraOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;

	@Column(name = "hora_inicio", nullable = false)
	public Time horaInicio;

	@Column(name = "hora_limite", nullable = false)
	public Time horaLimite;

	@ManyToOne()
	@JoinColumn(name = "camara_codigo", nullable = false)
	public TipoCamaraOB tipoCamara;

	@ManyToOne()
	@JoinColumn(name = "transferencia_codigo", nullable = false)
	public TipoTransferenciaOB tipoTransferencia;

}