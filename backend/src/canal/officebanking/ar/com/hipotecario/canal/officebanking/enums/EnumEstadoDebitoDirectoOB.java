package ar.com.hipotecario.canal.officebanking.enums;


public enum EnumEstadoDebitoDirectoOB {
	EN_BANDEJA(1), A_PROCESAR(2),  PROCESADO(3), RECHAZADO(4);

    private final int codigo;

    EnumEstadoDebitoDirectoOB(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }
}
