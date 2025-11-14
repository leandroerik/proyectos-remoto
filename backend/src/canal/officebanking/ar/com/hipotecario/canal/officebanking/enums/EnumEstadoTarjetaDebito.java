package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumEstadoTarjetaDebito {
	A("Activa"), B("Bloqueada"), G("Impresa");

	private final String descripcion;

	EnumEstadoTarjetaDebito(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getDescripcion() {
		return descripcion;
	}
}
