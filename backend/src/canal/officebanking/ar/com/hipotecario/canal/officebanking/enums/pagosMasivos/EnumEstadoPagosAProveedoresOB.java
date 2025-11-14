package ar.com.hipotecario.canal.officebanking.enums.pagosMasivos;

public enum EnumEstadoPagosAProveedoresOB {
    EN_BANDEJA(1), PENDIENTE(2), ENVIADO_BANCO(3), PENDIENTE_AUTORIZACION(4), PROCESADO(5), RECHAZADO(6);

	private final int codigo;

	EnumEstadoPagosAProveedoresOB(int codigo) {
		this.codigo = codigo;
	}

	public int getCodigo() {
		return codigo;
	}
}
