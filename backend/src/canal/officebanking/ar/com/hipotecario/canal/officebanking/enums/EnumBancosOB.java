package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumBancosOB {

	BH("Banco Hipotecario");

	private final String nombre;

	EnumBancosOB(String nombre) {
		this.nombre = nombre;
	}

	public String getNombre() {
		return nombre;
	}
}