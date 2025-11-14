package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(schema = "dbo", name = "OB_PAP_Comprobantes")
@NamedQueries({
        @NamedQuery(name = "comprobantes", query = "SELECT c FROM ComprobantePAPOB c WHERE c.convenio = :convenio " +
                "AND c.subconvenio = :subconv " +
                "AND c.fechaCreacion  BETWEEN :fechaD and :fechaH " +
                "AND c.empresa = :codigo")
})
public class ComprobantePAPOB {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",nullable = false)
    public Long id;

    @ManyToOne
    @JoinColumn(name = "emp_codigo",nullable = false)
    public EmpresaOB empresa;

    @ManyToOne
    @JoinColumn(name = "usu_codigo",nullable = false)
    public UsuarioOB usuario;

    @Column(name = "fecha_creacion", nullable = false)
    public LocalDateTime fechaCreacion;

    @Column(name = "ultima_modificacion")
    public LocalDateTime ultimaModificacion;

    @Column(name = "nombre_archivo")
    public String nombreArchivo;

    @Column(name = "convenio")
    public Integer convenio;

    @Column(name = "subconvenio")
    public Integer subconvenio;

    @Column(name = "cantidad_filas")
    public Integer cantidad_filas;

    @Column(name = "estado")
    public String estado;

}
