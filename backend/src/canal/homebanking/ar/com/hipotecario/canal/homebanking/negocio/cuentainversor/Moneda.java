package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor;

public class Moneda {

	private String id;

	public Moneda() {
	}

	public Moneda(String id) {
		this.id = id;
	}

	public String getId() {
		return id != null ? id : "";
	}

	public void setId(String id) {
		this.id = id;
	}
}
