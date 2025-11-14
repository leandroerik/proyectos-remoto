package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumEstadoTRNOB {

	A_PROCESAR(1), EN_PROCESO(2), PROGRAMADA(3), EXITO(4), RECHAZADO(5), EN_BANDEJA(6);

	private final int codigo;

	EnumEstadoTRNOB(int codigo) {
		this.codigo = codigo;
	}

	public int getCodigo() {
		return codigo;
	}

}