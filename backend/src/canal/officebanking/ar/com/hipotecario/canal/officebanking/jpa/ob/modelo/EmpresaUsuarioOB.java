package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema = "dbo", name = "OB_Usuarios_Empresas", uniqueConstraints = { @UniqueConstraint(columnNames = { "usu_codigo", "emp_codigo" }) })
@NamedQueries({
    @NamedQuery(name = "EmpresaUsuario", query = "SELECT emp FROM EmpresaUsuarioOB emp WHERE emp.usuario = :usuario AND emp.empresa = :empresa"),
    @NamedQuery(name = "EmpresaUsuarioActivo", query = "SELECT emp FROM EmpresaUsuarioOB emp WHERE emp.usuario = :usuario AND emp.empresa = :empresa"),
    @NamedQuery(name = "EmpresaRol", query = "SELECT emp FROM EmpresaUsuarioOB emp WHERE emp.empresa = :empresa AND emp.rol = :rol AND emp.usuario.estado = :estado" ),
    @NamedQuery(name = "EmpresaHabilitadosRol", query = "SELECT emp FROM EmpresaUsuarioOB emp WHERE emp.rol = :rol AND emp.usuario.estado = :estado" ),
    @NamedQuery(name = "UsuariosEmpresa", query = "SELECT emp FROM EmpresaUsuarioOB emp WHERE emp.empresa = :empresa"),
	@NamedQuery(name = "UsuariosPorIdCobisEmpresa", query = "SELECT emp FROM EmpresaUsuarioOB emp WHERE emp.empresa.idCobis = :idCobisEmpresa"),
	@NamedQuery(name = "updateRol", query = "update  EmpresaUsuarioOB emp set emp.rol.rol_codigo = :rol WHERE emp.empresa = :empresa")
})
public class EmpresaUsuarioOB extends BaseEntityOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Integer id;

	@ManyToOne()
	@JoinColumn(name = "usu_codigo", nullable = false)
	public UsuarioOB usuario;

	@ManyToOne()
	@JoinColumn(name = "emp_codigo", nullable = false)
	public EmpresaOB empresa;

	@ManyToOne()
	@JoinColumn(name = "rol_codigo", nullable = false)
	public RolOB rol;
	
	@OneToMany(cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "empresaUsuario")
	public List<CuentaOperadorOB> cuentas;

	@OneToMany(cascade = CascadeType.MERGE, orphanRemoval = true, fetch = FetchType.EAGER, mappedBy = "empresaUsuario")
	@Fetch(value = FetchMode.SUBSELECT)
	public List<PermisoOperadorOB> permisos;

	public EmpresaUsuarioOB() {
		super();
		this.fechaCreacion = LocalDateTime.now();
		this.cuentas = new ArrayList<>();
		this.permisos = new ArrayList<>();
	}
}