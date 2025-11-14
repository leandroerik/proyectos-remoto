package ar.com.hipotecario.canal.officebanking.enums.pagosMasivos;

public enum EnumMediosPagoPAPOB {
    CHEQUE("002"), CHEQUE_DIFERIDO("003"), TRANSFERENCIA("009"), ECHEQ("012"), ECHEQ_DIFERIDO("013");


    private final String codigo;

    EnumMediosPagoPAPOB(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    @Override
    public String toString() {
        return this.name();
    }
}
