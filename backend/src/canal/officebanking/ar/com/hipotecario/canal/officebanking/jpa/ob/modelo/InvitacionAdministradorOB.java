package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import java.time.LocalDateTime;

import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoInvitacionOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Invitaciones_Administrador")
public class InvitacionAdministradorOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;

	@Column(name = "usu_nombre", nullable = false)
	public String usu_nombre;

	@Column(name = "usu_apellido", nullable = false)
	public String usu_apellido;
	
	@Column(name = "usu_nro_documento", nullable = false, columnDefinition = "NUMERIC(8,0)")
	public Long usu_nro_documento;
	
	@Column(name = "usu_cuil", nullable = false, columnDefinition = "NUMERIC(11,0)")
	public Long usu_cuil;
	
	@Column(name = "usu_correo", nullable = false)
	public String usu_correo;
	
	@Column(name = "usu_telefono_movil", nullable = false)
	public String usu_telefono_movil;
	
//	@Column(name = "usu_idCobis", length = 20)
//	public String usu_idCobis;
	@Column(name = "usu_idCobis", length = 20)
	public String usu_idCobis;
		
	@ManyToOne()
	@JoinColumn(name = "emp_codigo", nullable = false)
	public EmpresaOB empresa;

	@Column(name = "fecha_creacion", nullable = false)
	public LocalDateTime fechaCreacion;

	@Enumerated(EnumType.ORDINAL)
	public EnumEstadoInvitacionOB estado;

	public Boolean enviada() {
		return this.estado.equals(EnumEstadoInvitacionOB.ENVIADA) || this.estado.equals(EnumEstadoInvitacionOB.REENVIADA) || this.estado.equals(EnumEstadoInvitacionOB.TRANSMIT);
	}

	public InvitacionAdministradorOB() {
		this.fechaCreacion = LocalDateTime.now();
		this.estado = EnumEstadoInvitacionOB.ENVIADA;
	}

}