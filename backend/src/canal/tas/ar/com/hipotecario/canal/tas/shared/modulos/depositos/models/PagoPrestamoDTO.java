package ar.com.hipotecario.canal.tas.shared.modulos.depositos.models;

import ar.com.hipotecario.backend.conector.sql.SqlObjetos;

public class PagoPrestamoDTO extends DepositoCuentaDTO {

	public static class DepositoCuentaDTO extends SqlObjetos<DepositoCuentaDTO> {
	}
	
	private String numeroPrestamo; 
	private String tipoPrestamo; 
	private String idPrestamo;
	
	
	public String getNumeroPrestamo() {
		return numeroPrestamo;
	}
	public void setNumeroPrestamo(String numeroPrestamo) {
		this.numeroPrestamo = numeroPrestamo;
	}
	public String getTipoPrestamo() {
		return tipoPrestamo;
	}
	public void setTipoPrestamo(String tipoPrestamo) {
		this.tipoPrestamo = tipoPrestamo;
	}
	public String getIdPrestamo() {
		return idPrestamo;
	}
	public void setIdPrestamo(String idPrestamo) {
		this.idPrestamo = idPrestamo;
	}
	
	
	


}
