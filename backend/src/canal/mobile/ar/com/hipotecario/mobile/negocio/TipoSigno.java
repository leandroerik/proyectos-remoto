package ar.com.hipotecario.mobile.negocio;

public enum TipoSigno {
	ND("N/D"), NC("N/C");

	private String tipo;

	TipoSigno(String tipo) {
		this.tipo = tipo;
	}

	public String valor() {
		return tipo;
	}
}
