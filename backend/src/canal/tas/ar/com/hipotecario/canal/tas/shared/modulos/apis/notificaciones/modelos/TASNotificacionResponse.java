package ar.com.hipotecario.canal.tas.shared.modulos.apis.notificaciones.modelos;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class TASNotificacionResponse extends ApiObjeto {
    private Integer idAlerta;
    private String textoAlerta;
    private String textoRespuesta;
    private String tipoRespuesta;

    public TASNotificacionResponse() {
    }

    public TASNotificacionResponse(Integer idAlerta, String textoAlerta, String textoRespuesta, String tipoRespuesta) {
        this.idAlerta = idAlerta;
        this.textoAlerta = textoAlerta;
        this.textoRespuesta = textoRespuesta;
        this.tipoRespuesta = tipoRespuesta;
    }

    public Integer getIdAlerta() {
        return idAlerta;
    }

    public void setIdAlerta(Integer idAlerta) {
        this.idAlerta = idAlerta;
    }

    public String getTextoAlerta() {
        return textoAlerta;
    }

    public void setTextoAlerta(String textoAlerta) {
        this.textoAlerta = textoAlerta;
    }

    public String getTextoRespuesta() {
        return textoRespuesta;
    }

    public void setTextoRespuesta(String textoRespuesta) {
        this.textoRespuesta = textoRespuesta;
    }

    public String getTipoRespuesta() {
        return tipoRespuesta;
    }

    public void setTipoRespuesta(String tipoRespuesta) {
        this.tipoRespuesta = tipoRespuesta;
    }
}
