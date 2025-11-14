package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones;

import java.time.LocalDateTime;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
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
@Table(schema = "dbo", name = "OB_Bandeja_Acciones")
@NamedQueries({ 
		@NamedQuery(name = "BandejaAccionesOB.buscarPorIdEmpresaUsuarioYAccion", query = "SELECT b FROM BandejaAccionesOB b WHERE b.empresaUsuario.id = :idEmpresaUsuario AND (b.bandeja.id = :idBandeja) AND (:accion IS NULL OR b.accion.id = :accion)"),
		@NamedQuery(name = "BandejaAccionesOB.buscarPorIdBandejaYAccion", query = "SELECT b FROM BandejaAccionesOB b WHERE (b.bandeja.id = :idBandeja) AND (:accion IS NULL OR b.accion.id = :accion)"),
		@NamedQuery(name = "BandejaAccionesOB.buscarPorBandeja", query = "SELECT b FROM BandejaAccionesOB b WHERE b.bandeja.id = :idBandeja "), })
public class BandejaAccionesOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	public Integer id;

	@ManyToOne()
	@JoinColumn(name = "id_bandeja")
	public BandejaOB bandeja;

	@ManyToOne()
	@JoinColumn(name = "idEmpresaUsuario", nullable = false)
	public EmpresaUsuarioOB empresaUsuario;

	@Column(name = "fecha_creacion", nullable = false)
	public LocalDateTime fechaCreacion;

	@ManyToOne()
	@JoinColumn(name = "accion", nullable = false)
	public AccionesOB accion;

	@ManyToOne()
	@JoinColumn(name = "id_estado_inicial", nullable = false)
	public EstadoBandejaOB estadoInicial;

	@ManyToOne()
	@JoinColumn(name = "id_estado_final", nullable = false)
	public EstadoBandejaOB estadoFinal;
}
