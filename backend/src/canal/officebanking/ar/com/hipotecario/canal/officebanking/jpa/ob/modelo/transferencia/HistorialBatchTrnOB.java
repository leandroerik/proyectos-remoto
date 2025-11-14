package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Historial_Batch_TRN")
@NamedQueries({ @NamedQuery(name = "HistorialBatchTrnOB.buscarPorTrn", query = "SELECT h FROM HistorialBatchTrnOB h WHERE h.transferencia.id = :idTransferencia") })
public class HistorialBatchTrnOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	public Integer id;

	@ManyToOne()
	@JoinColumn(name = "id_trn", nullable = false)
	public TransferenciaOB transferencia;

	@ManyToOne()
	@JoinColumn(name = "id_est_inicial", nullable = false)
	public EstadoTRNOB estadoInicial;

	@ManyToOne()
	@JoinColumn(name = "id_est_final", nullable = false)
	public EstadoTRNOB estadoFinal;

	@ManyToOne()
	@JoinColumn(name = "id_moneda", nullable = false)
	public MonedaOB moneda;

	@Column(name = "monto", nullable = false)
	public BigDecimal monto;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "tipoCuentaOrigen", nullable = false)
	public TipoCuentaOB tipoCuentaOrigen;

	@Column(name = "cuenta_origen", nullable = false)
	public String cuentaOrigen;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "tipoCuentaDestino", nullable = false)
	public TipoCuentaOB tipoCuentaDestino;

	@Column(name = "cuenta_destino", nullable = false)
	public String cuentaDestino;

}