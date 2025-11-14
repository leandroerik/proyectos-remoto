package ar.com.hipotecario.canal.officebanking.enums.debin;

public enum EnumEstadoDebinRecibidasOB {
	ACEPTAR_O_RECHAZAR_DEBIN(1), EN_BANDEJA(2), REALIZADO(3), VENCIDO(4), RECHAZADO(5);
	
	private final int codigo;

	EnumEstadoDebinRecibidasOB(int codigo) {
		this.codigo = codigo;
	}

	public int getCodigo() {
		return codigo;
	}
}
