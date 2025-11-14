package ar.com.hipotecario.mobile.negocio.cuentainversor;

public class Banco {

	private String id;

	public Banco() {
	}

	public Banco(String id) {
		this.id = id;
	}

	public String getId() {
		return id != null ? id : "";
	}

	public void setId(String id) {
		this.id = id;
	}
}
