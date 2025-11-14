package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Cedip")
//@NamedQueries({
//	@NamedQuery(name = "CedipOB.findAll", query = "SELECT c FROM CedipOB"),
//	@NamedQuery(name = "CedipOB.buscarCedipPorNroCedipYCuit", query = "SELECT c FROM CedipOB c WHERE c.emp_codigo = :emp_codigo AND c.nroCedip = :nroCedip"),
//	@NamedQuery(name = "CedipOB.find", query = "SELECT c FROM CedipOB c WHERE ((:emp_codigo) IS NULL OR c.emp_codigo = :emp_codigo)") 
//})

public class CedipOB extends BandejaOB {
	
	@Column(name = "accion")
	public String accion;
	
	@Column(name = "fecha_accion") 
	public LocalDateTime fecha_accion;
	
//	@Column(name = "estado_cedip")
//	public String estado_cedip;
	
	@ManyToOne()
	@JoinColumn(name = "estado_cedip", nullable = false)
	public EstadoCedipOB estado_cedip;
	
	@Column(name = "estado_firma")
	public String estado_firma;

	@Column(name = "canal", nullable = false)
	public Integer canal;
	
	@Column(name = "cap_interes")
	public String capInteres;
	
	@Column(name = "cedip")
	public boolean cedip;	
	
	@Column(name = "cedip_CBU_Acred")
	public String cedipCBUAcred;
	
	@Column(name = "cedip_tipo_acred")
	public String cedipTipoAcred;
	
	@Column(name = "cuenta")
	public String cuenta;
	
	@Column(name = "id_plan_ahorro")
	public Integer idPlanAhorro;
	
	@Column(name = "id_cliente", nullable = false)
	public Integer idcliente;
	
	@Column(name = "cedip_moneda", nullable = false)
	public Integer monedaCedip;
	
	@Column(name = "cedip_monto", nullable = false)
	public BigDecimal montoCedip;
	
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