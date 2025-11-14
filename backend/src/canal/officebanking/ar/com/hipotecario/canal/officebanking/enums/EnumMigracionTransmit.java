package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumMigracionTransmit {
	MIGRAR(0), MIGRADO(1), NO_MIGRADO(2);

	private final int codigo;

	EnumMigracionTransmit(int codigo) {
		this.codigo = codigo;
	}

	public int getCodigo() {
		return codigo;
	}
}