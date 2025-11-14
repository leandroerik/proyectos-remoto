package ar.com.hipotecario.backend.exception;

public class MaximoReintentosException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/* ========== CONSTRUCTORES ========== */
	public MaximoReintentosException() {
	}

	/* ========== METODOS ========== */
	public static void throwIf(Boolean condicion) {
		if (condicion) {
			throw new MaximoReintentosException();
		}
	}
}
