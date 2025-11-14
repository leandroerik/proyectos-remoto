package ar.com.hipotecario.canal.officebanking.enums.debin;

public enum EnumDebinVencimientoOB {
	VTO_1(1), VTO_3(3), VTO_6(6), VTO_12(12), VTO_24(24), VTO_48(48), VTO_72(72);
	
	private final int codigo;

	EnumDebinVencimientoOB(int codigo) {
		this.codigo = codigo;
	}

	public int getCodigo() {
		return codigo;
	}
}
