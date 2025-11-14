package ar.com.hipotecario.canal.officebanking.enums.echeq;

public enum EnumEstadoEcheqChequeraOB {
    EN_BANDEJA(1), PENDIENTE(2), CREADO(3), RECHAZADO(4);

    private final int codigo;

    EnumEstadoEcheqChequeraOB(int codigo) {
        this.codigo = codigo;
    }
    public int getCodigo() {
        return codigo;
    }
}
