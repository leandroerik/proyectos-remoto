package ar.com.hipotecario.canal.homebanking.negocio;

public enum EnumMoneda {
    PESOS("80"), DOLARES("2"), EURO("98");

    private final String moneda;

    EnumMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getMoneda() {
        return moneda;
    }

    public static EnumMoneda fromString(String colorName) {
        for (EnumMoneda moneda : EnumMoneda.values()) {
            if (moneda.name().equalsIgnoreCase(colorName))
                return moneda;
        }
        throw new IllegalArgumentException("No existe " + colorName);
    }
}
