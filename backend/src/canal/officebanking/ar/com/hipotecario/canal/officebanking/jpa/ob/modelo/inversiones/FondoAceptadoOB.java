package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones;

import java.time.LocalDateTime;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.BaseEntityOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Fondos_Aceptados")
@NamedQueries({ @NamedQuery(name = "FondoAceptadoOB.buscarPorFondoYEmpresa", query = "SELECT f FROM FondoAceptadoOB f WHERE f.parametria.id = :id AND f.version = :version AND f.empresa.emp_codigo = :empresa") })
public class FondoAceptadoOB extends BaseEntityOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Integer id;

	@ManyToOne()
	@JoinColumn(name = "id_empresa", nullable = false)
	public EmpresaOB empresa;

	@ManyToOne()
	@JoinColumn(name = "id_fondo", nullable = false)
	public ParametriaFciOB parametria;

	@Column(name = "version")
	public Integer version;

	public FondoAceptadoOB() {
		super();
		this.fechaCreacion = LocalDateTime.now();
	}
}