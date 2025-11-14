package ar.com.hipotecario.canal.officebanking.enums;

public enum EnumEstadosEcheqOB {

    EN_BANDEJA(1), PENDIENTE(2), CREADO(3), RECHAZADO(4);

    private final int codigo;

    EnumEstadosEcheqOB(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

}