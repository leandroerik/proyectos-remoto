package ar.com.hipotecario.backend.servicio.api.transmit;

public enum NemonicosUsuarioTrasnmitEnum {
    MONOPRODUCTO("monoproducto"), CLIENTEEXTERIOR("en_extranjero"), TARJETADEBITOVIRTUAL("con_tarj_deb_virt"), NUEVO("es_nuevo"), NORMAL("normal");

    private String descripcion;

    public String getDescripcion() {
        return descripcion;
    }

    NemonicosUsuarioTrasnmitEnum(String descripcion) {
        this.descripcion = descripcion;
    }

    public static NemonicosUsuarioTrasnmitEnum fromDescripcion(String descripcion) {
        for (NemonicosUsuarioTrasnmitEnum nemonico : values())
            if (nemonico.descripcion.equalsIgnoreCase(descripcion))
                return nemonico;
        throw new IllegalArgumentException("Nemonico no encontrado: " + descripcion);
    }
}