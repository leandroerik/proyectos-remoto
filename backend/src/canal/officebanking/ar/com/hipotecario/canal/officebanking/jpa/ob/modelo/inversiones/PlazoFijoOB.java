package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones;

import java.time.LocalDateTime;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_PlazoFijo")

public class PlazoFijoOB extends BandejaOB {
	
	@Column(name = "accion")
	public String accion;
	
	@Column(name = "fecha_accion") 
	public LocalDateTime fecha_accion;
	
	@Column(name = "nro_plazo_fijo")
	public String nroPlazoFijo;
	
	@ManyToOne()
	@JoinColumn(name = "estado_plazo_fijo", nullable = false)
	public EstadoCedipOB estado_plazo_fijo;
	
	@Column(name = "estado_firma")
	public String estado_firma;

	@Column(name = "canal", nullable = false)
	public Integer canal;
	
	@Column(name = "cap_interes")
	public String capInteres;
	
	@Column(name = "cuenta")
	public String cuenta;
	
	@Column(name = "id_plan_ahorro")
	public Integer idPlanAhorro;
	
	@Column(name = "id_cliente", nullable = false)
	public Integer idcliente;
	
	@Column(name = "nro_operacion")
	public Integer nroOperacion;
	
	@Column(name = "periodo")
	public Integer periodo;
	
	@Column(name = "plazo", nullable = false)
	public Integer plazo;
	
	@Column(name = "renova")
	public String renova;
	
	@Column(name = "reverso")
	public String reverso;
	
	@Column(name = "tipo_cuenta")
	public String tipoCuenta;
	
	@Column(name = "tipo_operacion", nullable = false)
	public String tipoOperacion;
	
	@Column(name = "usuario_alta")
	public String usuarioAlta;
	
}