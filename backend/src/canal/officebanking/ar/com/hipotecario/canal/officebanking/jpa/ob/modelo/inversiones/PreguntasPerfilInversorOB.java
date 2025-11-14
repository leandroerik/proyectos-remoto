package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Preguntas_PI")
public class PreguntasPerfilInversorOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	public Integer id;

	@Column(name = "pregunta", nullable = false)
	public String pregunta;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "pregunta")
	public List<RespuestasPerfilInversorOB> respuestas;

}