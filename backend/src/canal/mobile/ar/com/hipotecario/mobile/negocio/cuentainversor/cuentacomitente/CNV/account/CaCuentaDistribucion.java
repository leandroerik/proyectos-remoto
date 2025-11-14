package ar.com.hipotecario.mobile.negocio.cuentainversor.cuentacomitente.CNV.account;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaCuentaDistribucion {
    private String tipoIdentificadorCuenta; // accountIdentifierType
    private String identificador; // identifier

    public CaCuentaDistribucion() {
    }

    public CaCuentaDistribucion(String tipoIdentificadorCuenta, String identificador) {
        this.tipoIdentificadorCuenta = tipoIdentificadorCuenta;
        this.identificador = identificador;
    }
    public String getTipoIdentificadorCuenta() {
        return tipoIdentificadorCuenta;
    }
    public void setTipoIdentificadorCuenta(String tipoIdentificadorCuenta) {
        this.tipoIdentificadorCuenta = tipoIdentificadorCuenta;
    }
    public String getIdentificador() {
        return identificador;
    }
    public void setIdentificador(String identificador) {
        this.identificador = identificador;
    }
}
