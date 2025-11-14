package ar.com.hipotecario.backend.servicio.api.transmit;

public enum OpcionMigradoEnum {

    CONSULTAR(1, "Consultar"),
    ACTUALIZAR(2, "Actualizar"),
    INSERTAR_ERROR(3, "Insertar Error");

    public Integer codigo;
    public String descripcion;

    OpcionMigradoEnum(Integer codigo, String descripcion) {
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public static OpcionMigradoEnum codigo(int codigo) {
        for (OpcionMigradoEnum e : values())
            if (e.getCodigo() == codigo)
                return e;

        throw new IllegalArgumentException(String.valueOf(codigo));
    }

    public int getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

}
