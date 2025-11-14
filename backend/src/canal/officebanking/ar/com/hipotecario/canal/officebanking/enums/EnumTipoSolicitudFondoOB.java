package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumTipoSolicitudFondoOB {
    TIPO_SOLICITUD_SUSCRIPCION("Suscripcion"),
    TIPO_SOLICITUD_RESCATE("Rescate");

    private final String tipoSolicitud;

    EnumTipoSolicitudFondoOB(String tipoSolicitud){
        this.tipoSolicitud = tipoSolicitud;
    }

    public String getTipoSolicitud() {
        return tipoSolicitud;
    }

    public static EnumTipoSolicitudFondoOB getTipoSolicitud(String tipo) {
        for (EnumTipoSolicitudFondoOB e : values()) {
            if (e.getTipoSolicitud().equalsIgnoreCase(tipo)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Tipo de solicitud no válido: " + tipo);
    }
}
