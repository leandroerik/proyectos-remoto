package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(schema = "dbo",name = "OB_Referencia_Pago_Servicios")
public class ReferenciaPagoServicioOB implements Serializable {

    @ManyToOne
    @JoinColumn(name = "emp_codigo")
    @Id
    public EmpresaOB empresa;

    @Column(name = "ente", nullable = false)
    @Id
    public String ente;

    @Column(name = "codigo_link", nullable = false)
    @Id
    public String codigoLink;

    @Column(name = "referencia", nullable = false)
    public String referencia;

    @Column(name = "fecha_modificacion", nullable = false)
    public LocalDateTime fechaModificacion;

    @Column(name = "fecha_creacion", nullable = false)
    public LocalDateTime fechaCreacion;


}
