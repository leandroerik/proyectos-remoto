
package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Usuarios")
public class UsuarioOB implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "usu_codigo", nullable = false)
	public Integer codigo;

	@Column(name = "usu_nro_documento", nullable = false, unique = true, columnDefinition = "NUMERIC(8,0)")
	public Long numeroDocumento;

	@Column(name = "usu_cuil", nullable = false, unique = false, columnDefinition = "NUMERIC(11,0)")
	public Long cuil;

	@Column(name = "usu_nombre", nullable = false)
	public String nombre;

	@Column(name = "usu_apellido", nullable = false)
	public String apellido;

	@Column(name = "usu_login", nullable = false, length = 128)
	public String login;

	@Column(name = "usu_idCobis", length = 20)
	public String idCobis;

	@Column(name = "usu_email", nullable = false, unique = true)
	public String email;

	@Column(name = "usu_fecha_creacion", nullable = false)
	public LocalDateTime fechaCreacion;

	@ManyToOne()
	@JoinColumn(name = "esu_codigo", nullable = false)
	public EstadoUsuarioOB estado;

	@Column(name = "usu_ultimo_acceso")
	public LocalDateTime ultimoAcceso;

	@Column(name = "usu_acceso_fecha")
	public LocalDateTime accesoFecha;

	@Column(name = "usu_intentos", nullable = false)
	public Short intentos;

	@Column(name = "usu_telefono_laboral", length = 32)
	public String telefonoLaboral;

	@Column(name = "usu_telefono_movil", length = 32)
	public String telefonoMovil;

	@Column(name = "usu_compania_movil", length = 20)
	public String companiaMovil;

	@Column(name = "valido_otp", nullable = false)
	public Boolean validoOTP;

	@Column(name = "usu_email_validado", nullable = false)
	public Boolean emailValidado;

	@Column(name = "usu_adherido_gire")
	public Boolean adheridoGire;

	@Column(name = "usu_softToken_activo", nullable = false)
	public Boolean softTokenActivo;

	@Column(name = "usu_intentos_dni", nullable = false)
	public Short intentosDni;

	@Column(name = "migrado")
	public Byte migrado = 0;
	
	public UsuarioOB() {
		this.intentos = 0;
		this.intentosDni = 0;
		this.telefonoLaboral = "";
		this.ultimoAcceso = null;
		this.validoOTP = false;
		this.fechaCreacion = LocalDateTime.now();
		this.softTokenActivo = false;
		this.companiaMovil = null;
		this.adheridoGire = false;
	}
	
	public UsuarioOB(Integer codigo, Long numeroDocumento, Long cuil, String nombre, String apellido, String login, String idCobis, String email, LocalDateTime fechaCreacion, EstadoUsuarioOB estado, LocalDateTime ultimoAcceso, LocalDateTime accesoFecha, Short intentos, String telefonoLaboral,String telefonoMovil, String companiaMovil, Boolean validoOTP, Boolean emailValidado, Boolean adheridoGire, Boolean softTokenActivo, Short intentosDni) {
		this.codigo = codigo;
		this.numeroDocumento = numeroDocumento;
		this.cuil = cuil;
		this.nombre = nombre;
		this.apellido = apellido;
		this.login = login;
		this.idCobis = idCobis;
		this.email = email;
		this.fechaCreacion = fechaCreacion;
		this.estado = estado;
		this.ultimoAcceso = ultimoAcceso;
		this.accesoFecha = accesoFecha;
		this.intentos = intentos;
		this.telefonoLaboral = telefonoLaboral;
		this.telefonoMovil = telefonoMovil;
		this.companiaMovil = companiaMovil;
		this.validoOTP = validoOTP;
		this.emailValidado = emailValidado;
		this.adheridoGire = adheridoGire;
		this.softTokenActivo = softTokenActivo;
		this.intentosDni = intentosDni;		
	}

	public String nombreCompleto() {
		return nombre + " " + apellido;
	}

}