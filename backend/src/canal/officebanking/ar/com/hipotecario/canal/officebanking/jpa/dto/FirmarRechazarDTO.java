package ar.com.hipotecario.canal.officebanking.jpa.dto;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.DebinOB;

import java.math.BigDecimal;

public class FirmarRechazarDTO {

    private String estado;
    private String accion;
    private Integer idOperacion;
    private String estadoBandeja;
    private String moneda;
    private String cuenta;
    private BigDecimal monto;
    private String fondo;
    private String fechaConcertacion;
    private String descripcion;
    public Object armarRespuesta() {
        Objeto respuesta = new Objeto();
        respuesta.set("estado", estado);
        respuesta.set("accion", accion);
        respuesta.set("idOperacion", idOperacion);
        respuesta.set("estadoBandeja", estadoBandeja);
        respuesta.set("moneda", moneda);
        respuesta.set("cuenta", cuenta);
        respuesta.set("monto", monto);
        respuesta.set("descripcion", descripcion);
        //fci
        respuesta.set("fondo", fondo);
        respuesta.set("fechaConcertacion", fechaConcertacion);

        return respuesta;
    }


    public FirmarRechazarDTO(String estado, String accion, Integer idOperacion, String estadoBandeja, String moneda, String cuenta, BigDecimal monto, String fondo, String fechaConcertacion, String descripcion) {
        this.estado = estado;
        this.accion = accion;
        this.idOperacion = idOperacion;
        this.estadoBandeja = estadoBandeja;
        this.moneda = moneda;
        this.cuenta = cuenta;
        this.monto = monto;
        this.fondo = fondo;
        this.fechaConcertacion = fechaConcertacion;
        this.descripcion = descripcion;
    }

    public FirmarRechazarDTO() {
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public String getCuenta() {
        return cuenta;
    }

    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getFondo() {
        return fondo;
    }

    public void setFondo(String fondo) {
        this.fondo = fondo;
    }

    public String getFechaConcertacion() {
        return fechaConcertacion;
    }

    public void setFechaConcertacion(String fechaConcertacion) {
        this.fechaConcertacion = fechaConcertacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public Integer getIdOperacion() {
        return idOperacion;
    }

    public void setIdOperacion(Integer idOperacion) {
        this.idOperacion = idOperacion;
    }

    public String getEstadoBandeja() {
        return estadoBandeja;
    }

    public void setEstadoBandeja(String estadoBandeja) {
        this.estadoBandeja = estadoBandeja;
    }
}
