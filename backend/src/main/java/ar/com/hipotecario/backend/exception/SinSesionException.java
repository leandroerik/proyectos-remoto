package ar.com.hipotecario.backend.exception;

public class SinSesionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/* ========== CONSTRUCTORES ========== */
	public SinSesionException() {
	}

	/* ========== METODOS ========== */
	public static void throwIf(Boolean condicion) {
		if (condicion) {
			throw new SinSesionException();
		}
	}
}
