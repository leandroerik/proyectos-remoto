package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Lista_Blanca")
public class ListaBlancaOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer id;

	@Column(name = "usu_cuil", nullable = false, unique = false, columnDefinition = "NUMERIC(11,0)")
	public Long cuil;

}