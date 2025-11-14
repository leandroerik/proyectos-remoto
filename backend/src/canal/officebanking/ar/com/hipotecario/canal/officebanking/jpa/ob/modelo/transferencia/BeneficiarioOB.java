package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(schema = "dbo", name = "OB_Beneficiarios", uniqueConstraints = { @UniqueConstraint(columnNames = { "cbu", "emp_codigo" }) })
public class BeneficiarioOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;

	@Column(name = "cbu", nullable = false, length = 22)
	public String cbu;

	@Column(name = "alias", length = 20)
	public String alias;

	@Column(name = "nombre")
	public String nombre;

	@Column(name = "emp_cuit", nullable = false, columnDefinition = "NUMERIC(11,0)")
	public Long cuit;

	@Column(name = "email")
	public String email;

	@Column(name = "referencia")
	public String referencia;

	@Column(name = "banco_destino", nullable = false)
	public String bancoDestino;

	@Enumerated(EnumType.STRING)
	public TipoCuentaOB tipoCuenta;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(schema = "dbo", name = "OB_Monedas_Beneficiario", uniqueConstraints = @UniqueConstraint(columnNames = { "id_beneficiario", "id_moneda" }), joinColumns = { @JoinColumn(name = "id_beneficiario") }, inverseJoinColumns = { @JoinColumn(name = "id_moneda") })
	public List<MonedaOB> monedas;

	@ManyToOne()
	@JoinColumn(name = "emp_codigo", nullable = false)
	public EmpresaOB empresa;

	@ManyToOne()
	@JoinColumn(name = "tipo_beneficiario", nullable = false)
	public TipoBeneficiarioOB tipo;

	@Column(name = "fecha_creacion", nullable = false)
	public LocalDateTime fechaCreacion;

	@Column(name = "ultima_modificacion")
	public LocalDateTime ultimaModificacion;

	public BeneficiarioOB() {
		this.monedas = new ArrayList<MonedaOB>();
		this.fechaCreacion = LocalDateTime.now();
	}

}