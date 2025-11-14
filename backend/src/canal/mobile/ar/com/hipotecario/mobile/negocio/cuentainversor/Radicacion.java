package ar.com.hipotecario.mobile.negocio.cuentainversor;

public class Radicacion {

	private String agenteColocador;
	private String sucursal;
	private String canalVivienda;
	private String oficinaCuenta;

	public Radicacion() {
	}

	public Radicacion(String agenteColocador, String sucursal, String canalVivienda, String oficinaCuenta) {
		this.agenteColocador = agenteColocador;
		this.sucursal = sucursal;
		this.canalVivienda = canalVivienda;
		this.oficinaCuenta = oficinaCuenta;
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

	public String getCanalVivienda() {
		return canalVivienda != null ? canalVivienda : "";
	}

	public void setCanalVivienda(String canalVivienda) {
		this.canalVivienda = canalVivienda;
	}

	public String getOficinaCuenta() {
		return oficinaCuenta != null ? oficinaCuenta : "";
	}

	public void setOficinaCuenta(String oficinaCuenta) {
		this.oficinaCuenta = oficinaCuenta;
	}
}
