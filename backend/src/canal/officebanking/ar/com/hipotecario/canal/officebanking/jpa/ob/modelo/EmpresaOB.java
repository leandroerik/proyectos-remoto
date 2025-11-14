package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Empresas")
public class EmpresaOB implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer emp_codigo;

	@Column(name = "emp_cuit", nullable = false, columnDefinition = "NUMERIC(11,0)")
	public Long cuit;

	@Column(name = "emp_idCobis", nullable = false, unique = true)
	public String idCobis;

	@Column(name = "emp_razon_social", nullable = false)
	public String razonSocial;
	
	public EmpresaOB() {
				
	}
	
	public EmpresaOB(int emp_codigo, Long cuit, String idCobis, String razonSocial) {
		this.emp_codigo = emp_codigo;
		this.cuit = cuit;
		this.idCobis = idCobis;
		this.razonSocial = razonSocial;		
	}

	public String cuitFormateado() {
		String cuit = this.cuit.toString();
		String cuitformateado = "";
		cuitformateado += cuit.substring(0, 2) + "-";
		cuitformateado += cuit.substring(2, cuit.length() - 1) + "-";
		cuitformateado += cuit.substring(cuit.length() - 1);
		return cuitformateado;
	}

}