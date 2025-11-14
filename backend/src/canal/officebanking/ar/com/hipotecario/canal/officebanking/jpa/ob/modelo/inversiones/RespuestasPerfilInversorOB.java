package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Respuestas_PI")
public class RespuestasPerfilInversorOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	public Integer id;

	@Column(name = "respuesta", nullable = false)
	public String respuesta;

	@ManyToOne()
	@JoinColumn(name = "id_pregunta", nullable = false)
	public PreguntasPerfilInversorOB pregunta;

	@Column(name = "escala", nullable = false, length = 1)
	public Integer escala;

}