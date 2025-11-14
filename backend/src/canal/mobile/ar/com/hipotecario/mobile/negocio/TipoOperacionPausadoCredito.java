package ar.com.hipotecario.mobile.negocio;

 public enum TipoOperacionPausadoCredito {
    PAUSAR("DISABLED"), HABILITAR("ENABLED");

    private String value;

    TipoOperacionPausadoCredito(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return this.getValue();
    }

    public static TipoOperacionPausadoCredito getEnum(String value) {
        if (value == null)
            return null;
        for (TipoOperacionPausadoCredito v : values())
            if (value.equalsIgnoreCase(v.getValue()))
                return v;
        return null;
    }


 }
