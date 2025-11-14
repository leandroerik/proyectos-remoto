package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
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
@Table(schema = "dbo", name = "OB_Errores_Archivos")
@NamedQueries({ @NamedQuery(name = "ErroresArchivosOB.buscarPorIdOperacion", query = "SELECT e " + "FROM ErroresArchivosOB e " + "WHERE e.operacion = :operacion") })
public class ErroresArchivosOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_error", nullable = false)
	public Integer id;

	@ManyToOne()
	@JoinColumn(name = "id")
	public BandejaOB operacion;

	@Column(name = "titulo")
	public String titulo;

	@Column(name = "descripcion")
	public String descripcion;

	@Column(name = "linea")
	public Integer linea;

	@Column(name = "campo")
	public String campo;
}
