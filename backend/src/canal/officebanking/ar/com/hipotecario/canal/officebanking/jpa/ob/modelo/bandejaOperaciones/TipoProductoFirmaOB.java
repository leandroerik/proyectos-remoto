package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Tipo_Producto_Firma")
@NamedQueries({ @NamedQuery(name = "TipoProductoFirmaOB.listarPorActivo", query = "SELECT t FROM TipoProductoFirmaOB t where t.activo = true ORDER BY t.descripcion DESC") })
public class TipoProductoFirmaOB {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	public Integer id;

	@Column(name = "activo", nullable = false)
	public Boolean activo;

	@Column(name = "descripcion", nullable = false)
	public String descripcion;

	@Column(name = "cod_prod_firma", nullable = false)
	public Integer codProdFirma;

	@Column(name = "multiple_firma", nullable = false)
	public Boolean multiproducto;

	public TipoProductoFirmaOB() {
    }
	
	public TipoProductoFirmaOB(Integer id, Boolean activo, String descripcion, Integer codProdFirma,
			Boolean multiproducto) {
		super();
		this.id = id;
		this.activo = activo;
		this.descripcion = descripcion;
		this.codProdFirma = codProdFirma;
		this.multiproducto = multiproducto;
	}
}