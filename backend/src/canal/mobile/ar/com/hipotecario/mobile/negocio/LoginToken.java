package ar.com.hipotecario.mobile.negocio;

import java.time.LocalDateTime;

public class LoginToken {
	private String uuid;
	private LocalDateTime fecha;
	private String idCobis;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public LocalDateTime getFecha() {
		return fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

	public String getIdCobis() {
		return idCobis;
	}

	public void setIdCobis(String idCobis) {
		this.idCobis = idCobis;
	}
}