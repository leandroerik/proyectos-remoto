package ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.model;

public class AjusteModificaciones {

    private String estado;
    private Integer lote;
    private String secuencia;

    public AjusteModificaciones() {
    }

    public AjusteModificaciones(String estado, Integer lote, String secuencia) {
        this.estado = estado;
        this.lote = lote;
        this.secuencia = secuencia;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getLote() {
        return lote;
    }

    public void setLote(Integer lote) {
        this.lote = lote;
    }

    public String getSecuencia() {
        return secuencia;
    }

    public void setSecuencia(String secuencia) {
        this.secuencia = secuencia;
    }

}
