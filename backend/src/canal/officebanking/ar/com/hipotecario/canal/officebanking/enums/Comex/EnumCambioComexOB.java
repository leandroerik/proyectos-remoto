package ar.com.hipotecario.canal.officebanking.enums.Comex;

public enum EnumCambioComexOB {
    BANCO_HIPOTECARIO("bh"),CLIENTE_BANCO_HIPOTECARIO("cbh"),CORREDOR_BANCO_HIPOTECARIO("ccbh"), SC("s/c");

    private final String codigo;
    EnumCambioComexOB(String codigo){this.codigo = codigo;}
    public String getCodigo(){return codigo;}

    public static EnumCambioComexOB getByCodigo(String codigo){
        for (EnumCambioComexOB cambio : values()){
            if (cambio.getCodigo().equalsIgnoreCase(codigo)){
                return cambio;
            }
        }
        throw new IllegalArgumentException("Código no válido: " + codigo);
    }
}
