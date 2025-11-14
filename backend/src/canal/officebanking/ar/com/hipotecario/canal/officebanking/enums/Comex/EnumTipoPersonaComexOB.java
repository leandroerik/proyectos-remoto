package ar.com.hipotecario.canal.officebanking.enums.Comex;

import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;

public enum EnumTipoPersonaComexOB {
    PERSONA_HUMANA("ph"),PERSONA_JURIDICA("pj");
    private final String codigo;

    EnumTipoPersonaComexOB(String codigo){this.codigo=codigo;}
    public String getCodigo(){return codigo;}

    public static EnumTipoPersonaComexOB getByCodigo(String codigo){
        for (EnumTipoPersonaComexOB persona:values()){
            if (persona.getCodigo().equalsIgnoreCase(codigo)){
                return persona;
            }
        }
        throw new IllegalArgumentException("Código no válido: " + codigo);
    }
}
