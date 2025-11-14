package ar.com.hipotecario.canal.rewards.middleware.models.negocio;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class RWBeneficioAdherido extends ApiObjeto {
    private String programa;
    private String unidad;
    private String cuenta;
    private String altaNovedad;
    private String fechaEnvio;
    private String fechaRespuesta;
    private String tipoNovedad;
    private String estado;
    private int cantBloqueos;
    private int cantRedimidos;
    private int cantVencidos;
    private int calculadosTC;
    private int calculadosTD;
    private int cantPromocion;
    private int cantAjustados;
    private String codigoCampania;

    public RWBeneficioAdherido() {

    }

    public RWBeneficioAdherido(Objeto obj) {
        this.programa = obj.string("programa");
        this.unidad = obj.string("unidad");
        this.cuenta = obj.string("cuenta");
        this.altaNovedad = obj.string("altaNovedad");
        this.fechaEnvio = obj.string("fechaEnvio");
        this.fechaRespuesta = obj.string("fechaRespuesta");
        this.tipoNovedad = obj.string("tipoNovedad");
        this.estado = obj.string("estado");
        this.cantBloqueos = obj.integer("cantBloqueos");
        this.cantRedimidos = obj.integer("cantRedimidos");
        this.cantVencidos = obj.integer("cantVencidos");
        this.calculadosTC = obj.integer("calculadosTC");
        this.calculadosTD = obj.integer("calculadosTD");
        this.cantPromocion = obj.integer("cantPromocion");
        this.cantAjustados = obj.integer("cantAjustados");
        this.codigoCampania = obj.string("codigoCampania");
    }

    public String getPrograma() {
        return programa;
    }

    public void setPrograma(String programa) {
        this.programa = programa;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public String getCuenta() {
        return cuenta;
    }

    public void setCuenta(String cuenta) {
        this.cuenta = cuenta;
    }

    public String getAltaNovedad() {
        return altaNovedad;
    }

    public void setAltaNovedad(String altaNovedad) {
        this.altaNovedad = altaNovedad;
    }

    public String getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(String fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(String fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public String getTipoNovedad() {
        return tipoNovedad;
    }

    public void setTipoNovedad(String tipoNovedad) {
        this.tipoNovedad = tipoNovedad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getCantBloqueos() {
        return cantBloqueos;
    }

    public void setCantBloqueos(int cantBloqueos) {
        this.cantBloqueos = cantBloqueos;
    }

    public int getCantRedimidos() {
        return cantRedimidos;
    }

    public void setCantRedimidos(int cantRedimidos) {
        this.cantRedimidos = cantRedimidos;
    }

    public int getCantVencidos() {
        return cantVencidos;
    }

    public void setCantVencidos(int cantVencidos) {
        this.cantVencidos = cantVencidos;
    }

    public int getCalculadosTC() {
        return calculadosTC;
    }

    public void setCalculadosTC(int calculadosTC) {
        this.calculadosTC = calculadosTC;
    }

    public int getCalculadosTD() {
        return calculadosTD;
    }

    public void setCalculadosTD(int calculadosTD) {
        this.calculadosTD = calculadosTD;
    }

    public int getCantPromocion() {
        return cantPromocion;
    }

    public void setCantPromocion(int cantPromocion) {
        this.cantPromocion = cantPromocion;
    }

    public int getCantAjustados() {
        return cantAjustados;
    }

    public void setCantAjustados(int cantAjustados) {
        this.cantAjustados = cantAjustados;
    }

    public String getCodigoCampania() {
        return codigoCampania;
    }

    public void setCodigoCampania(String codigoCampania) {
        this.codigoCampania = codigoCampania;
    }
}
