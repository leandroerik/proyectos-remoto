package ar.com.hipotecario.canal.homebanking.negocio.seguro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Enlatado {
    @JsonProperty("idEnlatado")
    private String idEnlatado;

    @JsonProperty("premio")
    private BigDecimal premio;

    @JsonProperty("premioFormateado")
    private String premioFormateado;

    public String getPremioFormateado() {
        return premioFormateado;
    }

    public void setPremioFormateado(String premioFormateado) {
        this.premioFormateado = premioFormateado;
    }

    @JsonProperty("asistencia")
    private String asistencia;

    @JsonProperty("ambiente")
    private String ambiente;

    @JsonProperty("name")
    private String name;

    @JsonProperty("sellos")
    private Integer sellos;

    @JsonProperty("combo")
    private String combo;

    @JsonProperty("codigoEnlatado")
    private String codigoEnlatado;

    @JsonProperty("id")
    private String id;

    @JsonProperty("productor")
    private String productor;

    @JsonProperty("coberturas")
    private List<Cobertura> coberturas;

    public String getIdEnlatado() {
        return idEnlatado;
    }

    public void setIdEnlatado(String idEnlatado) {
        this.idEnlatado = idEnlatado;
    }

    public BigDecimal getPremio() {
        return premio;
    }

    public void setPremio(BigDecimal premio) {
        this.premio = premio;
    }

    public String getAsistencia() {
        return asistencia;
    }

    public void setAsistencia(String asistencia) {
        this.asistencia = asistencia;
    }

    public String getAmbiente() {
        return ambiente;
    }

    public void setAmbiente(String ambiente) {
        this.ambiente = ambiente;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSellos() {
        return sellos;
    }

    public void setSellos(Integer sellos) {
        this.sellos = sellos;
    }

    public String getCombo() {
        return combo;
    }

    public void setCombo( String combo ) {
        this.combo = combo;
    }

    public String getCodigoEnlatado() {
        return codigoEnlatado;
    }

    public void setCodigoEnlatado(String codigoEnlatado) {
        this.codigoEnlatado = codigoEnlatado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductor() {
        return productor;
    }

    public void setProductor(String productor) {
        this.productor = productor;
    }

    public List<Cobertura> getCoberturas() {
        return coberturas;
    }

    public void setCoberturas(List<Cobertura> coberturas) {
        this.coberturas = coberturas;
    }

}
