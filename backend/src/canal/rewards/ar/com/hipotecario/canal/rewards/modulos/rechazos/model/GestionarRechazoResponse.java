package ar.com.hipotecario.canal.rewards.modulos.rechazos.model;
import ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.model.ResponseAprobarAjuste;
import com.google.gson.Gson;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;


public class GestionarRechazoResponse extends ApiObjeto{
    private String codError;
    private String descripcionError;
    private Integer lote;
    private Integer nroNovedad;
    private String programa;

    // Constructor vacío
    public GestionarRechazoResponse() {}

    // Constructor con todos los campos
    public GestionarRechazoResponse(String codError, String descripcionError, Integer lote, Integer nroNovedad, String programa) {
        this.codError = codError;
        this.descripcionError = descripcionError;
        this.lote = lote;
        this.nroNovedad = nroNovedad;
        this.programa = programa;
    }
    public static GestionarRechazoResponse fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, GestionarRechazoResponse.class);
    }
    // Getters y Setters
    public String getCodError() {
        return codError;
    }

    public void setCodError(String codError) {
        this.codError = codError;
    }

    public String getDescripcionError() {
        return descripcionError;
    }

    public void setDescripcionError(String descripcionError) {
        this.descripcionError = descripcionError;
    }

    public Integer getLote() {
        return lote;
    }

    public void setLote(Integer lote) {
        this.lote = lote;
    }

    public Integer getNroNovedad() {
        return nroNovedad;
    }

    public void setNroNovedad(Integer nroNovedad) {
        this.nroNovedad = nroNovedad;
    }

    public String getPrograma() {
        return programa;
    }

    public void setPrograma(String programa) {
        this.programa = programa;
    }
}