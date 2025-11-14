package ar.com.hipotecario.canal.officebanking.enums.pagosMasivos;

public enum EnumEstadosOrdenesPagoOB {
    EN_BANDEJA(1), ENVIADA(2), SIN_RETIRAR(3), PROCESADA(4), RECHAZADA(5), PAGO_ANULADO(6);

    private final int codigo;

    EnumEstadosOrdenesPagoOB(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }
}