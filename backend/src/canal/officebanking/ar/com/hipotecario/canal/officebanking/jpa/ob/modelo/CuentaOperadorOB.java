package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import java.time.LocalDateTime;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(schema = "dbo", name = "OB_Cuentas_Operador", uniqueConstraints = { @UniqueConstraint(columnNames = { "ope_codigo", "numero_cuenta" }) })
@NamedQueries({ @NamedQuery(name = "CuentaOperadorOB.delete", query = "DELETE CuentaOperadorOB where id= :idCuentaOperador") })
public class CuentaOperadorOB extends BaseEntityOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;

	@ManyToOne()
	@JoinColumn(name = "ope_codigo", nullable = false)
	public EmpresaUsuarioOB empresaUsuario;

	@Column(name = "numero_cuenta", nullable = false, length = 32)
	public String numeroCuenta;

	public CuentaOperadorOB() {
		super();
		this.fechaCreacion = LocalDateTime.now();
	}

}