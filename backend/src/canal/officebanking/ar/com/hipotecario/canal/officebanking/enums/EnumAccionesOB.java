package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumAccionesOB {
	FIRMAR(1), RECHAZAR(2), CREAR(3), NO_APLICA(4), APROBAR(5);

	private final int codigo;

	EnumAccionesOB(int codigo) {
		this.codigo = codigo;
	}

	public int getCodigo() {
		return codigo;
	}
}