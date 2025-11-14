package ar.com.hipotecario.mobile.enums.sucursalvirtual;

public enum Productos {
    PRESTAMO_PERSONAL("2", "PRÉSTAMO PERSONAL"),
    TARJETA_CREDITO_VISA("5","TARJETA DE CRÉDITO VISA"),
    CUENTA_CORRIENTE_ACUERDO("7", "CUENTA CORRIENTE CON ACUERDO"),
    PAQUETE_PRODUCTO_NUEVO("32", "PAQUETE DE PRODCUTOS (NUEVO)");
    private final String id;
    private final String descripcion;

    Productos(String id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }

    public String getId() {
        return id;
    }

    public Integer getIdToInteger() {
        return Integer.valueOf(this.getId());
    }

    public String getDescripcion() {
        return descripcion;
    }


}
