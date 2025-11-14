package ar.com.hipotecario.backend.exception;

public class SesionExpiradaException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/* ========== CONSTRUCTORES ========== */
	public SesionExpiradaException() {
	}

	/* ========== METODOS ========== */
	public static void throwIf(Boolean condicion) {
		if (condicion) {
			throw new SesionExpiradaException();
		}
	}
}
