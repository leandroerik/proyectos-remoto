package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores;

import java.math.BigDecimal;

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
@Table(schema = "dbo", name = "OB_Historial_Pago_A_Proveedores")
public class HistorialPagoAProveedoresOB {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Integer id;

	@ManyToOne()
	@JoinColumn(name = "id_accion_trn", nullable = false)
	public AccionesOB accion;

	@ManyToOne()
	public PagoAProveedoresOB pap;

	@ManyToOne()
	@JoinColumn(name = "id_usuario", nullable = false)
	public EmpresaUsuarioOB empresaUsuario;

	@ManyToOne()
	@JoinColumn(name = "id_est_inicial", nullable = false)
	public EstadosPagosAProveedoresOB estadoInicial;

	@ManyToOne()
	@JoinColumn(name = "id_est_final", nullable = false)
	public EstadosPagosAProveedoresOB estadoFinal;

	@Column(name = "monto", nullable = false)
	public BigDecimal monto;

	@ManyToOne()
	@JoinColumn(name = "id_moneda", nullable = false)
	public MonedaOB moneda;

	@Column(name = "cuenta_origen", nullable = false)
	public String cuentaOrigen;

	@Column(name = "tipo_producto")
	public String tipoProducto;
}
