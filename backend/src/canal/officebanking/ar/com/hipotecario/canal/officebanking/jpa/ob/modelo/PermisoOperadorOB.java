package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(schema = "dbo", name = "OB_Permisos_Operador", uniqueConstraints = { @UniqueConstraint(columnNames = { "ope_codigo", "per_codigo" }) })
@NamedQueries({
    @NamedQuery(name = "PermisoOperadorOB.findByEmpresaPermiso", query = "SELECT p FROM PermisoOperadorOB p WHERE p.empresaUsuario = :empresaUsuario AND p.permiso = :permiso"),
    @NamedQuery(name = "PermisoOperadorOB.deletePermiso",query = "DELETE PermisoOperadorOB where id= :idPermisoOperador")
})
public class PermisoOperadorOB extends BaseEntityOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;

	@ManyToOne()
	@JoinColumn(name = "ope_codigo", nullable = false)
	public EmpresaUsuarioOB empresaUsuario;

	@ManyToOne()
	@JoinColumn(name = "per_codigo", nullable = false)
	public PermisoOB permiso;

	public PermisoOperadorOB() {
		super();
		this.fechaCreacion = LocalDateTime.now();
	}
}