package ar.com.hipotecario.canal.officebanking.enums.recaudaciones;

public enum EnumTipoPagosReca {

     EFECTIVO(2,"EF"), ECHEQ(3,"EC"), TRANSFERENCIA(4,"TR"),DEBIN_POR_LOTE(5,"DE");
    private final Integer codigo;
    private final String referencia;

    EnumTipoPagosReca(Integer codigo, String referencia) {
        this.codigo = codigo;this.referencia=referencia;
    }

    public Integer getCodigo() {
        return codigo;
    }

    public String getReferencia() {
        return referencia;
    }

    public static boolean isValidCodigo(Integer tipoPago) {
        for (EnumTipoPagosReca tipo : EnumTipoPagosReca.values()) {
            if (tipo.getCodigo().equals(tipoPago)) {
                return true;
            }
        }
        return false;
    }
}
