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
@Table(schema = "dbo", name = "OB_Invitaciones")
public class InvitacionOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;

	@Column(name = "usu_nro_documento", nullable = false, columnDefinition = "NUMERIC(8,0)")
	public Long numeroDocumento;

	@ManyToOne()
	@JoinColumn(name = "emp_codigo", nullable = false)
	public EmpresaOB empresa;

	@Column(name = "nombre", nullable = false)
	public String nombre;

	@Column(name = "apellido", nullable = false)
	public String apellido;

	@Column(name = "correo", nullable = false)
	public String correo;

	@Column(name = "token", nullable = false)
	public String token;

	@Column(name = "fecha_creacion", nullable = false)
	public LocalDateTime fechaCreacion;

	@Enumerated(EnumType.ORDINAL)
	public EnumEstadoInvitacionOB estado;

	@Column(name = "intentos", nullable = false)
	public Short intentos;

	public Boolean enviada() {
		return this.estado.equals(EnumEstadoInvitacionOB.ENVIADA) || this.estado.equals(EnumEstadoInvitacionOB.REENVIADA) || this.estado.equals(EnumEstadoInvitacionOB.TRANSMIT);
	}

	public InvitacionOB() {
		this.fechaCreacion = LocalDateTime.now();
		this.estado = EnumEstadoInvitacionOB.ENVIADA;
		this.intentos = 0;
	}

}