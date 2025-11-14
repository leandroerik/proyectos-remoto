package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(schema = "dbo",name = "OB_Firmas_Producto")
@NamedQueries({
        @NamedQuery(name = "FirmasProductoOB.buscarOperacionesSegunMonto", query = "SELECT o "
                + "FROM FirmasProductoOB o "
                + "WHERE o.id = :id "
                + "AND o.monto > 0"),
})
public class FirmasProductoOB {
    @Id
    @Column(name = "id_operacion", nullable = false)
    public Integer id;
    @Column(name = "cod_producto", nullable = false)
    public Integer codProducto;

    @Column(name = "monto", nullable = false)
    public BigDecimal monto;

}