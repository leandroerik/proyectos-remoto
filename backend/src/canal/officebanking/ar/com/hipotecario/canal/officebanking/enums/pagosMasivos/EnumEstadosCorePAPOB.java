package ar.com.hipotecario.canal.officebanking.enums.pagosMasivos;

public enum EnumEstadosCorePAPOB {
    PENDIENTE(1), EN_PROCESO(2), EMITIDO(3), RECHAZADO(4), ANULADO(5), EXITOSA(6), VARIOS(8);

    private final int codigo;

    EnumEstadosCorePAPOB(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }
    public static EnumEstadosCorePAPOB fromCodigo(int codigo) {
        for (EnumEstadosCorePAPOB estado : EnumEstadosCorePAPOB.values()) {
            if (estado.getCodigo() == codigo) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Código inválido: " + codigo);
    }
    public static Integer getCodigoPorTexto(String texto) {
        for (EnumEstadosCorePAPOB estado : EnumEstadosCorePAPOB.values()) {
            if (estado.name().equalsIgnoreCase(texto)) {
                return estado.getCodigo();
            }
        }
        return null;
    }
}

