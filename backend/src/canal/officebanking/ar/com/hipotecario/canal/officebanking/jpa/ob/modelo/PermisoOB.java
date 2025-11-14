package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import java.util.List;

import ar.com.hipotecario.backend.base.Objeto;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Permisos")
@NamedQueries({ @NamedQuery(name = "PermisoOB.permisos", query = "SELECT p FROM PermisoOB p WHERE p.padre IS NULL ORDER BY p.codigo"), @NamedQuery(name = "PermisoOB.permisosSinAsignarAEmpresa", query = "SELECT p FROM PermisoOB p " + "WHERE padre = :permiso AND p NOT IN (SELECT po.permiso FROM PermisoOperadorOB po WHERE po.empresaUsuario = :empresaUsuario)"), @NamedQuery(name = "PermisoOB.permisosAsignadoAEmpresa", query = "SELECT p FROM PermisoOB p " + "WHERE padre = :permiso AND p IN (SELECT po.permiso FROM PermisoOperadorOB po WHERE po.empresaUsuario = :empresaUsuario)")

})
public class PermisoOB {

	@Id
	@Column(name = "per_codigo", nullable = false)
	public Integer codigo;

	@Column(name = "per_nombre", nullable = false)
	public String nombre;

	@Column(name = "per_opcional", nullable = false)
	public Boolean opcional;

	@ManyToOne
	@JoinColumn(name = "per_codigo_padre", referencedColumnName = "per_codigo")
	public PermisoOB padre;

	@OneToMany(mappedBy = "padre", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	public List<PermisoOB> subpermisos;

	public Objeto objeto(boolean incluirPermisos) {
		Objeto result = new Objeto();
		result.set("codigo", this.codigo);
		result.set("nombre", this.nombre);
		boolean tienePermisos = this.subpermisos.size() > 0 ? true : false;
		if (tienePermisos && incluirPermisos) {
			Objeto objSP = result.set("permisos");
			for (PermisoOB sp : subpermisos) {
				objSP.add(new Objeto().set("codigo", sp.codigo).set("nombre", sp.nombre));
			}
		}
		return result;
	}

}