package ar.com.hipotecario.mobile.negocio;

public enum Enumlog {

	ERROR_REFRESS_TOKEN("E001", "Error generado al actualizar el refresh token por biometria"), ERROR_LOGIN_BIOMETRIA("E002", "Error generado al loguearse el usuario por Biometria"), LOG_BUHOFACIL("L001", "Logueado con Buhofacil"), LOG_BIOMETRIA_HUELLA("L002", "Logueado con Biometria"), LOG_BIOMETRIA_ROSTRO("L003", "Logueado con Biometria"), LOG_BIOMETRIA_ACCESO("A001", "Login"), LOG_BOBLEFACTOR_ACCESO("A002", "Doble Factor");

	public final String codigo;
	public final String valor;

	Enumlog(String codigo, String valor) {
		this.codigo = codigo;
		this.valor = valor;
	}

	public static String getValor(String codigo) {

		for (Enumlog e : values()) {
			if (e.codigo.equalsIgnoreCase(codigo)) {
				return e.valor;
			}
		}
		return "";
	}

	public static String getCodigo(String valor) {
		for (Enumlog e : values()) {
			if (e.valor.equalsIgnoreCase(valor)) {
				return e.codigo;
			}
		}
		return "";
	}

}
