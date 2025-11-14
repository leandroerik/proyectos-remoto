package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Moneda")
public class MonedaOB {

	@Id
	@Column(name = "id")
	public Integer id;

	@Column(name = "descripcion", nullable = false)
	public String descripcion;

	@Column(name = "codigo_cobis", nullable = false)
	public String codigoCobis;

	@Column(name = "simbolo", nullable = false)
	public String simbolo;
	
	public MonedaOB() {
    }
	
	public MonedaOB(int id, String descripcion, String codigoCobis, String simbolo) {
		this.id = id;
		this.descripcion = descripcion;
		this.codigoCobis = codigoCobis;
		this.simbolo = simbolo;		
	}

}