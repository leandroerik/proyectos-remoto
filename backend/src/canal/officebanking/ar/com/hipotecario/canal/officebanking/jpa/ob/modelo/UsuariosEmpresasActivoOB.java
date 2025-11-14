package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Usuarios_Empresas_Activo")
@NamedQueries({
    @NamedQuery(name = "UsuariosEmpresasActivo", query = "SELECT uea FROM UsuariosEmpresasActivoOB uea WHERE empresaUsuario.empresa = :empresa and empresaUsuario.usuario =: usuario and empresaUsuario.rol.rol_codigo=2")
})

public class UsuariosEmpresasActivoOB extends BaseEntityOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Integer id;
	
	@OneToOne
	@JoinColumn(name = "id_empresa_usuario", nullable = false)
	public EmpresaUsuarioOB empresaUsuario;
	
	@Column(name = "activo", nullable = false)
	public Boolean activo;
}