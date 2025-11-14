package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Texto")
public class TextoOB {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	public Integer id;

	@ManyToOne()
	@JoinColumn(name = "id_tipo_producto_firma")
	public TipoProductoFirmaOB tipoProductoFirma;

	@Column(name = "titulo", nullable = false)
	public String titulo;
	
	@Column(name = "subtitulo", nullable = false)
	public String subtitulo;
	
	@Column(name = "descripcion", nullable = false)
	public String descripcion;
	
	@Column(name = "idFront", nullable = false)
	public String idFront;
	
	public TextoOB() {
    }
	
	public TextoOB(int id, TipoProductoFirmaOB tipoProductoFirma, String titulo, String subtitulo, String descripcion, String idFront) {
		this.id = id;
		this.tipoProductoFirma = tipoProductoFirma;
		this.titulo = titulo;
		this.subtitulo = subtitulo;	
		this.descripcion = descripcion;	
		this.idFront = idFront;	
	}

}
