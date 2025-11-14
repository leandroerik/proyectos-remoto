package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumMulticausalAcreditacionesOB {
	SUELDOS("001"), AGUINALDOS("002"), HONORARIOS("005"), VIATICOS("019"), JUBILACIONES("020"), INDEMNIZACIONES("021"), FONDO_CESE_LABORAL("022"), BECAS_ESTUDIOS("023"), ASIGNACIONES_FAMILIARES("026"),DECRETO_VDOTUERTO("027"),HABERES_QUINCENALES("029");

	private final String codigo;

	EnumMulticausalAcreditacionesOB(String codigo) {
		this.codigo = codigo;
	}

	public String getCodigo() {
		return codigo;
	}
}
