package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumEstadoBandejaOB {
	PENDIENTE_AUTORIZACION(1), RECHAZADO_AUTORIZACION(2), PENDIENTE_FIRMA(3), RECHAZADO_EN_FIRMA(4), PARCIALMENTE_FIRMADA(5), FIRMADO_COMPLETO(6),DENY(7);

	private final int codigo;

	EnumEstadoBandejaOB(int codigo) {
		this.codigo = codigo;
	}

	public int getCodigo() {
		return codigo;
	}

	public static String getEstadoDescripcion(int codigo) {
		switch(codigo) {
			case 1:
				return PENDIENTE_AUTORIZACION.toString();
			case 2:
				return RECHAZADO_AUTORIZACION.toString();
			case 3:
				return PENDIENTE_FIRMA.toString();
			case 4:
				return RECHAZADO_EN_FIRMA.toString();
			case 5:
				return PARCIALMENTE_FIRMADA.toString();
			case 6:
				return FIRMADO_COMPLETO.toString();
		}
		
		// TODO Auto-generated method stub
		return null;
	}
}
