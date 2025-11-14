package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumCuentasOB {
	CUENTA_UNIPERSONAL("UNIPERSONAL"), CUENTA_EMPRESA("EMPRESA"), TIPO_TITULARIDAD("T");
	
	private final String codigo;

	EnumCuentasOB(String codigo) {
		this.codigo = codigo;
	}

	public String getCodigo() {
		return codigo;
	}

}
