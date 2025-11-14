package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.CNV.account;

public class GrupoCuenta {
    private String fechaDesactivacion; //deactivationDate
    private String nombre; //
    public GrupoCuenta() {
    }
    public GrupoCuenta(String fechaDesactivacion, String nombre) {
        this.fechaDesactivacion = fechaDesactivacion;
        this.nombre = nombre;}
    public String getFechaDesactivacion() {
        return fechaDesactivacion;
    }
    public void setFechaDesactivacion(String fechaDesactivacion) {
        this.fechaDesactivacion = fechaDesactivacion;
    }
    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
