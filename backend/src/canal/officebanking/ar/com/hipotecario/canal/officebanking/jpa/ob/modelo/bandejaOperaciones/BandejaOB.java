package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Bandeja")
@Inheritance(strategy = InheritanceType.JOINED)
@NamedQueries({ @NamedQuery(name = "BandejaOB.buscarPendientesDeFirma",
		query = "SELECT b " + "FROM BandejaOB b " + "WHERE (:emp_codigo IS NULL OR b.empresa = :emp_codigo) "
		+ "AND (b.estadoBandeja.id = :idEstado1 OR b.estadoBandeja.id = :idEstado2) "
				+ "AND (b.fechaUltActulizacion BETWEEN :fechaDesde AND :fechaHasta) "
		+ "AND (:moneda IS NULL OR b.moneda = :moneda) "
				+ "AND (:codProducto IS NULL OR b.tipoProductoFirma.codProdFirma = :codProducto) "
		+ "AND (:cuenta IS NULL OR b.cuentaOrigen = :cuenta) "
				+ "AND (:tipoSolicitud IS NULL OR b.tipoSolicitud = :tipoSolicitud) "
		+ "ORDER BY b.fechaUltActulizacion DESC"), })

public class BandejaOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	public Integer id;

	@ManyToOne()
	@JoinColumn(name = "id_tipo_producto_firma")
	public TipoProductoFirmaOB tipoProductoFirma;

	@ManyToOne
	@JoinColumn(name = "emp_codigo")
	public EmpresaOB empresa;

	@ManyToOne()
	@JoinColumn(name = "estado_bandeja")
	public EstadoBandejaOB estadoBandeja;

	@Column(name = "cuenta_origen", nullable = false)
	public String cuentaOrigen;

	@Column(name = "monto", nullable = false)
	public BigDecimal monto;

	@ManyToOne()
	@JoinColumn(name = "moneda", nullable = false)
	public MonedaOB moneda;

	@Column(name = "fecha_ult_actulizacion")
	public LocalDateTime fechaUltActulizacion;



}