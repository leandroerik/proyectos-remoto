package ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.model;

import com.google.gson.Gson;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class ResponseAprobarAjuste extends ApiObjeto {

    private String lote;
    private String numeronovedad;
    private String codigoerror;
    private String descripcionerror;

    public ResponseAprobarAjuste() {
    }

    public ResponseAprobarAjuste(String lote, String numeronovedad, String codigoerror, String descripcionerror) {
        this.lote = lote;
        this.numeronovedad = numeronovedad;
        this.codigoerror = codigoerror;
        this.descripcionerror = descripcionerror;
    }

    // create method fromJson
    public static ResponseAprobarAjuste fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, ResponseAprobarAjuste.class);
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getNumeronovedad() {
        return numeronovedad;
    }

    public void setNumeronovedad(String numeronovedad) {
        this.numeronovedad = numeronovedad;
    }

    public String getCodigoerror() {
        return codigoerror;
    }

    public void setCodigoerror(String codigoerror) {
        this.codigoerror = codigoerror;
    }

    public String getDescripcionerror() {
        return descripcionerror;
    }

    public void setDescripcionerror(String descripcionerror) {
        this.descripcionerror = descripcionerror;
    }

}