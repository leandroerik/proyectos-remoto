package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.TextoOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Categorias_Comex")
public class CategoriaComexOB {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	public Integer id;

	@Column(name = "descripcion", nullable = false)
	public String descripcion;
	
	@ManyToOne()
	@JoinColumn(name = "idTexto")
	public TextoOB texto;
	@Column(name = "estado")
	public boolean estado;

	public CategoriaComexOB() {
    }
	
	public CategoriaComexOB(int id, String descripcion, TextoOB textoOB) {
		this.id = id;
		this.descripcion = descripcion;
		this.texto = textoOB;
	}
	
}
