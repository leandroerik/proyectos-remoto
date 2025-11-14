package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.CNV.investor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
public class DetalleContato {
    private String nombreApellido; //contactName
    private String email;

    public DetalleContato() {
    }

    public DetalleContato(String nombreApellido, String email) {
        this.nombreApellido = nombreApellido;
        this.email = email;
    }
    public String getNombreApellido() {
        return nombreApellido;
    }
    public void setNombreApellido(String nombreApellido) {
        this.nombreApellido = nombreApellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
