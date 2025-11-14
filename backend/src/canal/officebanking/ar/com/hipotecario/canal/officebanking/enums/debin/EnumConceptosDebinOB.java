package ar.com.hipotecario.canal.officebanking.enums.debin;

public enum EnumConceptosDebinOB {
    ALQUILERES(1), CUOTAS(2), EXPENSAS(3), FACTURAS(4), HONORARIOS(5), PRESTAMOS(6), SEGUROS(7), VARIOS(8), HABERES(9);

    private final int codigo;

    EnumConceptosDebinOB(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }
}
