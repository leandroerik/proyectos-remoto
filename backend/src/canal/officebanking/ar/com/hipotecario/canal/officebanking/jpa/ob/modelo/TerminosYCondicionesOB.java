package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import jakarta.persistence.*;


@Entity
@Table(schema = "dbo", name = "OB_Terminos_Condiciones")
@NamedQueries({
        @NamedQuery(name = "TerminosYCondicionesOB.buscarPorEmpresaYCuenta", query = "SELECT p FROM TerminosYCondicionesOB p WHERE p.empCodigo =:idEmpresa AND p.numeroCuenta = :numeroCuenta")
})
public class TerminosYCondicionesOB {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @ManyToOne()
    @JoinColumn(name = "empresa", nullable = false)
    public EmpresaOB empCodigo;

    @Column(name = "numero_cuenta", nullable = false, length = 32)
    public String numeroCuenta;

    @Column(name = "producto", nullable = true, length = 32)
    public String producto;

    @Column(name = "tyc", nullable = false)
    public Boolean tyc;

    @Column(name = "v_tyc", nullable = true)
    public Integer vTyc;
}