package ar.com.hipotecario.backend.servicio.api.inversiones;

public class CuentaCuotapartistaResumenRequest {
    private int codCuotapartista;
    private String codFondo;
    private String fechaDesde;
    private String fechaHasta;
    private boolean soloCtasAct;

    public int getCodCuotapartista() {
        return codCuotapartista;
    }

    public void setCodCuotapartista(int codCuotapartista) {
        this.codCuotapartista = codCuotapartista;
    }

    public String getCodFondo() {
        return codFondo;
    }

    public void setCodFondo(String codFondo) {
        this.codFondo = codFondo;
    }

    public String getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(String fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public String getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(String fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public boolean isSoloCtasAct() {
        return soloCtasAct;
    }

    public void setSoloCtasAct(boolean soloCtasAct) {
        this.soloCtasAct = soloCtasAct;
    }
}
