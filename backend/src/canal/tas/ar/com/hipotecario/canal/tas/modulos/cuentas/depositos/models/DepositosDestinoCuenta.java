package ar.com.hipotecario.canal.tas.modulos.cuentas.depositos.models;

public class DepositosDestinoCuenta {
    public Integer KioscoId;
    public Long DepositoId;
    public String CodigoCliente;
    public String TipoCuenta;
    public String NumeroCuenta;
    public String IdentificadorProducto;
    public String NumeroCuentaCOBIS;

    public DepositosDestinoCuenta(Integer kioscoId, Long depositoId, String codigoCliente, String tipoCuenta,
            String numeroCuenta,
            String identificadorProducto, String numeroCuentaCOBIS) {
        KioscoId = kioscoId;
        DepositoId = depositoId;
        CodigoCliente = codigoCliente;
        TipoCuenta = tipoCuenta;
        NumeroCuenta = numeroCuenta;
        IdentificadorProducto = identificadorProducto;
        NumeroCuentaCOBIS = numeroCuentaCOBIS;
    }

    public Long getDepositoId() {
        return DepositoId;
    }

    public void setDepositoId(Long depositoId) {
        DepositoId = depositoId;
    }

    public String getCodigoCliente() {
        return CodigoCliente;
    }

    public void setCodigoCliente(String codigoCliente) {
        CodigoCliente = codigoCliente;
    }

    public String getTipoCuenta() {
        return TipoCuenta;
    }

    public void setTipoCuenta(String tipoCuenta) {
        TipoCuenta = tipoCuenta;
    }

    public String getNumeroCuenta() {
        return NumeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        NumeroCuenta = numeroCuenta;
    }

    public String getIdentificadorProducto() {
        return IdentificadorProducto;
    }

    public void setIdentificadorProducto(String identificadorProducto) {
        IdentificadorProducto = identificadorProducto;
    }

    public String getNumeroCuentaCOBIS() {
        return NumeroCuentaCOBIS;
    }

    public void setNumeroCuentaCOBIS(String numeroCuentaCOBIS) {
        NumeroCuentaCOBIS = numeroCuentaCOBIS;
    }

    public Integer getKioscoId() {
        return KioscoId;
    }

    public void setKioscoId(Integer kioscoId) {
        KioscoId = kioscoId;
    }

}
