package ar.com.hipotecario.canal.officebanking.enums.pagoTarjeta;

public enum EnumEstadoPagoTarjetaOB {
    EN_BANDEJA(1), EXITO(2), PAGO_RECHAZADO(3), RECHAZADO_DE_FIRMA(4), PENDIENTE(5), PARCIALMENTE_FIRMADO(6), FIRMA_COMPLETA(7);

    private final int codigo;

    EnumEstadoPagoTarjetaOB(int codigo) {
        this.codigo = codigo;
    }
    public int getCodigo() {
        return codigo;
    }
}
