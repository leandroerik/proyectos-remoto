package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Tarjetas_Virtuales")
@NamedQueries({ 
	@NamedQuery(name = "TarjetaVirtualOB.findByEmpresa", query = "SELECT t FROM TarjetaVirtualOB t WHERE t.empresa = :emp_codigo"),
	@NamedQuery(name = "TarjetaVirtualOB.findByEmpresaAndId", query = "SELECT t FROM TarjetaVirtualOB t WHERE t.empresa = :emp_codigo and t.id>:id"),
	@NamedQuery(name = "TarjetaVirtualOB.lastIndex", query = "SELECT t.tvIndex FROM TarjetaVirtualOB t ORDER BY t.tvIndex DESC")
})
public class TarjetaVirtualOB {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	public Integer id;

	@ManyToOne
	@JoinColumn(name = "emp_codigo")
	public EmpresaOB empresa;
	
	@Column(name = "nro_tarjeta", nullable = false)
	public String nroTarjeta;
	
	@Column(name = "tv_index", nullable = false)
	public String tvIndex;
}