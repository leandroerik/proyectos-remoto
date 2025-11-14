package ar.com.hipotecario.canal.officebanking.enums.planSueldo.nomina;

public enum EnumSituacionLaboralNominaOB {
    EFECTIVO(1), CONTRATADO(2);

    private final int codigo;

    EnumSituacionLaboralNominaOB(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static String obtenerNamePorId(int id) {
        for (EnumSituacionLaboralNominaOB s : values()) {
            if (s.codigo == id) return s.name();
        }
        return "Situacion laboral inválida";
    }
}
