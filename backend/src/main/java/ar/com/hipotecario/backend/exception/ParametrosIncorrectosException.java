package ar.com.hipotecario.backend.exception;

public class ParametrosIncorrectosException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/* ========== CONSTRUCTORES ========== */
	public ParametrosIncorrectosException(String clave, Object valor) {
		super(String.format("Parametro [%s]: Valor incorrecto %s", clave, valor));
	}

	/* ========== METODOS ========== */
	public static void throwIf(Boolean condicion, String clave, Object valor) {
		if (condicion) {
			throw new ParametrosIncorrectosException(clave, valor);
		}
	}
}
