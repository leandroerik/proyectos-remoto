package ar.com.hipotecario.mobile.enums.sucursalvirtual;

public enum EstadoGestion {
    CERO("0"),
    ESTADO_ACTIVA("1"),
    ESTADO_INACTIVA("2"),
    ESTADO_FAVORABLE("3"),
    ESTADO_DESFAVORABLE_FECHA("4"),
    ESTADO_DESFAVORABLE_SISTEMA("5"),
    GESTION_ABIERTA("2"),
    ;

    private final String mensaje;

    EstadoGestion(String mensaje) {
        this.mensaje = mensaje;
    }

    @Override
    public String toString() {
        return this.mensaje;
    }

    public Integer toInteger() {
        return Integer.valueOf(this.mensaje);
    }



}
