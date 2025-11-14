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
@Table(schema = "dbo", name = "OB_Cedip_Acciones")

public class CedipAccionesOB extends BandejaOB {
	
	@Column(name = "accion")
	public String accion;
	
	@Column(name = "fecha_accion") 
	public LocalDateTime fecha_accion;
	
	@Column(name = "fecha_vencimiento") 
	public String fechaVencimiento;
	
	@ManyToOne()
	@JoinColumn(name = "estado_cedip", nullable = false)
	public EstadoCedipOB estado;

	
	@Column(name = "estado_firma")
	public String estado_firma;

	@Column(name = "canal", nullable = false)
	public Integer canal;
	
	@Column(name = "cedip")
	public boolean cedip;	
	
	@Column(name = "cuenta")
	public String cuenta;
	
	@Column(name = "id_cliente", nullable = false)
	public Integer idcliente;
	
	@Column(name = "cedip_moneda", nullable = false)
	public Integer monedaCedip;
	
	@Column(name = "usuario_alta")
	public String usuarioAlta;
	
	/////
	//ACCIONES
	@Column(name = "cedip_id")
	public String cedipId;
	
	@Column(name = "cedip_CBU_Acred")
	public String cedipCBUAcred;
	
	@Column(name = "cedip_tipo_acred")
	public String cedipTipoAcred;
	
	@Column(name = "codigo_banco")
	public String codigoBanco;
	
	@Column(name = "ejecutor_documento")
	public String ejecutorDocumento;
	
	@Column(name = "ejecutor_tipo_documento")
	public String ejecutorTipoDocumento;
	
	@Column(name = "documento_firmante")
	public String documentoFirmante;
	
	@Column(name = "tipo_documento_firmante")
	public String tipoDocumentoFirmante;
    
	@Column(name = "fraccion_id")
	public Integer fraccionId;
	
	@Column(name = "fraccionado")
	public boolean fraccionado;
	
	@Column(name = "tenedor_documento")
	public String tenedorDocumento;
	
	@Column(name = "tenedor_tipo_documento")
    public String tenedorTipoDocumento;
	
	@Column(name = "beneficiario_documento")
	public String beneficiarioDocumento;
	
	@Column(name = "beneficiario_nombre")
    public String beneficiarioNombre;
	
	@Column(name = "beneficiario_tipo_documento")
    public String beneficiarioTipoDocumento;

	@Column(name = "cedip_monto", nullable = false)
	public BigDecimal montoCedip;
	
	@Column(name = "tipo_transmision")
    public String tipoTransmision;
	
	@Column(name = "tipo_acreditacion")
    public String tipoAcreditacion;
	
	@Column(name = "cbu_acreditar")
    public String cbuAcreditar;
	
	@Column(name = "tipo_operacion")
	public String tipoOperacion;
	
	@Column(name = "tipo_cuenta")
	public String tipoCuenta;
	
	@Column(name = "monto_depositado")
	public BigDecimal montoDepositado;
	
	@Column(name = "monto_intereses")
	public BigDecimal montoIntereses;
	
	@Column(name = "monto_a_cobrar")
	public BigDecimal montoACobrar;
	
	@Column(name = "monto_retencion")
	public BigDecimal montoRetencion;
}