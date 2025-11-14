package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.mocks;
import jakarta.persistence.*;
@Entity
@Table(name = "OB_MockTC", schema = "dbo")
@NamedQueries({
        @NamedQuery(name = "MockTCOB.findByIdPrisma", query = "SELECT m FROM MockTCOB m WHERE m.idPrisma = :idPrisma AND m.servicio = 'obtenerCuentas'"),
        @NamedQuery(name = "MockTCOB.findByIdPrismaYCuenta", query = "SELECT m FROM MockTCOB m WHERE m.idPrisma = :idPrisma AND m.cuenta = :cuenta AND m.servicio = 'obtenerVencimientos'"),
        @NamedQuery(name = "MockTCOB.findListadoTarjetasByIdPrismaYCuenta",
                query = "SELECT m FROM MockTCOB m WHERE m.idPrisma = :idPrisma AND m.cuenta = :cuenta AND m.servicio = 'obtenerListadoTarjetas'"),
        @NamedQuery(name = "MockTCOB.findStopDebitByCuenta",
                query = "SELECT m FROM MockTCOB m WHERE m.cuenta = :cuenta AND m.servicio = 'stopDebit'"),
        @NamedQuery(name = "MockTCOB.findVencimientoTarjetasByIdPrismaYCuenta",
                query = "SELECT m FROM MockTCOB m WHERE m.idPrisma = :idPrisma AND m.cuenta = :cuenta AND m.servicio = 'obtenerVencimientos'"),
        @NamedQuery(name = "MockTCOB.findTransaccionesByIdPrismaYRequest",
                query = "SELECT m FROM MockTCOB m WHERE m.idPrisma = :idPrisma AND m.requestBody = :requestBody AND m.servicio = 'obtenerTransacciones'"),
        @NamedQuery(name = "MockTCOB.findByIdCliente", query = "SELECT m FROM MockTCOB m WHERE m.idPrisma = :idCliente"),


})
public class MockTCOB {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "idPrisma", nullable = false)
    private String idPrisma;

    @Column(name = "servicio", nullable = false)
    private String servicio;

    @Column(name = "cuenta")
    private String cuenta;

    @Column(name = "requestBody")
    private String requestBody;

    @Column(name = "httpCode", nullable = false)
    private Integer httpCode;

    @Column(name = "httpBody", columnDefinition = "TEXT", nullable = false)
    private String httpBody;

    public String getHttpBody() {
        return httpBody;

    }
}
