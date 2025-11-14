package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores;

import java.sql.Blob;
import java.time.LocalDateTime;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import jakarta.persistence.GenerationType;
import jakarta.persistence.*;

@Entity
@Table(schema = "dbo", name = "OB_Pago_Beneficiarios")
@NamedQueries({
        @NamedQuery(name = "PagoBeneficiariosOB.buscarArchivo", query = "SELECT p "
                + "FROM PagoBeneficiariosOB p "
                + "WHERE p.nombreArchivo = :archivo "
                + "AND p.emp_codigo.emp_codigo = :empresa"
        ),
        @NamedQuery(name = "PagoBeneficiariosOB.buscarByNombre", query = "SELECT p FROM PagoBeneficiariosOB p WHERE p.nombreArchivo = :archivo")
})
public class PagoBeneficiariosOB {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    public Integer id;

    @ManyToOne()
    @JoinColumn(name = "emp_codigo", nullable = false)
    public EmpresaOB emp_codigo;

    @ManyToOne()
    @JoinColumn(name = "usu_codigo")
    public UsuarioOB usuario;

    @Column(name = "fecha_creacion", nullable = false)
    public LocalDateTime fechaCreacion;

    @Column(name = "cuenta_origen", nullable = false)
    public String cuentaOrigen;

    @Column(name = "ultima_modificacion")
    public LocalDateTime ultimaModificacion;

    @Column(name = "usu_modificacion")
    public String usuarioModificacion;

    @Column(name = "fecha_ult_actulizacion")
    public LocalDateTime fechaUltActulizacion;

    @Column(name = "nombre_archivo")
    public String nombreArchivo;

    @Lob
    @Column(name = "archivo")
    public Blob archivo;

    @Column(name = "cantidad_registros")
    public Integer cantidadRegistros;

    @Column(name = "tipo_producto")
    public String tipoProducto;

    @Column(name = "convenio")
    public Integer convenio;

    @Column(name = "subconvenio")
    public Integer subconvenio;
}
