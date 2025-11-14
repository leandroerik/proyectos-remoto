package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumEstadosRegistrosAcreditacionesOB {
	DEBITADA_CBU("D"), ELIMINADA("E"), LIQUIDADA_BH("L"), PROCESADA("P"), RECHAZADA("R"), REVERSADA("W"), A_PROCESAR("I");

	private final String codigo;

	EnumEstadosRegistrosAcreditacionesOB(String codigo) {
		this.codigo = codigo;
	}

	public String getCodigo() {
		return codigo;
	}

	public static String obtenerPorCodigo(String codigo) {
		for (EnumEstadosRegistrosAcreditacionesOB e : values()) {
			if (e.codigo.equals(codigo))
				return e.name().replaceAll("_", " ");
		}
		return "UNKNOWN";
	}

}
