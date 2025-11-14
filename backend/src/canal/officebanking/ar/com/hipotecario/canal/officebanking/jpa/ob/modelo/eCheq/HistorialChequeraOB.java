package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import jakarta.persistence.*;


@Entity
@Table(schema = "dbo", name = "OB_Historial_Chequera")
public class HistorialChequeraOB {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Integer id;

    @ManyToOne()
    @JoinColumn(name = "id_accion_trn", nullable = false)
    public AccionesOB accion;

    @ManyToOne()
    public ChequeraOB chequera;

    @ManyToOne()
    @JoinColumn(name = "id_usuario", nullable = false)
    public EmpresaUsuarioOB empresaUsuario;

    @ManyToOne()
    @JoinColumn(name = "id_est_inicial", nullable = false)
    public EstadoChequeraOB estadoInicial;

    @ManyToOne()
    @JoinColumn(name = "id_est_final", nullable = false)
    public EstadoChequeraOB estadoFinal;

    @ManyToOne()
    @JoinColumn(name = "id_moneda", nullable = false)
    public MonedaOB moneda;

    @Column(name = "cuenta_origen", nullable = false)
    public String cuentaOrigen;

    @Column(name = "tipo_producto")
    public String tipoProducto;
}
