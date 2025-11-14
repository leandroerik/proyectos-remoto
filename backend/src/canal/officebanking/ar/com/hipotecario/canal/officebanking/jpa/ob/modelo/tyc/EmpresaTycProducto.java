package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tyc;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(name = "OB_Empresa_Tyc_Producto", schema = "dbo")
@NamedQuery(name = "EmpresaTycProducto.obtenerPorEmpresaYProducto",
query = "SELECT etp FROM EmpresaTycProducto etp WHERE etp.empCodigo = :empresaCodigo AND etp.idTycProducto = :productoId")
public class EmpresaTycProducto {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "emp_codigo")
    private int empCodigo;

    @Column(name = "id_tyc_producto")
    private int idTycProducto;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getEmpCodigo() {
		return empCodigo;
	}

	public void setEmpCodigo(int empCodigo) {
		this.empCodigo = empCodigo;
	}

	public int getIdTycProducto() {
		return idTycProducto;
	}

	public void setIdTycProducto(int idTycProducto) {
		this.idTycProducto = idTycProducto;
	}
    
}