package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes;

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

import java.sql.Blob;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(schema = "dbo", name = "OB_Pago_De_Haberes")
@NamedQueries({
        @NamedQuery(name = "PagoDeHaberesOB.filtrarMovimientosHistorial", query = "SELECT p "
                + "FROM PagoDeHaberesOB p "
                + "WHERE p.emp_codigo = :empCodigo "
                + "AND (p.tipoProducto = :producto) "
                + "AND ((:fechaDesde IS NULL) OR (:fechaHasta IS NULL) OR p.fechaCreacion BETWEEN :fechaDesde AND :fechaHasta) "
                + "AND (:convenio IS NULL OR p.convenio = :convenio) "
                + "AND (:estado IS NULL OR p.estado = :estado) "
                + "ORDER BY p.fechaCreacion DESC"),
        @NamedQuery(name = "PagoDeHaberesOB.buscarAcreditacionesSinFirmaAFechaArchivo", query = "SELECT p "
                + "FROM PagoDeHaberesOB p "
                + "WHERE p.estado = :estado "
                + "AND (p.tipoProducto = :tipoProducto) "
                + "AND (p.fechaCargaArchivo <= :fechaHoy)"
        ),
        @NamedQuery(name = "PagoDeHaberesOB.buscarArchivo", query = "SELECT p "
                + "FROM PagoDeHaberesOB p "
                + "WHERE p.nombreArchivo = :archivo "
                + "AND p.estado.id <> :estado "
                + "AND p.emp_codigo.emp_codigo = :empresa"
        ),
        @NamedQuery(name = "PagoDeHaberesOB.buscarByNombre", query = "SELECT p FROM PagoDeHaberesOB p WHERE p.nombreArchivo = :archivo"),
        @NamedQuery(name = "PagoDeHaberesOB.buscarArchivoContains", query = "SELECT p "
                + "FROM PagoDeHaberesOB p "
                + "WHERE p.nombreArchivo LIKE :archivo "
                + "AND p.estado.id <> :estado "
                + "AND p.emp_codigo.emp_codigo = :empresa"
        )

})
public class PagoDeHaberesOB extends BandejaOB {

    @ManyToOne()
    @JoinColumn(name = "estado", nullable = false)
    public EstadoPagosHaberesOB estado;

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

    @Column(name = "numero_lote")
    public String numeroLote;
    @Column(name = "fecha_carga_lote")
    public LocalDateTime fechaCargaLote;

    @Column(name = "fecha_carga_archivo")
    public LocalDate fechaCargaArchivo;

    @Column(name = "fcl")
    public Boolean fcl;

}
