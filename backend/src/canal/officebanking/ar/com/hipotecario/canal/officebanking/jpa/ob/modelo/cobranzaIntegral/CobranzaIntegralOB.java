package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import jakarta.persistence.*;

import java.sql.Blob;
import java.time.LocalDateTime;

@Entity
@Table(schema = "dbo",name = "OB_Cobranza_Integral")
@NamedQueries({
        @NamedQuery(name = "CobranzaIntegralOB.filtrarMovimientosHistorial", query = "SELECT d "
                + "FROM CobranzaIntegralOB d "
                + "WHERE d.emp_codigo = :empCodigo "
                + "AND ((:fechaDesde IS NULL) OR (:fechaHasta IS NULL) OR d.fechaCreacion BETWEEN :fechaDesde AND :fechaHasta) "
                + "AND (:convenio IS NULL OR d.convenio = :convenio) "
                + "AND (:estado IS NULL OR d.estado = :estado) "
                + "ORDER BY d.fechaCreacion DESC"),
        @NamedQuery(name = "CobranzaIntegralOB.buscarPorEstado",query = "SELECT d " +
                "FROM CobranzaIntegralOB d " +
                "WHERE d.estado = :estado"),
        @NamedQuery(name = "CobranzaIntegralOB.buscarPorFechaHora", query = "SELECT c FROM CobranzaIntegralOB c WHERE c.fechaCreacion >= :fechaInicio AND c.fechaCreacion < :fechaFin AND c.emp_codigo.emp_codigo = :empCodigo"),
        @NamedQuery(name = "CobranzaIntegralOB.buscarPorFechaCreacion", query = "SELECT c " +
                "FROM CobranzaIntegralOB c " +
                "WHERE c.fechaCreacion >= :fechaInicio " +
                "AND c.fechaCreacion < :fechaFin")

})
public class CobranzaIntegralOB extends BandejaOB {
    @ManyToOne()
    @JoinColumn(name = "estado", nullable = false)
    public EstadosCobranzaIntegralOB estado;

    @ManyToOne
    @JoinColumn(name = "emp_codigo",nullable = false)
    public EmpresaOB emp_codigo;

    @ManyToOne
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

    @Column(name = "gcr")
    public String gcr;

}
