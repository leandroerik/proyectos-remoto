package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.BE;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "BE_Transferencia_TRN", schema = "dbo")
@NamedQueries({
        @NamedQuery(
                name = "BETransferencia.findByCuitEmpresaAndCbuCredito",
                query = "SELECT b FROM BETransferencia b " +
                        "WHERE b.cuitEmpresa = :cuitEmpresa " +
                        "AND b.cbuCredito = :cbuCredito"
        )
})
public class BETransferencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trt_id", nullable = false)
    private Integer id;

    @Column(name = "emp_cuit_empresa", precision = 11, scale = 0)
    private BigDecimal cuitEmpresa;

    @Column(name = "tit_id_tipo_transferencia", nullable = false)
    private String tipoTransferencia;

    @Column(name = "cont_id_concepto", precision = 2, scale = 0)
    private BigDecimal conceptoId;

    @Column(name = "camt_id_camara", nullable = false, length = 2)
    private String camara;

    @Column(name = "trt_fecha_aplicacion")
    private LocalDateTime fechaAplicacion;

    @Column(name = "trt_monto", nullable = false)
    private BigDecimal monto;

    @Column(name = "trt_debito_consolidado", nullable = false)
    private Boolean debitoConsolidado;

    @Column(name = "trt_cbu_debito", nullable = false, length = 22)
    private String cbuDebito;

    @Column(name = "trt_cuit_debito", precision = 11, scale = 0)
    private BigDecimal cuitDebito;

    @Column(name = "trt_denominacion_debito", length = 255)
    private String denominacionDebito;

    @Column(name = "trt_descripcion_debito", length = 255)
    private String descripcionDebito;

    @Column(name = "mot_id_moneda_debito", precision = 3, scale = 0)
    private BigDecimal monedaDebitoId;

    @Column(name = "trt_nro_cuenta_debito", length = 17)
    private String nroCuentaDebito;

    @Column(name = "trt_tp_cuenta_debito", length = 3)
    private String tipoCuentaDebito;

    @Column(name = "trt_cbu_credito", nullable = false, length = 22)
    private String cbuCredito;

    @Column(name = "trt_cuit_credito", precision = 11, scale = 0)
    private BigDecimal cuitCredito;

    @Column(name = "trt_denominacion_credito", nullable = false, length = 255)
    private String denominacionCredito;

    @Column(name = "trt_nro_cuenta_credito", length = 17)
    private String nroCuentaCredito;

    @Column(name = "trt_tp_cuenta_credito", length = 3)
    private String tipoCuentaCredito;

    @Column(name = "trt_email_credito", length = 255)
    private String emailCredito;

    @Column(name = "trt_descripcion_credito", length = 255)
    private String descripcionCredito;

    @Column(name = "trt_comentario_credito", length = 255)
    private String comentarioCredito;

    @Column(name = "mot_id_moneda_credito", precision = 3, scale = 0)
    private BigDecimal monedaCreditoId;

    @Column(name = "trt_cbu_validadoXrucc", nullable = false)
    private Boolean cbuValidado;

    @Column(name = "trt_id_carga", nullable = false)
    private Integer idCarga;

    @Column(name = "usu_codigo_creacion")
    private Integer codigoCreacion;

    @Column(name = "trt_fecha_creacion")
    public LocalDateTime fechaCreacion;

    @Column(name = "usu_codigo_modificacion")
    private Integer codigoModificacion;

    @Column(name = "trt_fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @Column(name = "est_id_estado", nullable = false)
    private Integer estadoId;

    public BETransferencia() {}
}

