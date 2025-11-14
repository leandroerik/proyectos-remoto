package ar.com.hipotecario.canal.officebanking.enums.planSueldo.nomina;

public enum EnumTipoCuitCuilNominaOB {
    CUIL("08"), CUIT("11");

    private final String codigo;

    EnumTipoCuitCuilNominaOB(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }

    public static String obtenerNamePorId(String id) {
        for (EnumTipoCuitCuilNominaOB tipo : values()) {
            if (tipo.codigo.equals(id)) return tipo.name();
        }
        return "";
    }
}
