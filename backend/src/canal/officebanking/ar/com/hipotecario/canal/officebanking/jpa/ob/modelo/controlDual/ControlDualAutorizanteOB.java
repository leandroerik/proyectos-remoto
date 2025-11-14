package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.controlDual;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Control_Dual_Autorizante")
@NamedQueries({
     @NamedQuery(name = "AutorizantesActivos", query = "SELECT c FROM ControlDualAutorizanteOB c WHERE c.empresa = :empresa and autorizante=true"),
     @NamedQuery(name = "AutorizantePorEmpresa", query = "SELECT c FROM ControlDualAutorizanteOB c WHERE c.empresa = :empresa and c.usuario = :usuario and autorizante=true")
})
public class ControlDualAutorizanteOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;

	@ManyToOne()
	@JoinColumn(name = "usu_codigo", nullable = false)
	public UsuarioOB usuario;
	
	@ManyToOne()
	@JoinColumn(name = "emp_codigo", nullable = false)
	public EmpresaOB empresa;
	
	@Column(name = "autorizante", nullable = false)
	public Boolean autorizante;
}
