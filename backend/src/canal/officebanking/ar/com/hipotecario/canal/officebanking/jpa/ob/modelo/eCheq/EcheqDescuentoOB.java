package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq;

import ar.com.hipotecario.canal.officebanking.enums.echeq.EnumAccionesEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import jakarta.persistence.*;

@Entity
@NamedQueries({
		@NamedQuery(
				name = "EcheqDescuentoOB.buscarPorEstado",
				query = "SELECT e FROM EcheqDescuentoOB e WHERE e.estado = :estado"
		)
})
@Table(schema = "dbo", name = "OB_Echeq_Descuento")

public class EcheqDescuentoOB extends BandejaOB {
	
	@Column(name = "numero_documento")
	public String numeroDocumento;
	
	@Column(name = "tipo_documento", nullable = false)
	public Integer tipoDocumento;
	
	@Column(name = "estado_codigo")
	public String estadoCodigo;
	
	@Column(name = "solicitud_numero")
	public String solicitudNumero;
	
	@ManyToOne()
    @JoinColumn(name = "estado", nullable = false)
    public EstadoEcheqOB estado;
	
	@ManyToOne
    @JoinColumn(name = "usu_codigo")
    public UsuarioOB usuario;
	
	@Column(name = "accion")
    public EnumAccionesEcheqOB accion;
	
}