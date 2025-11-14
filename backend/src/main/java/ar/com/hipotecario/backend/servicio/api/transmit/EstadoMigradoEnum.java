package ar.com.hipotecario.backend.servicio.api.transmit;

public enum EstadoMigradoEnum {

    MIGRAR("0", "Usuario a Migrar"),
    MIGRADO("1", "Usuario Migrado"),
    NOMIGRA("2", "No Migra"),
    ERROR_MIGRAR("3", "Error al Migrar");

    public String codigo;
    public String descripcion;

    EstadoMigradoEnum(String codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public static EstadoMigradoEnum codigo(String codigo) {
        for (EstadoMigradoEnum e : values())
            if (e.getCodigo().equals(codigo))
                return e;

        throw new IllegalArgumentException(String.valueOf(codigo));
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esNoMigra() {
        return this.equals(NOMIGRA);
    }

    public boolean esMigrado() {
        return this.equals(MIGRADO);
    }

    public boolean esParaMigrar() {
        return this.equals(MIGRAR);
    }

    public boolean esErrorAlMigrar() {
        return this.equals(ERROR_MIGRAR);
    }
}