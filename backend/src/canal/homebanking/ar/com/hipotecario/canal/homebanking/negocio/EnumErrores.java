package ar.com.hipotecario.canal.homebanking.negocio;

public enum EnumErrores {

	ERROR_REFRESS_TOKEN("E001", "Error generado al actualizar el refresh token por biometria"), ERROR_LOGIN_BIOMETRIA("E002", "Error generado al loguearse el usuario por Biometria"), LOGIN_BIOMETRIA_METODO("L003", "Logueado con Biometria. Metodo: ");

	private final String codigo;
	private final String valor;

	EnumErrores(String codigo, String valor) {
		this.codigo = codigo;
		this.valor = valor;
	}

	public String getCodigo() {
		return codigo;
	}

	public String getValor() {
		return valor;
	}

}
