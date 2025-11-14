package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumEstadoNominaOB {

	PROCESADO_SIN_OBSERVACIONES("PROCESADO SIN OBSERVACIONES"), PROCESADO_CON_OBSERVACIONES("PROCESADO CON OBSERVACIONES");

	private final String codigo;

	EnumEstadoNominaOB(String codigo) {
		this.codigo = codigo;
	}

	public String getCodigo() {
		return codigo;
	}

}