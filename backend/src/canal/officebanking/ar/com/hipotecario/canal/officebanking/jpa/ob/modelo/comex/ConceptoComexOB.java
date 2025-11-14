package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Conceptos_Comex")
public class ConceptoComexOB {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	public Integer id;

	@Column(name = "codigo", nullable = false)
	public String codigo;
	
	@Column(name = "descripcion", nullable = false)
	public String descripcion;
	
	@ManyToOne()
	@JoinColumn(name = "idCategoria")
	public CategoriaComexOB categoria;
	
	@ManyToOne()
	@JoinColumn(name = "moneda", nullable = true)
	public MonedaOB moneda;
	
	@ManyToOne()
	@JoinColumn(name = "idItem", nullable = true)
	public ItemsConceptoOB item;

	@Column(name = "estado")
	public boolean estado;
	
	public ConceptoComexOB() {
    }
	
	public ConceptoComexOB(int id, String codigo, String descripcion, CategoriaComexOB categoria, MonedaOB moneda, ItemsConceptoOB item) {
		this.id = id;
		this.codigo = codigo;
		this.descripcion = descripcion;
		this.categoria = categoria;		
		this.moneda = moneda;
		this.item = item;
	}
}
