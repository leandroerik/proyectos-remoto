package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.CNV.investor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IdentificadorInteresado {
    private String tipo; //type
    private Integer numeroIdentificador; //identifier
    private String paisEntidadEmisora;//issuingEntity
    private String paisDomicilioInversor; //countryOfIssuance

    public IdentificadorInteresado() {
    }

    public IdentificadorInteresado(String tipo, Integer numeroIdentificador, String paisEntidadEmisora, String paisDomicilioInversor) {
        this.tipo = tipo;
        this.numeroIdentificador = numeroIdentificador;
        this.paisEntidadEmisora = paisEntidadEmisora;
        this.paisDomicilioInversor = paisDomicilioInversor;
    }
    public String getTipo() {
        return tipo;
    }
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    public Integer getNumeroIdentificador() {
        return numeroIdentificador;
    }
    public void setNumeroIdentificador(Integer numeroIdentificador) {
        this.numeroIdentificador = numeroIdentificador;
    }
    public String getPaisEntidadEmisora() {
        return paisEntidadEmisora;
    }
    public void setPaisEntidadEmisora(String paisEntidadEmisora) {
        this.paisEntidadEmisora = paisEntidadEmisora;
    }
    public String getPaisDomicilioInversor() {
        return paisDomicilioInversor;
    }
    public void setPaisDomicilioInversor(String paisDomicilioInversor) {
        this.paisDomicilioInversor = paisDomicilioInversor;
    }
    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
