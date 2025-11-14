package ar.com.hipotecario.canal.homebanking.enums.sucursalvirtual;

public enum Productos {
    PRESTAMO_PERSONAL("2", "PRÉSTAMO PERSONAL", "prestamoPersonal"),
    TARJETA_CREDITO_VISA("5","TARJETA DE CRÉDITO VISA", ""),
    CUENTA_CORRIENTE_ACUERDO("7", "CUENTA CORRIENTE CON ACUERDO", "cuentaCorriente"),
    CAJA_AHORRO_PESOS("8", "CAJA DE AHORROS", "cajaAhorro"),
    CAJA_AHORRO_DOLARES("9", "CAJA DE AHORROS EN DÓLARES", "cajaAhorro"),
    CAJA_SEGURIDAD("12", "CAJA DE SEGURIDAD", "cajaSeguridad"),
    INCLUSION_COTITULAR_CUENTA("19", "INCLUSIÓN COTITULAR CUENTA", "inclusionModificacion"),
    PAQUETE_PRODUCTO_NUEVO("32", "PAQUETE DE PRODCUTOS (NUEVO)", ""),
    ADICION_COTITULARES("33", "ADICION DE COTITULARES", "adicionCotitulares"),
    ACUERDO_CUENTA_CORRIENTE("36", "ACUERDO CUENTA CORRIENTE", "acuerdoCuentaCorriente");
    private final String id;
    private final String descripcion;
    private final String tipoRequest;

    Productos(String id, String descripcion, String tipoRequest) {
        this.id = id;
        this.descripcion = descripcion;
        this.tipoRequest = tipoRequest;
    }

    public String getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getTipoRequest() {
        return tipoRequest;
    }

    public static String getTipoPorId(String id) {
        for (Productos item : Productos.values()) {
            if (item.getId().equals(id)) {
                return item.getTipoRequest();
            }
        }
        return "";
    }
}
