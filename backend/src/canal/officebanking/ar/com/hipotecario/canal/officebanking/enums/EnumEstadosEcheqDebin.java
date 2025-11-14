package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumEstadosEcheqDebin {
    DEPOSITADO("D"), RECHAZADO("R"), PAGADO("P"), PRESENTADO("S"), ACTIVO("A"), ACTIVO_PENDIENTE("N"), CUSTODIA("C"), DEVOLUCION_PENDIENTE("L"), EMITIDO_PENDIENTE("M"), ANULADO("U"), REPUDIADO("E"), CADUCADO("CAD");

    private final String codigo;

    EnumEstadosEcheqDebin(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public static boolean isValidarEstado(String estadoCheque) {
        for (EnumEstadosEcheqDebin tipo : EnumEstadosEcheqDebin.values()) {
            if (tipo.getCodigo().equals(estadoCheque)) {
                return true;
            }
        }
        return false;
    }
}


