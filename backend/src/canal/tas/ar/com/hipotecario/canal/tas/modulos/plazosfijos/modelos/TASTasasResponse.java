package ar.com.hipotecario.canal.tas.modulos.plazosfijos.modelos;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

import java.math.BigDecimal;

public class TASTasasResponse extends ApiObjeto {

    private BigDecimal orden;
    private String    codigoMoneda;
    private String     idTipoDeposito;
    private String     descTipoDeposito;
    private Integer    plazoMinimo;
    private Integer    plazoMaximo;
    private BigDecimal montoMinimo;
    private BigDecimal montoMaximo;
    private BigDecimal valorTasa;

    public TASTasasResponse() {
    }

    public BigDecimal getOrden() {
        return orden;
    }

    public void setOrden(BigDecimal orden) {
        this.orden = orden;
    }

    public String getCodigoMoneda() {
        return codigoMoneda;
    }

    public void setCodigoMoneda(String codigoMoneda) {
        this.codigoMoneda = codigoMoneda;
    }

    public String getIdTipoDeposito() {
        return idTipoDeposito;
    }

    public void setIdTipoDeposito(String idTipoDeposito) {
        this.idTipoDeposito = idTipoDeposito;
    }

    public String getDescTipoDeposito() {
        return descTipoDeposito;
    }

    public void setDescTipoDeposito(String descTipoDeposito) {
        this.descTipoDeposito = descTipoDeposito;
    }

    public Integer getPlazoMinimo() {
        return plazoMinimo;
    }

    public void setPlazoMinimo(Integer plazoMinimo) {
        this.plazoMinimo = plazoMinimo;
    }

    public Integer getPlazoMaximo() {
        return plazoMaximo;
    }

    public void setPlazoMaximo(Integer plazoMaximo) {
        this.plazoMaximo = plazoMaximo;
    }

    public BigDecimal getMontoMinimo() {
        return montoMinimo;
    }

    public void setMontoMinimo(BigDecimal montoMinimo) {
        this.montoMinimo = montoMinimo;
    }

    public BigDecimal getMontoMaximo() {
        return montoMaximo;
    }

    public void setMontoMaximo(BigDecimal montoMaximo) {
        this.montoMaximo = montoMaximo;
    }

    public BigDecimal getValorTasa() {
        return valorTasa;
    }

    public void setValorTasa(BigDecimal valorTasa) {
        this.valorTasa = valorTasa;
    }
}
