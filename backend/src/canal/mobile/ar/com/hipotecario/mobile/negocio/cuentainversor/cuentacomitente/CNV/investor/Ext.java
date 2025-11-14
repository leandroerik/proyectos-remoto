package ar.com.hipotecario.mobile.negocio.cuentainversor.cuentacomitente.CNV.investor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Ext {
    private String tipoIdentificador; //taxPayerIdType
    private String estadoIVA;//vatStatus
    private String estadoImpuesoSobreRenta; //incomeTaxStatus
    private String actividadNegocio; //businessActivity
    private String businessSize;
    private String paisRegistro; //registrationArea
    private String cuidadRegistro; //RpcPlace

    public Ext() {
    }

    public Ext(String tipoIdentificador, String estadoIVA, String estadoImpuesoSobreRenta){
        this.tipoIdentificador = tipoIdentificador;
        this.estadoIVA = estadoIVA;
        this.estadoImpuesoSobreRenta = estadoImpuesoSobreRenta;
    }

    public Ext(String tipoIdentificador, String estadoIVA, String estadoImpuesoSobreRenta, String actividadNegocio, String businessSize, String paisRegistro, String cuidadRegistro) {
        this.tipoIdentificador = tipoIdentificador;
        this.estadoIVA = estadoIVA;
        this.estadoImpuesoSobreRenta = estadoImpuesoSobreRenta;
        this.actividadNegocio = actividadNegocio;
        this.businessSize = businessSize;
        this.paisRegistro = paisRegistro;
        this.cuidadRegistro = cuidadRegistro;
    }

    public String getTipoIdentificador() {
        return tipoIdentificador;
    }
    public void setTipoIdentificador(String tipoIdentificador) {
        this.tipoIdentificador = tipoIdentificador;
    }
    public String getEstadoIVA() {
        return estadoIVA;
    }
    public void setEstadoIVA(String estadoIVA) {
        this.estadoIVA = estadoIVA;
    }
    public String getEstadoImpuesoSobreRenta() {
        return estadoImpuesoSobreRenta;
    }
    public void setEstadoImpuesoSobreRenta(String estadoImpuesoSobreRenta) {
        this.estadoImpuesoSobreRenta = estadoImpuesoSobreRenta;
    }
    public String getActividadNegocio() {
        return actividadNegocio;
    }
    public void setActividadNegocio(String actividadNegocio) {
        this.actividadNegocio = actividadNegocio;
    }
    public String getBusinessSize() {
        return businessSize;
    }

    public void setBusinessSize(String businessSize) {
        this.businessSize = businessSize;
    }
    public String getPaisRegistro() {
        return paisRegistro;
    }
    public void setPaisRegistro(String paisRegistro) {
        this.paisRegistro = paisRegistro;
    }
    public String getCuidadRegistro() {
        return cuidadRegistro;
    }
    public void setCuidadRegistro(String cuidadRegistro) {
        this.cuidadRegistro = cuidadRegistro;
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
