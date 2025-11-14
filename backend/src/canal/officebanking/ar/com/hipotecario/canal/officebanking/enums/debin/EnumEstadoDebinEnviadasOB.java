package ar.com.hipotecario.canal.officebanking.enums.debin;

public enum EnumEstadoDebinEnviadasOB {
	INICIADO(1), ACREDITADO(2), VENCIDO(3), RECHAZADO(4);
	
	
	private final int codigo;

	EnumEstadoDebinEnviadasOB(int codigo) {
		this.codigo = codigo;
	}

	public int getCodigo() {
		return codigo;
	}
}
