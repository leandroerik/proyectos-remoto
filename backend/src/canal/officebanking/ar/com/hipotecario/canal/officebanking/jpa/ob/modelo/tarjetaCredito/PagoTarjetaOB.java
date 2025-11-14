package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tarjetaCredito;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Pago_Tarjeta_Credito")

public class PagoTarjetaOB extends BandejaOB {
	
	@Column(name = "cuentaTarjeta")
	public String cuentaTarjeta;
	
	@Column(name = "tipoCuenta", nullable = false)
	public String tipoCuenta;
	
	@Column(name = "tipoTarjeta")
	public String tipoTarjeta;
	
	@ManyToOne()
    @JoinColumn(name = "estado", nullable = false)
    public EstadoPagoTarjetaOB estado;
	
	@ManyToOne
    @JoinColumn(name = "usu_codigo")
    public UsuarioOB usuario;
	
}