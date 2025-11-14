package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Completa_Firma")
public class CompletaFirmaOB {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	public Integer id;

	@ManyToOne()
	@JoinColumn(name = "id_bandeja")
	public BandejaOB bandeja;

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "usu_codigo", nullable = false)
	public UsuarioOB usuario;
}
