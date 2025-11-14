package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagosVep;

import java.time.LocalDate;
import java.time.LocalDateTime;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.EstadoPagoOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Pagos_Vep")
@NamedQueries({ @NamedQuery(name = "PagosVepOB.buscarPorNroVep", query = "SELECT p FROM PagosVepOB p WHERE p.numeroVep = :numeroVep"), 
@NamedQuery(name = "PagosVepOB.buscarPorEmpresaYFiltros", query = "SELECT p FROM PagosVepOB p WHERE p.emp_codigo = :emp_codigo AND (p.estado.id = :estado) AND ((:fechaDesde IS NULL) OR (:fechaHasta IS NULL) OR p.fechaPago BETWEEN :fechaDesde AND :fechaHasta) AND (:numeroVep IS NULL OR p.numeroVep = :numeroVep) ORDER BY p.fechaPago DESC"),
@NamedQuery(name = "PagosVepOB.buscarPorEmpresaYEnte", query = ""
		+ "SELECT p FROM PagosVepOB p "
		+ "WHERE p.emp_codigo = :emp_codigo "
	    + "AND (:ente IS NULL OR p.descripcion = :ente) "
		+ "AND ((:fechaDesde IS NULL) OR (:fechaHasta IS NULL) OR p.fechaCreacion BETWEEN :fechaDesde AND :fechaHasta) "
		+ "ORDER BY p.fechaCreacion DESC"), 
@NamedQuery(name = "PagosVepOB.buscarPorEstado", query = "SELECT p FROM PagosVepOB p WHERE p.estado = :estado")

})
public class PagosVepOB extends BandejaOB {

	@Column(name = "id_tributario_cliente")
	public String idTributarioCliente;

	@Column(name = "id_tributario_empresa")
	public String idTributarioEmpresa;

	@Column(name = "id_tributario_contribuyente")
	public String idTributarioContribuyente;

	@Column(name = "id_tributario_originante")
	public String idTributarioOriginante;

	@Lob
	@Column(name = "token")
	public String token;

	@Column(name = "tipo_cuenta")
	public String tipoProducto;

	@Column(name = "tarjeta")
	public String numeroTarjeta;

	@Column(name = "numero_vep")
	public String numeroVep;

	@ManyToOne()
	@JoinColumn(name = "usu_codigo")
	public UsuarioOB usuario;

	@ManyToOne
	@JoinColumn(name = "emp_codigo")
	public EmpresaOB emp_codigo;

	@Column(name = "fecha_creacion", nullable = false)
	public LocalDateTime fechaCreacion;

	@Column(name = "ultima_modificacion")
	public LocalDateTime ultimaModificacion;

	@Column(name = "fecha_vencimiento", nullable = false)
	public LocalDate fechaVencimiento;

	@ManyToOne()
	@JoinColumn(name = "estado", nullable = false)
	public EstadoPagoOB estado;

	@Column(name = "descripcion", nullable = false)
	public String descripcion;

	@Column(name = "fecha_pago")
	public LocalDateTime fechaPago;

	@Column(name = "tipo_consulta_link")
	public String tipoConsultaLink;
}
