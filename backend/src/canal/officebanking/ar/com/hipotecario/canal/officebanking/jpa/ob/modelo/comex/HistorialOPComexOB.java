package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Historial_OP_Comex")
public class HistorialOPComexOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	public Integer id;
	
	@Column(name = "nro_cuenta_credito_pesos", nullable = false)
	public String nroCuentaCreditoPesos;

	@Column(name = "nro_cuenta_cred_moneda_ext", nullable = false)
	public String nroCuentaCredMonedaExt;
	
	@Column(name="monto", nullable = false)
	public BigDecimal monto;
	
	@ManyToOne()
	@JoinColumn(name = "moneda", nullable = false)
	public MonedaOB moneda;
	
	@Column(name = "monto_moneda_ext", nullable = false)
	public BigDecimal montoMonedaExt;
	
	@ManyToOne()
	@JoinColumn(name = "simbolo_moneda_ext", nullable = false)
	public MonedaOB simboloMonedaExt;
	
	@ManyToOne()
	@JoinColumn(name = "id_accion_op", nullable = false)
	public AccionesOB accion;
	
	@Column(name = "rectificacion", nullable = false)
	public Character rectificacion;
	
	@ManyToOne()
	@JoinColumn(name = "id_usuario", nullable = false)
	public EmpresaUsuarioOB empresaUsuario;
	
	@ManyToOne()
	@JoinColumn(name = "id_est_inicial", nullable = false)
	public EstadoOPComexOB estadoInicial;
	
	@ManyToOne()
	@JoinColumn(name = "id_est_final", nullable = false)
	public EstadoOPComexOB estadoFinal;
	
	@ManyToOne()
	public OrdenPagoComexOB ordenPago;
	
	@Column(name = "fecha_creacion", nullable = false)
	public LocalDateTime fechaCreacion;
}
