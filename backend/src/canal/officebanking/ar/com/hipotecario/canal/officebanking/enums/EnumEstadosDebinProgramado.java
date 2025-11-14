package ar.com.hipotecario.canal.officebanking.enums;
public enum EnumEstadosDebinProgramado {
    ALTA_SUSCRIPCION("ALTA"), BAJA_SUSCRIPCION("BAJA"),ADHESION("ADHESION");

    private final String codigo;

    EnumEstadosDebinProgramado(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
