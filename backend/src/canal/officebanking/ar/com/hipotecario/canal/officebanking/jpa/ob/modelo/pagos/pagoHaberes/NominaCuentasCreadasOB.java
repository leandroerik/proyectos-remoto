package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes;

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
@Table(schema = "dbo", name = "OB_Nomina_Cuentas_Creadas")
@NamedQueries({ @NamedQuery(name = "NominaCuentasCreadasOB.buscarPorIdOperacion", query = "SELECT c " + "FROM NominaCuentasCreadasOB c " + "WHERE c.nomina = :nomina") })
public class NominaCuentasCreadasOB {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id_proceso_cuenta", nullable = false)
	public Integer id;

	@ManyToOne()
	@JoinColumn(name = "id")
	public PagoDeHaberesOB nomina;

	@Column(name = "linea")
	public Integer linea;

	@Column(name = "cuenta_sueldo_creada")
	public String cuentaSueldo;

	@Column(name = "cbu_cuenta_sueldo_creada")
	public String cbuCuentaSueldo;

	@Column(name = "cuenta_fcl_creada")
	public String cuentaFCL;

	@Column(name = "cbu_cuenta_fcl_creada")
	public String cbuCuentaFCL;
}
