package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumEstadoOrdenPagoComexOB {

	EN_BANDEJA(1), EXITO(2), RECHAZADO(3), PENDIENTE_AUTORIZACION(4);

	private int codigo;

	EnumEstadoOrdenPagoComexOB(int codigo) {
		this.codigo = codigo;
	}

	public int getCodigo() {
		return codigo;
	}
}
