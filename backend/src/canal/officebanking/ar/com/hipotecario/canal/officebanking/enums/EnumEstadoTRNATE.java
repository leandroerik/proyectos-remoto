package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumEstadoTRNATE {

	VALOR_AL_COBRO(40), RECHAZADA_BCO_DEBITO(70), RECHAZO_BCO_CREDITO(80), RECHAZO_POR_INHABILITACION_CTA_CREDITO(90);

	
	
	private final int codigo;

	EnumEstadoTRNATE(int codigo) {
		this.codigo = codigo;
	}

	public int getCodigo() {
		return codigo;
	}
}
