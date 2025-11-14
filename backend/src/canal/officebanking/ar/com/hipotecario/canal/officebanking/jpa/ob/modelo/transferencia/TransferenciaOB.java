package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(schema = "dbo", name = "OB_Transferencias")
@NamedQueries({
		@NamedQuery(
				name = "TransferenciaOB.findByEmpresaAndCbu",
				query = "SELECT t FROM TransferenciaOB t " +
						"JOIN t.credito d " +
						"WHERE t.emp_codigo = :empresa " +
						"AND d.cbu = :cbu"
		), @NamedQuery(name = "TransferenciaOB.find", query = "SELECT t FROM TransferenciaOB t WHERE ((:emp_codigo) IS NULL OR t.emp_codigo = :emp_codigo) AND ((:beneficiario) = '' OR t.credito.titular LIKE CONCAT('%',:beneficiario,'%')) AND ((:idEstado) = 0 OR t.estado.id = :idEstado) AND (:fechaDesde IS NULL OR t.fechaEjecucion BETWEEN :fechaDesde AND :fechaHasta) ORDER BY t.fechaCreacion DESC"),
	@NamedQuery(name = "TransferenciaOB.findByState", query = "SELECT t FROM TransferenciaOB t WHERE t.estado.id = :idEstado ORDER BY t.fechaCreacion DESC"), 
	@NamedQuery(name = "TransferenciaOB.findByStateAndApplicationDate", query = "SELECT t FROM TransferenciaOB t WHERE t.estado.id = :idEstado AND t.fechaAplicacion = :fechaAplicacion ORDER BY t.fechaCreacion DESC"),
	@NamedQuery(name = "TransferenciaOB.findByTwoStatesBetweenApplicationDate", query = "SELECT t FROM TransferenciaOB t WHERE t.emp_codigo = :emp_codigo AND t.estado.id = :idEstado1 OR t.estado.id = :idEstado2 AND t.fechaAplicacion BETWEEN :fechaDesde AND :fechaHasta ORDER BY t.fechaCreacion DESC"), @NamedQuery(name = "TransferenciaOB.buscarPorEstado", query = "SELECT t FROM TransferenciaOB t WHERE t.emp_codigo = :emp_codigo AND t.estado.id = :idEstado ORDER BY t.fechaCreacion DESC"),
	@NamedQuery(name = "TransferenciaOB.buscarSinFirmaPorVencer", query = "SELECT t FROM TransferenciaOB t WHERE t.estado.id = :estadoEnBandeja AND t.fechaAplicacion <= :fechaHoy") ,
		@NamedQuery(
				name = "TransferenciaOB.findTodayByEmpresaAndEstadoBandejaNotEqual",
				query = "SELECT t FROM TransferenciaOB t " +
						"WHERE t.emp_codigo.id = :empCodigo " +
						"AND CONVERT(date, t.fechaCreacion) = CONVERT(date, GETDATE()) " +
						"AND t.estadoBandeja.id <> 4"
		)})
public class TransferenciaOB extends BandejaOB {

	@ManyToOne()
	@JoinColumn(name = "emp_codigo", nullable = false)
	public EmpresaOB emp_codigo;

	@ManyToOne()
	@JoinColumn(name = "estado", nullable = false)
	public EstadoTRNOB estado;

	@ManyToOne()
	@JoinColumn(name = "tipo_t_cod", nullable = false)
	public TipoTransferenciaOB tipo;

	@ManyToOne()
	@JoinColumn(name = "concepto_codigo", nullable = false)
	public ConceptoOB concepto;

	@ManyToOne()
	@JoinColumn(name = "camara_codigo", nullable = false)
	public TipoCamaraOB camara;

	@Column(name = "fecha_aplicacion")
	public LocalDate fechaAplicacion;

	@Column(name = "debito_consolidado")
	public Boolean debitoConsolidado;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "id_carga", nullable = false)
	public SecuenciaOB idDeCarga;

	@ManyToOne()
	@JoinColumn(name = "usu_codigo")
	public UsuarioOB usuario;

	@Column(name = "fecha_creacion", nullable = false)
	public LocalDateTime fechaCreacion;

	@Column(name = "ultima_modificacion")
	public LocalDateTime ultimaModificacion;

	@Column(name = "usu_modificacion")
	public String usuarioModificacion;

	@Column(name = "cuenta_sueldo", nullable = false)
	public Boolean cuentaSueldo;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "tr_debito", nullable = false)
	public DebitoTranfOB debito;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "tr_credito", nullable = false)
	public CreditoTranfOB credito;

	@Column(name = "comentario")
	public String comentario;

	@Column(name = "fecha_ejecucion")
	public LocalDateTime fechaEjecucion;
}