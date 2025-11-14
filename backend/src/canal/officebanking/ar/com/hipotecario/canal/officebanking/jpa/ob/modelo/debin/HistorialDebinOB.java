package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin;

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
@Table(schema = "dbo", name = "OB_Historial_Debin")
public class HistorialDebinOB {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Integer id;

	@ManyToOne()
	@JoinColumn(name = "id_accion", nullable = false)
	public AccionesOB accion;

	@ManyToOne()
	public DebinOB debin;

	@ManyToOne()
	@JoinColumn(name = "id_usuario", nullable = false)
	public EmpresaUsuarioOB empresaUsuario;

	@ManyToOne()
	@JoinColumn(name = "id_est_inicial_enviada", nullable = false)
	public EstadoDebinEnviadasOB estadoInicialEnviada;

	@ManyToOne()
	@JoinColumn(name = "id_est_final_enviada", nullable = false)
	public EstadoDebinEnviadasOB estadoFinalEnviada;
	
	@ManyToOne()
	@JoinColumn(name = "id_est_inicial_recibida", nullable = false)
	public EstadoDebinRecibidasOB estadoInicialRecibida;

	@ManyToOne()
	@JoinColumn(name = "id_est_final_recibida", nullable = false)
	public EstadoDebinRecibidasOB estadoFinalRecibida;

	@ManyToOne()
	@JoinColumn(name = "id_moneda", nullable = false)
	public MonedaOB moneda;

	@Column(name = "monto", nullable = false)
	public BigDecimal monto;

	@Column(name = "cuenta_origen", nullable = false)
	public String cuentaOrigen;

	@Column(name = "cuenta_destino", nullable = false)
	public String cuentaDestino;

}
