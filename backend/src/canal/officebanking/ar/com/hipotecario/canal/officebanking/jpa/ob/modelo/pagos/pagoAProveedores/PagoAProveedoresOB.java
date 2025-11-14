package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores;

import java.sql.Blob;
import java.time.LocalDateTime;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.sql.Blob;
import java.time.LocalDateTime;

@Entity
@Table(schema = "dbo", name = "OB_Pago_A_Proveedores")
@NamedQueries({
        @NamedQuery(name = "PagoAProveedoresOB.filtrarMovimientosHistorial", query = "SELECT p "
                + "FROM PagoAProveedoresOB p "
                + "WHERE p.emp_codigo = :empCodigo "
                + "AND ((:fechaDesde IS NULL) OR (:fechaHasta IS NULL) OR p.fechaCreacion BETWEEN :fechaDesde AND :fechaHasta) "
                + "AND (:convenio IS NULL OR p.convenio = :convenio) "
                + "AND (:subconvenio IS NULL OR p.subconvenio = :subconvenio) "
                + "AND (:nroAdherente IS NULL OR p.nroAdherente = :nroAdherente) "
                + "AND (:estado IS NULL OR p.estado = :estado) "
                + "ORDER BY p.fechaCreacion DESC"),
        @NamedQuery(name = "PagoAProveedoresOB.buscarPorNroLoteYEmpresa", query = "SELECT p "
                + "FROM PagoAProveedoresOB p "
                + "WHERE p.emp_codigo = :empCodigo "
                + "AND p.nroLote = :nroLote "),
        @NamedQuery(name = "PagoAProveedoresOB.buscarPorEstado", query = "SELECT p FROM PagoAProveedoresOB p WHERE p.estado = :estado")
})
public class PagoAProveedoresOB extends BandejaOB {

	@ManyToOne()
	@JoinColumn(name = "estado", nullable = false)
	public EstadosPagosAProveedoresOB estado;

	@ManyToOne()
	@JoinColumn(name = "emp_codigo", nullable = false)
	public EmpresaOB emp_codigo;

	@ManyToOne()
	@JoinColumn(name = "usu_codigo")
	public UsuarioOB usuario;

	@Column(name = "fecha_creacion", nullable = false)
	public LocalDateTime fechaCreacion;

	@Column(name = "ultima_modificacion")
	public LocalDateTime ultimaModificacion;

	@Column(name = "usu_modificacion")
	public String usuarioModificacion;

	@Column(name = "nombre_archivo")
	public String nombreArchivo;

	@Lob
	@Column(name = "archivo")
	public Blob archivo;

	@Column(name = "cantidad_registros")
	public Integer cantidadRegistros;

	@Column(name = "tipo_producto")
	public String tipoProducto;

	@Column(name = "convenio")
	public Integer convenio;

    @Column(name = "subconvenio")
    public Integer subconvenio;

    @Column(name = "nroAdherente")
    public String nroAdherente;

    @Column(name = "nroLote")
    public String nroLote;

    @Column(name = "cheques")
    public BigDecimal cheques;

    @Column(name = "transferencias")
    public BigDecimal transferencias;

}
