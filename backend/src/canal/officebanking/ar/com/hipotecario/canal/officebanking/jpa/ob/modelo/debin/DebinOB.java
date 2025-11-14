package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Debin")
@NamedQueries({
        @NamedQuery(name = "DebinOB.findByIdDebin", query = "SELECT d " +
                "FROM DebinOB d " +
                "WHERE d.idDebin = :idDebin")
})
public class DebinOB extends BandejaOB {

    @ManyToOne()
    @JoinColumn(name = "emp_codigo", nullable = false)
    public EmpresaOB emp_codigo;

    @ManyToOne()
    @JoinColumn(name = "usu_codigo")
    public UsuarioOB usuario;

    @Column(name = "fecha_creacion", nullable = false)
    public String fechaCreacion;

    @Column(name = "vencimiento", nullable = false)
    public String vencimiento;

    @ManyToOne()
    @JoinColumn(name = "concepto_codigo", nullable = false)
    public ConceptoDebinOB concepto;

    @ManyToOne()
    @JoinColumn(name = "estadoRecibida", nullable = false)
    public EstadoDebinRecibidasOB estadoRecibida;

    @ManyToOne()
    @JoinColumn(name = "estadoEnviada", nullable = false)
    public EstadoDebinEnviadasOB estadoEnviada;

    @Column(name = "referencia_solicitud")
    public String referenciaSolicitud;

    @Column(name = "referencia_aceptacion")
    public String referenciaAceptacion;

    @Column(name = "idDebin")
    public String idDebin;

    @Column(name = "comprador_idTributario")
    public String idTributarioComprador;

    @Column(name = "comprador_nombre")
    public String nombreComprador;

    @Column(name = "comprador_cbu")
    public String cbuComprador;

    @Column(name = "comprador_cuenta")
    public String cuentaComprador;

    @Column(name = "comprador_tipo_cuenta")
    public String tipoCuentaComprador;

    @Column(name = "comprador_sucursal_id")
    public String sucursalIdVendedor;

    @Column(name = "comprador_sucursal_desc")
    public String sucursalDescVendedor;

    @Column(name = "vendedor_cuenta")
    public String cuentaVendedor;

    @Column(name = "vendedor_cuit")
    public String cuitVendedor;
}
