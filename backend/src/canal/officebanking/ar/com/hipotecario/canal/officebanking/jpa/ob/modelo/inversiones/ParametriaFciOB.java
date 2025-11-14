package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Parametria_FCI")
@NamedQueries({ @NamedQuery(name = "ParametriaFciOB.buscarPorFondo", query = "SELECT f FROM ParametriaFciOB f WHERE f.idFondo = :idFondo AND f.tipoVcpDescripcion = :tipoVcpDescripcion AND f.tipoSolicitud = :tipoSolicitud") })
public class ParametriaFciOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	public Integer id;

	@Column(name = "codigo_fci", nullable = false)
	public Integer codigo;

	@Column(name = "id_fondo", nullable = false)
	public Integer idFondo;

	@Column(name = "fondo_nombre", nullable = false)
	public String fondoNombre;

	@Column(name = "tipo_vcp_descripcion", nullable = false)
	public String tipoVcpDescripcion;

	@Column(name = "tipo_vcp_abreviatura", nullable = false)
	public String tipoVcpAbreviatura;

	@Column(name = "criterio_clase", nullable = false)
	public String criterioClase;

	@Column(name = "moneda_descripcion", nullable = false)
	public String monedaDescripcion;

	@Column(name = "tipo_solicitud", nullable = false)
	public String tipoSolicitud;

	@Column(name = "hora_inicio", nullable = false)
	public String horaInicio;

	@Column(name = "hora_fin", nullable = false)
	public String horaFin;

	@Column(name = "habilitado_canales", nullable = false)
	public Boolean habilitadoCanales;

	@Column(name = "min_a_operar")
	public BigDecimal minAoperar;

	@Column(name = "max_a_operar")
	public BigDecimal maxAoperar;

	@Column(name = "cond_ingreso_egreso")
	public String condIngresoEgreso;

	@Column(name = "descripcion_larga", length = 1000)
	public String descLarga;
}