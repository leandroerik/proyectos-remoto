package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones;

import java.time.LocalDateTime;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Perfil_Inversor")
@NamedQueries({ @NamedQuery(name = "SolicitudPerfilInversorOB.buscarPorEmpresaYEstado", query = "SELECT p FROM SolicitudPerfilInversorOB p WHERE p.empCodigo =:idEmpresa AND p.estado.id = :idEstadoPendiente") })
public class SolicitudPerfilInversorOB extends BandejaOB {

	@Column(name = "id_perfil", nullable = false)
	public String idPerfil;
	@Column(name = "nombre_perfil", nullable = false)
	public String nombrePerfil;

	@ManyToOne()
	@JoinColumn(name = "id_estado", nullable = false)
	public EstadoSolicitudInversionOB estado;
	@Column(name = "ultima_modificacion", nullable = false)
	public LocalDateTime ultimaModificacion;

	@ManyToOne()
	@JoinColumn(name = "usu_codigo")
	public UsuarioOB usuario;

	@ManyToOne()
	@JoinColumn(name = "empresa", nullable = false)
	public EmpresaOB empCodigo;

}