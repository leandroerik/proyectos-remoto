package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumEstadoSolicitudFCIOB {

	PENDIENTE(1), RECHAZADA(2), EN_PROCESO(3), REALIZADA(4);

	private final int codigo;

	EnumEstadoSolicitudFCIOB(int codigo) {
		this.codigo = codigo;
	}

	public int getCodigo() {
		return codigo;
	}
}