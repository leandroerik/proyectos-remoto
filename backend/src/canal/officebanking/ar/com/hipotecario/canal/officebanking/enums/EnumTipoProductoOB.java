package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumTipoProductoOB {

    PLAN_SUELDO(1),
    PAGO_PROVEEDORES(2),
    TRANSFERENCIAS(4),
    DEBITO_DIRECTO(6),
    DEBIN(7),
    FCI(8),
    PAGOS_VEP(9),
    ECHEQ(12),
    PAGO_SERVICIOS(13),
    NOMINA(14),
    PERFIL_INVERSOR(15),
    CHEQUERA_ELECTRONICA(16),
    COBRANZA_INTEGRAL(17),
    DEPOSITO_REMOTO(18),
    DEBIN_PROGRAMADO(20),
    CEDIP(100),
    COMERCIO_EXTERIOR(19),
    PLAZO_FIJO(102),
    ECHEQ_DESCUENTO(103),
    DEBIN_LOTE(104),
    BENEFICIARIOS(999),
    PAGO_TARJETA(105),
    STOP_DEBIT(106);

    private final int cod_prod_firma;

    EnumTipoProductoOB(int cod_prod_firma) {
        this.cod_prod_firma = cod_prod_firma;
    }

    public int getCodigo() {
        return cod_prod_firma;
    }

    public static EnumTipoProductoOB getByCodigo(int codigo) {
        for (EnumTipoProductoOB e : EnumTipoProductoOB.values()) {
            if (e.cod_prod_firma == codigo) {
                return e;
            }
        }
        return null;
    }
}
