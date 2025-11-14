package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.CNV.investor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
public class DetalleDireccion {
    private String direccion; //addressLine1
    private Integer codigoPostal; //postalCode
    private String cuidad; //city
    private String provincia; //stateProvince
    private String pais;//country
    private String tipoDireccion; //addressType

    public DetalleDireccion() {
    }

    public DetalleDireccion(String direccion, Integer codigoPostal, String cuidad, String provincia, String pais, String tipoDireccion) {
        this.direccion = direccion;
        this.codigoPostal = codigoPostal;
        this.cuidad = cuidad;
        this.provincia = provincia;
        this.pais = pais;
        this.tipoDireccion = tipoDireccion;
    }

    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    public Integer getCodigoPostal() {
        return codigoPostal;
    }
    public void setCodigoPostal(Integer codigoPostal) {
        this.codigoPostal = codigoPostal;
    }
    public String getCuidad() {
        return cuidad;
    }
    public void setCuidad(String cuidad) {
        this.cuidad = cuidad;
    }
    public String getProvincia() {
        return provincia;
    }
    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }
    public String getPais() {
        return pais;
    }
    public void setPais(String pais) {
        this.pais = pais;
    }
    public String getTipoDireccion() {
        return tipoDireccion;
    }
    public void setTipoDireccion(String tipoDireccion) {
        this.tipoDireccion = tipoDireccion;
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
