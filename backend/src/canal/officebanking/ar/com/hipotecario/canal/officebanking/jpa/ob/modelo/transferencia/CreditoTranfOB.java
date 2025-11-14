package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Credito_Transferencia")
public class CreditoTranfOB {

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

	@Column(name = "email")
	public String email;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "tipoCuenta", nullable = false)
	public TipoCuentaOB tipoCuenta;

	@Column(name = "nro_cuenta", nullable = false)
	public String nroCuenta;

	@ManyToOne()
	@JoinColumn(name = "codigo_banco", nullable = false)
	public BancoOB banco;

	@Column(name = "titular", nullable = false)
	public String titular;

	@Column(name = "validado_xruc")
	public Boolean validadoXRUC;

	@Column(name = "comentario", nullable = false)
	public String comentario;

}