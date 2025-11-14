package ar.com.hipotecario.canal.tas.shared.utils.models;

import ar.com.hipotecario.canal.tas.ContextoTAS;

public class TasMinoRequestParams {
    private String nroDoc;
    private String clave;
    private String journey;

    public TasMinoRequestParams(String nroDoc, String clave, String journey) {
        this.nroDoc = nroDoc;
        this.clave = clave;
        this.journey = journey;
    }
    
    public String getNroDoc() {
        return nroDoc;
    }

    public void setNroDoc(String nroDoc) {
        this.nroDoc = nroDoc;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getJourney() {
        return journey;
    }

    public void setJourney(String journey) {
        this.journey = journey;
    }
}
