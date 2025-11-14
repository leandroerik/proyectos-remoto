package ar.com.hipotecario.canal.tas.modulos.auditoria.modelos;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class TASAuditoria extends ApiObjeto {

    String kioscoId;
    Fecha fechaUltimaAuditoria;
    String precinto1;
    String precinto2;
    String lote;
    String tipoCierre;

    Objeto error;

    public TASAuditoria() {
    }

    public String getKioscoId() {
        return kioscoId;
    }

    public void setKioscoId(String kioscoId) {
        this.kioscoId = kioscoId;
    }

    public Fecha getFechaUltimaAuditoria() {
        return fechaUltimaAuditoria;
    }

    public void setFechaUltimaAuditoria(Fecha fechaUltimaAuditoria) {
        this.fechaUltimaAuditoria = fechaUltimaAuditoria;
    }

    public String getPrecinto1() {
        return precinto1;
    }

    public void setPrecinto1(String precinto1) {
        this.precinto1 = precinto1;
    }

    public String getPrecinto2() {
        return precinto2;
    }

    public Objeto getError() {
        return error;
    }

    public void setError(Objeto error) {
        this.error = error;
    }

    public void setPrecinto2(String precinto2) {
        this.precinto2 = precinto2;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getTipoCierre() {
        return tipoCierre;
    }

    public void setTipoCierre(String tipoCierre) {
        this.tipoCierre = tipoCierre;
    }


}
