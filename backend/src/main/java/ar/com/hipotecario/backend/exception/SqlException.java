package ar.com.hipotecario.backend.exception;

public class SqlException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/* ========== ATRIBUTOS ========== */
	public String codigoError = "ERROR";
	public String query = "";
	public Object[] parametros = {};

	/* ========== CONSTRUCTORES ========== */
	public SqlException(Throwable cause) {
		super(cause);
	}

	public SqlException(Throwable cause, String query, Object... parametros) {
		super(cause);
		this.query = query;
		this.parametros = parametros;
	}

	public SqlException(String codigoError) {
		super(codigoError);
		this.codigoError = codigoError;
	}

	/* ========== METODOS ========== */
	public static void throwIf(String codigoError, Boolean condicion) {
		if (condicion) {
			throw new SqlException(codigoError);
		}
	}
}
