package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumMonedasOB {

	PESOS(80), DOLARES(2), EURO(98);

	private final int moneda;

	EnumMonedasOB(int moneda) {
		this.moneda = moneda;
	}

	public int getMoneda() {
		return moneda;
	}

}