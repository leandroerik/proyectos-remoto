package ar.com.hipotecario.canal.officebanking.enums.Comex;

public enum EnumCondicionComexOB {

    TITULAR("t"),APODERADO("a"), SC("s/c");
    private final String codigo;

    EnumCondicionComexOB(String codigo){this.codigo = codigo;}
    public String getCodigo(){return codigo;}
    public static EnumCondicionComexOB getByCodigo(String codigo){
        for (EnumCondicionComexOB condicion : values()) {
            if (condicion.getCodigo().equalsIgnoreCase(codigo)) {
                return condicion;
            }
        }
        throw new IllegalArgumentException("Código no válido: " + codigo);
    }

}
