package ar.com.hipotecario.canal.officebanking.dto.prisma;

import java.math.BigDecimal;

public class PagoTarjetaDTO {
	
	private String cuenta;
	private String cuentaTarjeta;
	private BigDecimal importe;
	private String moneda;
	private String tipoTarjeta;
	private String tipoCuenta;
	
	public String getCuenta() {
		return cuenta;
	}
	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}
	public String getCuentaTarjeta() {
		return cuentaTarjeta;
	}
	public void setCuentaTarjeta(String cuentaTarjeta) {
		this.cuentaTarjeta = cuentaTarjeta;
	}
	public BigDecimal getImporte() {
		return importe;
	}
	public void setImporte(BigDecimal importe) {
		this.importe = importe;
	}
	public String getMoneda() {
		return moneda;
	}
	public void setMoneda(String moneda) {
		this.moneda = moneda;
	}
	public String getTipoTarjeta() {
		return tipoTarjeta;
	}
	public void setTipoTarjeta(String tipoTarjeta) {
		this.tipoTarjeta = tipoTarjeta;
	}
	public String getTipoCuenta() {
		return tipoCuenta;
	}
	public void setTipoCuenta(String tipoCuenta) {
		this.tipoCuenta = tipoCuenta;
	}
	
}
