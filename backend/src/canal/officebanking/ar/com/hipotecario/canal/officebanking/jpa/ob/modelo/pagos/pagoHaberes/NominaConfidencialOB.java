package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(schema = "dbo", name = "OB_Nomina_Confindencial")
@NamedQueries({
        @NamedQuery(name = "NominaConfidencial", query = "SELECT emp FROM NominaConfidencialOB emp WHERE emp.usuario = :usuario AND emp.emp_codigo = :emp_codigo"),
        @NamedQuery(name = "NominaConfidencial.delete", query = "DELETE FROM NominaConfidencialOB emp WHERE emp.emp_codigo.emp_codigo = :emp_codigo"),
        @NamedQuery(name = "NominaConfidencial.findByEmpresa", query = "SELECT emp FROM NominaConfidencialOB emp WHERE emp.emp_codigo = :emp_codigo")
})
public class NominaConfidencialOB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public Integer id; 
    
    @ManyToOne()
    @JoinColumn(name = "emp_codigo", nullable = false)
    public EmpresaOB emp_codigo;

    @ManyToOne()
    @JoinColumn(name = "usu_codigo", nullable = false)
    public UsuarioOB usuario;
    
    @Column(name = "acreditacion")
    public Boolean acreditacion;


    @Column(name="fechaCreacion")
    public LocalDateTime fechaCreacion;

    @Column(name="fechaModificacion")
    public LocalDateTime fechaModificacion;

}
