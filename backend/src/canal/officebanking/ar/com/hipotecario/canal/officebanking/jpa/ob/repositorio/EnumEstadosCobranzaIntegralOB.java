package ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio;

public enum EnumEstadosCobranzaIntegralOB {
	EN_BANDEJA(1), A_PROCESAR(2),  PROCESADO(3), RECHAZADO(4);

    private final int codigo;

    EnumEstadosCobranzaIntegralOB(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }
}

