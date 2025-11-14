package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumEstadoPagosHaberesOB {
	EN_BANDEJA(1), A_PROCESAR(2), PROCESADO(3), RECHAZADO(4), PROCESADO_PARCIALMENTE(5), ESPERANDO_FONDEO(6);

	private final int codigo;

	EnumEstadoPagosHaberesOB(int codigo) {
		this.codigo = codigo;
	}

	public int getCodigo() {
		return codigo;
	}
}
