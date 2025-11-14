package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_FCI")
@NamedQueries({
    @NamedQuery(name = "FondosComunesOB.filtrarMovimientosHistorial", query = "SELECT f "
            + "FROM FondosComunesOB f "
            + "WHERE f.empCodigo = :empCodigo "
            + "AND ((:fechaDesde IS NULL) OR (:fechaHasta IS NULL) OR f.fechaInicio BETWEEN :fechaDesde AND :fechaHasta) "
            + "AND (:idCuotapartista IS NULL OR f.idCuotapartista = :idCuotapartista) "
            + "AND (:tipoSolicitud IS NULL OR f.tipoSolicitud = :tipoSolicitud) "
            + "AND (:moneda IS NULL OR f.monedaId = :moneda) "
            + "ORDER BY f.fechaInicio DESC"),
    @NamedQuery(name = "FondosComunesOB.buscarSinFirmaCompletaPorVencer", query = "SELECT f FROM FondosComunesOB f WHERE f.estado.id = :estadoPendiente"),
    @NamedQuery(name = "FondosComunesOB.buscarIdSolicitud", query = "SELECT f FROM FondosComunesOB f WHERE f.idTransaccion = :idSolicitud")
})


public class FondosComunesOB extends BandejaOB {

	@ManyToOne()
	@JoinColumn(name = "id_fci", nullable = false)
	public ParametriaFciOB idFondo;

	@Column(name = "id_cuotapartista")
	public String idCuotapartista;

	@Column(name = "fecha_concertacion")
	public LocalDateTime fechaConcertacion;

	@Column(name = "fondo_abreviatura")
	public String abreviaturaFondo;

	@Column(name = "numero_fondo")
	public Integer numeroFondo;

	@Column(name = "nivel_de_riesgo")
	public String nivelDeRiesgo;

	@Column(name = "tipo_VCP_ID")
	public String tipoVCPid;

	@Column(name = "fecha_inicio")
	public LocalDateTime fechaInicio;

	@Column(name = "fecha_vencimiento")
	public LocalDateTime fechaVencimiento;

	@Column(name = "monto_intereses")
	public BigDecimal montoIntereses;

	@Column(name = "monto_comisiones")
	public BigDecimal montoComisiones;

	@ManyToOne()
	@JoinColumn(name = "usu_codigo")
	public UsuarioOB usuario;

	@ManyToOne
	@JoinColumn(name = "estado", nullable = false)
	public EstadoSolicitudInversionOB estado;

	@Column(name = "id_cuenta_bancaria")
	public String idCuentaBancaria;

	@Column(name = "tipo_solicitud")
	public String tipoSolicitud;

	@Column(name = "cantidad_cuota_partes")
	public Integer cantidadCuotaPartes;

	@Column(name = "es_total_rescate")
	public Boolean esTotal;

	@Column(name = "tipo_cuenta")
	public String tipoCuenta;

	@Column(name = "id_transaccion")
	public String idTransaccion;

	@ManyToOne
	@JoinColumn(name = "emp_codigo")
	public EmpresaOB empCodigo;

	@ManyToOne()
	@JoinColumn(name = "moneda", nullable = false)
	public MonedaOB monedaId;

	@Column(name = "plazo_liquidacion")
	public String plazoLiquidacion;
}
