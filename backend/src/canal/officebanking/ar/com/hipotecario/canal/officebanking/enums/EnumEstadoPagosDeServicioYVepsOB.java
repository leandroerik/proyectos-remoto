package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumEstadoPagosDeServicioYVepsOB {

	EN_BANDEJA(1), PAGADO(2), RECHAZADO(3), PENDIENTE_CONTROL(4), DISPONIBLE(5);

	private int codigo;

	EnumEstadoPagosDeServicioYVepsOB(int codigo) {
		this.codigo = codigo;
	}

	public int getCodigo() {
		return codigo;
	}
}