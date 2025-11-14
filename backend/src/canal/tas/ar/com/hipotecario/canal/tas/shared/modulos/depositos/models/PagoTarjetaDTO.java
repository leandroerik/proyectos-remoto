package ar.com.hipotecario.canal.tas.shared.modulos.depositos.models;

import ar.com.hipotecario.backend.conector.sql.SqlObjetos;

public class PagoTarjetaDTO extends DepositoCuentaDTO {

	public static class DepositoCuentaDTO extends SqlObjetos<DepositoCuentaDTO> {
	}
	
	private String numeroTarjeta; 
	private String tipoTarjeta; 
	private String tipoTitularidad;
	
	
	public String getNumeroTarjeta() {
		return numeroTarjeta;
	}
	public void setNumeroTarjeta(String numeroTarjeta) {
		this.numeroTarjeta = numeroTarjeta;
	}
	public String getTipoTarjeta() {
		return tipoTarjeta;
	}
	public void setTipoTarjeta(String tipoTarjeta) {
		this.tipoTarjeta = tipoTarjeta;
	}
	public String getTipoTitularidad() {
		return tipoTitularidad;
	}
	public void setTipoTitularidad(String tipoTitularidad) {
		this.tipoTitularidad = tipoTitularidad;
	}

}
