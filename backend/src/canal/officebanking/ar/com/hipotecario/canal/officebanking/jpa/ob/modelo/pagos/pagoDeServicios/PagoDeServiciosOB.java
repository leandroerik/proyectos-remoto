package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoDeServicios;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.EstadoPagoOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(schema = "dbo", name = "OB_Pago_De_Servicios")
@NamedQueries({
        @NamedQuery(name = "PagoDeServiciosOB.buscarSinFirmaCambioDeDia", query = "SELECT p FROM PagoDeServiciosOB p WHERE p.estado = :estadoEnBandeja"),
        @NamedQuery(name = "PagoDeServiciosOB.buscarPorCpeEstadoYEmpresa", query = "SELECT p FROM PagoDeServiciosOB p WHERE p.estado.id = :estado AND p.codigoLink = :codigoLink AND p.emp_codigo = :empresa AND p.ente = :ente"),
        @NamedQuery(name = "PagoDeServiciosOB.buscarPorUsuarioLP", query = "SELECT p FROM PagoDeServiciosOB p WHERE p.usuarioLP = :usuarioLP"),
        @NamedQuery(name = "PagoDeServiciosOB.buscarPorEmpresaYFiltros", query = "SELECT p FROM PagoDeServiciosOB p WHERE p.emp_codigo = :emp_codigo AND (p.ente = :ente) AND (p.estado.id = :estado) AND ((:fechaDesde IS NULL) OR (:fechaHasta IS NULL) OR p.fechaPago BETWEEN :fechaDesde AND :fechaHasta) AND (:codigoLink IS NULL OR p.codigoLink = :codigoLink) ORDER BY p.fechaPago DESC"),
        @NamedQuery(name = "PagoDeServiciosOB.buscarPorEmpresaYEnte", query = 
        "SELECT p FROM PagoDeServiciosOB p "
        + "WHERE p.emp_codigo = :emp_codigo "
        + "AND (:ente IS NULL OR p.ente = :ente) "
        + "AND ((:fechaDesde IS NULL) OR (:fechaHasta IS NULL) OR p.fechaPago BETWEEN :fechaDesde AND :fechaHasta) "
        + "ORDER BY p.fechaPago DESC"),
        @NamedQuery(name = "PagoDeServiciosOB.buscarPorEmpresaYEstadoCodigoLink", query = ""
        		+ "SELECT p FROM PagoDeServiciosOB p "
        		+ "WHERE p.emp_codigo = :emp_codigo AND p.estado.id = :estado AND p.codigoLink = :codigoLink")
})
public class PagoDeServiciosOB extends BandejaOB {

    @Column(name = "ente", nullable = false)
    public String ente;

    @Column(name = "descripcion_ente", length = 1000)
    public String descripcionEnte;

    @Column(name = "rubro", nullable = false)
    public String rubro;

    @Column(name = "codigo_link", nullable = false)
    public String codigoLink;

    @Column(name = "fecha_pago")
    public LocalDateTime fechaPago;

    @ManyToOne()
    @JoinColumn(name = "estado", nullable = false)
    public EstadoPagoOB estado;

    @ManyToOne()
    @JoinColumn(name = "usu_codigo")
    public UsuarioOB usuario;

    @Column(name = "fecha_creacion", nullable = false)
    public LocalDateTime fechaCreacion;

    @Column(name = "ultima_modificacion")
    public LocalDateTime ultimaModificacion;

    @Column(name = "usu_modificacion")
    public String usuarioModificacion;

    @Column(name = "concepto", nullable = false)
    public String conceptoId;

    @Column(name = "referencia")
    public String referencia;

    @Column(name = "identificador_pago")
    public String identificadorPago;

    @Column(name = "vencimiento")
    public LocalDate vencimiento;

    @Column(name = "usuario_LP")
    public String usuarioLP;

    @ManyToOne()
    @JoinColumn(name = "emp_codigo", nullable = false)
    public EmpresaOB emp_codigo;

    @Column(name = "id_deuda")
    public String idDeuda;

    @Column(name = "descripcion")
    public String descripcion;
}