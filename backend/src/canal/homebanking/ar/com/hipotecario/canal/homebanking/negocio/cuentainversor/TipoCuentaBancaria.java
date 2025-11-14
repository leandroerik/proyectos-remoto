package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor;

public class TipoCuentaBancaria {

	private String id;

	public TipoCuentaBancaria() {
	}

	public TipoCuentaBancaria(String id) {
		this.id = id;
	}

	public String getId() {
		return id != null ? id : "";
	}

	public void setId(String id) {
		this.id = id;
	}
}
