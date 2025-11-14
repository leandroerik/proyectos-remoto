package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import jakarta.persistence.*;

@Entity
@Table(schema = "dbo", name = "OB_Razones_Por_Empresa")
public class RazonesPorEmpresaOB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    int id;

    @ManyToOne
    @JoinColumn(name = "emp_codigo",nullable = false)
    public EmpresaOB emp_codigo;

    @Column(name = "razon_social")
    public String razonSocial;
    @Column
    public String cuit;
    @Column
    public String email;
}
