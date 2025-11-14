package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.controlDual;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;

@Entity
@Table(schema = "dbo", name = "OB_Control_Dual")
public class ControlDualOB {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;

	@ManyToOne()
	@JoinColumn(name = "emp_codigo", nullable = false)
	public EmpresaOB empresa;
	
	@Column(name = "emp_control_dual", nullable = false)
	public Boolean control_dual;
}
