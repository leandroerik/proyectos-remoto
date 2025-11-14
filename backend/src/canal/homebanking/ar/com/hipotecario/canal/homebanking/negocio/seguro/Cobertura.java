package ar.com.hipotecario.canal.homebanking.negocio.seguro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Cobertura {

    private String idCoberturaProducto;
    private String descripcionCorta;
    private String idCoberturaEnlatado;
    private String codigoCobertura;
    private String nameCoberturaEnlatado;
    private Integer porcentajeDeDisminucion;
    private String descripcionLarga;
    private List<Object> opciones;
    private String nameCoberturaProducto;
    private Integer porcentajeDeAumento;
    private String idCobertura;
    private Boolean coberturaEditable;
    private BigDecimal montoCobertura;
    private String montoCoberturaFormateado;


    public String getIdCoberturaProducto() {
        return idCoberturaProducto;
    }

    public void setIdCoberturaProducto(String idCoberturaProducto) {
        this.idCoberturaProducto = idCoberturaProducto;
    }

    public String getDescripcionCorta() {
        return descripcionCorta;
    }

    public void setDescripcionCorta(String descripcionCorta) {
        this.descripcionCorta = descripcionCorta;
    }

    public String getIdCoberturaEnlatado() {
        return idCoberturaEnlatado;
    }

    public void setIdCoberturaEnlatado(String idCoberturaEnlatado) {
        this.idCoberturaEnlatado = idCoberturaEnlatado;
    }

    public String getCodigoCobertura() {
        return codigoCobertura;
    }

    public void setCodigoCobertura(String codigoCobertura) {
        this.codigoCobertura = codigoCobertura;
    }

    public String getNameCoberturaEnlatado() {
        return nameCoberturaEnlatado;
    }

    public void setNameCoberturaEnlatado(String nameCoberturaEnlatado) {
        this.nameCoberturaEnlatado = nameCoberturaEnlatado;
    }

    public Integer getPorcentajeDeDisminucion() {
        return porcentajeDeDisminucion;
    }

    public void setPorcentajeDeDisminucion(Integer porcentajeDeDisminucion) {
        this.porcentajeDeDisminucion = porcentajeDeDisminucion;
    }

    public String getDescripcionLarga() {
        return descripcionLarga;
    }

    public void setDescripcionLarga(String descripcionLarga) {
        this.descripcionLarga = descripcionLarga;
    }

    public List<Object> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<Object> opciones) {
        this.opciones = opciones;
    }

    public String getNameCoberturaProducto() {
        return nameCoberturaProducto;
    }

    public void setNameCoberturaProducto(String nameCoberturaProducto) {
        this.nameCoberturaProducto = nameCoberturaProducto;
    }

    public Integer getPorcentajeDeAumento() {
        return porcentajeDeAumento;
    }

    public void setPorcentajeDeAumento(Integer porcentajeDeAumento) {
        this.porcentajeDeAumento = porcentajeDeAumento;
    }

    public String getIdCobertura() {
        return idCobertura;
    }

    public void setIdCobertura(String idCobertura) {
        this.idCobertura = idCobertura;
    }

    public Boolean getCoberturaEditable() {
        return coberturaEditable;
    }

    public void setCoberturaEditable(Boolean coberturaEditable) {
        this.coberturaEditable = coberturaEditable;
    }

    public BigDecimal getMontoCobertura() {
        return montoCobertura;
    }

    public void setMontoCobertura(BigDecimal montoCobertura) {
        this.montoCobertura = montoCobertura;
    }

    public String getMontoCoberturaFormateado() {
        return montoCoberturaFormateado;
    }

    public void setMontoCoberturaFormateado(String montoCoberturaFormateado) {
        this.montoCoberturaFormateado = montoCoberturaFormateado;
    }
}
