package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import java.time.LocalDateTime;

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
@Table(schema = "dbo", name = "OB_Clave_Usuario")
@NamedQueries({
	@NamedQuery(name="UltimaClave", query= "SELECT cu FROM ClaveUsuarioOB cu WHERE cu.usuario = :usuario ORDER BY cu.id DESC"),
})
public class ClaveUsuarioOB {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "clu_id", nullable = false)
	public Integer id;

	@ManyToOne()
	@JoinColumn(name = "usu_codigo", nullable = false)
	public UsuarioOB usuario;

	@Column(name = "clu_fecha", nullable = false)
	public LocalDateTime fechaCreacion;

	@Column(name = "clu_version_enc", nullable = false)
	public String versionEncriptacion;

	@Column(name = "clu_fecha_bloqueo")
	public LocalDateTime fechaBloqueo;

	@Column(name = "clu_fecha_desbloqueo")
	public LocalDateTime fechaDesbloqueo;

	@Column(name = "clu_clave", nullable = false)
	public String clave;

	public ClaveUsuarioOB() {
		this.fechaCreacion = LocalDateTime.now();
		this.versionEncriptacion = "SHA-512";
	}

}