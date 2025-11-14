package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor;

public class Origen {
	private String agenteColocador;
	private String sucursal;

	public Origen() {
	}

	public Origen(String agenteColocador, String sucursal) {
		this.agenteColocador = agenteColocador;
		this.sucursal = sucursal;
	}

	public String getAgenteColocador() {
		return agenteColocador != null ? agenteColocador : "";
	}

	public void setAgenteColocador(String agenteColocador) {
		this.agenteColocador = agenteColocador;
	}

	public String getSucursal() {
		return sucursal != null ? sucursal : "";
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}
}
