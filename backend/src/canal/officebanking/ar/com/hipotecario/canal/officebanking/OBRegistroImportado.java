package ar.com.hipotecario.canal.officebanking;

import java.util.List;

public class OBRegistroImportado {
	private Object error;
	private List<OBErrorMessage> errores;
	private boolean isCorrupted;
	private Integer NumLinea;

	public List<OBErrorMessage> getErrores() {
		return errores;
	}

	public Object getError() {
		return error;
	}

	public void setError(Object error) {
		this.error = error;
	}

	public void setErrores(List<OBErrorMessage> errores) {
		this.errores = errores;
	}

	public boolean isCorrupted() {
		return isCorrupted;
	}

	public void setCorrupted(boolean isCorrupted) {
		this.isCorrupted = isCorrupted;
	}

	public Integer getNumLinea() {
		return NumLinea;
	}

	public void setNumLinea(Integer numLinea) {
		NumLinea = numLinea;
	}

}
