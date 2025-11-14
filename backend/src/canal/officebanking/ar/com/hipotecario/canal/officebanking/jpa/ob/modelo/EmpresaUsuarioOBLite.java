package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema = "dbo", name = "OB_Usuarios_Empresas", uniqueConstraints = { @UniqueConstraint(columnNames = { "usu_codigo", "emp_codigo" }) })
@NamedQueries({
		@NamedQuery(name = "EmpresaUsuarioLite", query = "SELECT emp FROM EmpresaUsuarioOBLite emp WHERE emp.usuario = :usuario AND emp.empresa = :empresa"),
		@NamedQuery(name = "EmpresaUsuarioActivoLite", query = "SELECT emp FROM EmpresaUsuarioOBLite emp WHERE emp.usuario = :usuario AND emp.empresa = :empresa"),
		@NamedQuery(name = "EmpresaRolLite", query = "SELECT emp FROM EmpresaUsuarioOBLite emp WHERE emp.empresa = :empresa AND emp.rol = :rol AND emp.usuario.estado = :estado" ),
		@NamedQuery(name = "EmpresaHabilitadosRolLite", query = "SELECT emp FROM EmpresaUsuarioOBLite emp WHERE emp.rol = :rol AND emp.usuario.estado = :estado" ),
		@NamedQuery(name = "UsuariosEmpresaLite", query = "SELECT emp FROM EmpresaUsuarioOBLite emp WHERE emp.empresa = :empresa"),
		@NamedQuery(name = "UsuariosPorIdCobisEmpresaLite", query = "SELECT emp FROM EmpresaUsuarioOBLite emp WHERE emp.empresa.idCobis = :idCobisEmpresa"),
		@NamedQuery(name = "updateRolLite", query = "update  EmpresaUsuarioOBLite emp set emp.rol.rol_codigo = :rol WHERE emp.empresa = :empresa")
})
public class EmpresaUsuarioOBLite extends BaseEntityOB {

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

}