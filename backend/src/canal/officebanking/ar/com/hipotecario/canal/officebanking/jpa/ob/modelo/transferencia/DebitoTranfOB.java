package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Debito_Transferencia")
public class DebitoTranfOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	public Integer id;

	@Column(name = "cbu", nullable = false)
	public String cbu;

	@Column(name = "cuit", nullable = false)
	public String cuit;

	@Column(name = "descripcion", nullable = false)
	public String descripcion;

	@Enumerated(EnumType.ORDINAL)
	public TipoCuentaOB tipoCuenta;

	@Column(name = "nro_cuenta", nullable = false)
	public String nroCuenta;

	@Column(name = "denominacion")
	public String denominacion;

}