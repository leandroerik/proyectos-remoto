package ar.com.hipotecario.canal.officebanking.enums.planSueldo.nomina;

public enum EnumOcupacionNominaOB {
    FUTBOLISTA(11300), MILITAR_POLICIA_SEGURIDAD_VIGILANCIA(11800), EMPLEADO(11900), CAJERO(20005), ALBAÑIL(20500), OPERARIO(20600),
    ARQUITECTO(30201), CONTADOR_PÚBLICO(30303), LIC_PSICOLOGÍA(30505), ABOGADO(30701), FARMACÉUTICO(30801), MÉDICO(31101),
    KINESIÓLOGO(31103), ODONTÓLOGO(31201), MAESTRO(31501), PROFESOR(31502), NO_TIENE(40001);

    private final int codigo;

    EnumOcupacionNominaOB(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static String obtenerNamePorId(int id) {
        for (EnumOcupacionNominaOB ocupaciones : values()) {
            if (ocupaciones.codigo == id) return ocupaciones.name();
        }
        return "";
    }

}
