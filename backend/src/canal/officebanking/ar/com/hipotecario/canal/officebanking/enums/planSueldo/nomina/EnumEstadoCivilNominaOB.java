package ar.com.hipotecario.canal.officebanking.enums.planSueldo.nomina;

public enum EnumEstadoCivilNominaOB {
    SOLTERO("S"), CASADO("C"), DIVORCIADO("D"), VIUDO("V"), SEPARADO_JUDICIALMENTE("O");

    private final String codigo;

    EnumEstadoCivilNominaOB(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public static String obtenerNamePorId(String id) {
        for (EnumEstadoCivilNominaOB estadosCiviles : values()) {
            if (estadosCiviles.codigo.equals(id)) return estadosCiviles.name();
        }
        return "";
    }
}
