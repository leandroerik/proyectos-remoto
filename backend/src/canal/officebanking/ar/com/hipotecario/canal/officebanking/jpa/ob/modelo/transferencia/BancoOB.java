package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Bancos")
public class BancoOB {
	@Id
	@Column(name = "codigo")
	public Integer codigo;

	@Column(name = "denominacion", length = 120)
	public String denominacion;

	public BancoOB() {
	}

	public BancoOB(Integer codigo, String denominacion) {
		this.codigo = codigo;
		this.denominacion = denominacion;
	}

}