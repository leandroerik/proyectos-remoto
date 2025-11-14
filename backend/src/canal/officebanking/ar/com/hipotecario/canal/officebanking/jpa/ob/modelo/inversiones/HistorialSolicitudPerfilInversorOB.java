package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Historial_Solicitud_Perfil_Inversor")
public class HistorialSolicitudPerfilInversorOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Integer id;

	@ManyToOne()
	@JoinColumn(name = "id_accion", nullable = false)
	public AccionesOB accion;

	@ManyToOne()
	public SolicitudPerfilInversorOB perfilInversor;

	@ManyToOne()
	@JoinColumn(name = "id_usuario", nullable = false)
	public EmpresaUsuarioOB empresaUsuario;

	@ManyToOne()
	@JoinColumn(name = "id_est_inicial", nullable = false)
	public EstadoSolicitudInversionOB estadoInicial;

	@ManyToOne()
	@JoinColumn(name = "id_est_final", nullable = false)
	public EstadoSolicitudInversionOB estadoFinal;
}