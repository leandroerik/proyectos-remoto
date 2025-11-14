package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import jakarta.persistence.*;

import java.math.BigDecimal;
@Entity
@Table(schema = "dbo", name = "OB_Historial_Debin_Por_Lote")
public class HistorialDebinLote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Integer id;

    @ManyToOne()
    @JoinColumn(name = "id_accion_trn", nullable = false)
    public AccionesOB accion;

    @ManyToOne()
    public DebinLoteOB debinLote;

    @ManyToOne()
    @JoinColumn(name = "id_usuario", nullable = false)
    public EmpresaUsuarioOB empresaUsuario;

    @ManyToOne()
    @JoinColumn(name = "id_est_inicial", nullable = false)
    public EstadosDebinLoteOB estadoInicial;

    @ManyToOne()
    @JoinColumn(name = "id_est_final", nullable = false)
    public EstadosDebinLoteOB estadoFinal;

    @Column(name = "monto", nullable = false)
    public BigDecimal monto;

    @ManyToOne()
    @JoinColumn(name = "id_moneda", nullable = false)
    public MonedaOB moneda;

    @Column(name = "cuenta_origen", nullable = false)
    public String cuentaOrigen;

    @Column(name = "tipo_producto")
    public String tipoProducto;
}
