package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import ar.com.hipotecario.canal.officebanking.enums.Comex.EnumCambioComexOB;
import ar.com.hipotecario.canal.officebanking.enums.Comex.EnumCondicionComexOB;
import ar.com.hipotecario.canal.officebanking.enums.Comex.EnumTipoPersonaComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_OrdenPago_Comex")
@NamedQueries({
    @NamedQuery(name = "OrdenPagoComexOB.filtrarOrdenesPagosHistorial", query = ""
    		+ "SELECT d "
            + "FROM OrdenPagoComexOB d "
            + "WHERE d.empresa = :empresa "
            + "AND ((:fechaDesde IS NULL) OR (:fechaHasta IS NULL) OR d.fechaCreacion BETWEEN :fechaDesde AND :fechaHasta) "
            + "AND ((:cuenta IS NULL) OR d.cuentaOrigen = :cuenta) "
            + "ORDER BY d.fechaCreacion DESC"),
    @NamedQuery(name = "OrdenPagoComexOB.buscarPorTRR", query = ""
    		+ "SELECT d "
    		+ "FROM OrdenPagoComexOB d "
			+ "WHERE d.numeroTRR = :numeroTRR "
			+ "ORDER BY d.url DESC"),
    @NamedQuery(name = "OrdenPagoComexOB.listarCuentas", query = ""
    		+ "SELECT d "
            + "FROM OrdenPagoComexOB d "
            + "WHERE d.empresa = :empresa "
            + "ORDER BY d.cuentaOrigen DESC"),
})

public class OrdenPagoComexOB extends BandejaOB {

	@ManyToOne()
	@JoinColumn(name = "idCategoria", nullable = false)
	public CategoriaComexOB categoria;
	
	@ManyToOne()
	@JoinColumn(name = "idConcepto", nullable = false)
	public ConceptoComexOB concepto;
	
	@Column(name = "razon_social", nullable = false)
	public String razonSocial;
	
	@Column(name = "numero_trr", nullable = false)
	public String numeroTRR;

	@Column(name = "rectificacion", nullable = false)
	public Character rectificacion;
	
	@Column(name = "bienes_servicio", nullable = false)
	public Boolean bienesYservicio;
	
	@Column(name = "nro_cuenta_credito_pesos", nullable = false)
	public String nroCuentaCreditoPesos;
	
	@Column(name = "nro_cuenta_cred_moneda_ext", nullable = false)
	public String nroCuentaCredMonedaExt;
	
	@Column(name = "url", nullable = false)
	public String url;

	@Column(name = "monto_moneda_ext", nullable = false)
	public BigDecimal montoMonedaExt;
	
	@ManyToOne()
	@JoinColumn(name = "simbolo_moneda_ext", nullable = false)
	public MonedaOB simboloMonedaExt;
	
	@ManyToOne()
	@JoinColumn(name = "estado", nullable = false)
	public EstadoOPComexOB estado;
	
	@Column(name = "fecha_creacion", nullable = false)
	public LocalDateTime fechaCreacion;
	
	@Column(name = "fecha_modificacion", nullable = false)
	public LocalDateTime fechaModificacion;
	
	@ManyToOne()
	@JoinColumn(name = "usu_codigo")
	public UsuarioOB usuario;

	@Column(name = "cambio")
	public EnumCambioComexOB cambio;

	@Column(name = "condicion")
	public EnumCondicionComexOB condicion;
	@Column(name = "cuit_cuil")
	public String cuitCuil;
	@Column(name = "persona")
	public EnumTipoPersonaComexOB persona;
	@Column(name = "relacion")
	public boolean relacion;
	
	public OrdenPagoComexOB() {
	}
	
	public OrdenPagoComexOB(Integer id, CategoriaComexOB categoria, ConceptoComexOB concepto, String razonSocial, String numeroTRR, Character rectificacion, Boolean bienesYservicio, String nroCuentaCreditoPesos, String nroCuentaCredMonedaExt, String url, BigDecimal monto, MonedaOB moneda, BigDecimal montoMonedaExt, MonedaOB simboloMonedaExt, EstadoOPComexOB estado, LocalDateTime fechaCreacion, LocalDateTime fechaModificacion, UsuarioOB usuario,
			TipoProductoFirmaOB tipoProductoFirma, EmpresaOB empresa, EstadoBandejaOB estadoBandeja, String cuentaOrigen, LocalDateTime fechaUltActulizacion) {
		this.id = id;
		this.categoria = categoria;
		this.concepto = concepto;
		this.razonSocial = razonSocial;
		this.numeroTRR = numeroTRR;
		this.rectificacion = rectificacion;
		this.bienesYservicio = bienesYservicio;
		this.nroCuentaCreditoPesos = nroCuentaCreditoPesos;
		this.nroCuentaCredMonedaExt = nroCuentaCredMonedaExt;
		this.url = url;
		this.monto = monto;
		this.moneda = moneda;
		this.montoMonedaExt = montoMonedaExt;
		this.simboloMonedaExt = simboloMonedaExt;
		this.estado = estado;
		this.fechaCreacion = fechaCreacion; 
		this.fechaModificacion = fechaModificacion;
		this.usuario = usuario;
		this.tipoProductoFirma = tipoProductoFirma;
		this.empresa = empresa;
		this.estadoBandeja = estadoBandeja;
		this.cuentaOrigen = cuentaOrigen;
		this.fechaUltActulizacion = fechaUltActulizacion;
	}	
}
