package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Embeddable
public class EmpresaOBId implements Serializable {

	private static final long serialVersionUID = 1L;

	@ManyToOne()
	@JoinColumn(name = "usu_codigo", nullable = false)
	public UsuarioOB usuario;

	@ManyToOne()
	@JoinColumn(name = "emp_codigo", nullable = false)
	public EmpresaOB empresa;

}