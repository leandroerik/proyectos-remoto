package ar.com.hipotecario.canal.officebanking.enums.recaudaciones.cobranzaIntegral;

public enum EnumTipoDocumentoCobranzaIntegral {
    CI(0), CUIT(80), CUIL(86), CDI(87), LE(89), LC(90), PASAPORTE(94), DNI(96);
    private final Integer codigo;

    EnumTipoDocumentoCobranzaIntegral(Integer codigo) {
        this.codigo = codigo;
    }

    public Integer getCodigo() {
        return codigo;
    }
}
