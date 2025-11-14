package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin;


import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NamedQueries({
        @NamedQuery(name = "DebinProgramadoOB.findByIdDebin", query = "SELECT d " +
                "FROM DebinProgramadoOB d " +
                "WHERE d.idDebin = :idDebin order by d.id desc")
})
@Table(schema = "dbo",name = "OB_debin_programado")
public class DebinProgramadoOB extends BandejaOB {
    @Column(name = "idDebin")
    public String idDebin;

    @ManyToOne()
    @JoinColumn(name = "emp_codigo", nullable = false)
    public EmpresaOB emp_codigo;

    @ManyToOne()
    @JoinColumn(name = "usu_codigo")
    public UsuarioOB usuario;

    @Column(name = "estado", nullable = false)
    public String estado;

    @Column(name = "vendedor_cuit", nullable = false)
    public String vendedorCuit;

    @Column(name = "comprador_cuit", nullable = false)
    public String compradorCuit;

    @Column(name = "comprador_cbu", nullable = false)
    public String compradorCbu;

    @Column(name = "debin_moneda", nullable = false)
    public String debinMoneda;

    @Column(name = "debin_importe", nullable = false)
    public BigDecimal debinImporte;

    @Column(name = "debin_concepto", nullable = false)
    public String debinConcepto;

    @Column(name = "debin_detalle")
    public String debinDetalle;

    @Column(name = "debin_prestacion")
    public String debinPrestacion;

    @Column(name = "debin_referencia", nullable = false)
    public String debinReferencia;
    @Column(name = "debin_limite_cuotas", nullable = false)
    public int debinLimiteCuotas;

    @Column(name = "autorizado")
    public String autorizado;

    @Column(name = "fecha_creacion", nullable = false)
    public String fechaCreacion;
    @Column(name = "fecha_vencimiento", nullable = false)
    public String vencimiento;

    @Column(name = "cuit_creacion", nullable = false)
    public String cuitCreacion;

}