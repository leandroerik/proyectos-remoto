package ar.com.hipotecario.canal.rewards.modulos.campanias.model;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class Campania extends ApiObjeto {

    private String fechaProceso;
    private String fechaEnvio;
    private String totalRegistros;
    private String totalRegistrosProcesados;
    private String totalRegistrosRechazados;
    private String totalPuntos;
    private String totalPuntosConfirmados;
    private String totalPuntosRechazados;
    private String usuarioAprobacion;
    private String fecha;
    private String secuencial;


    public String getFechaProceso() {
        return fechaProceso;
    }

    public void setFechaProceso(String fechaProceso) {
        this.fechaProceso = fechaProceso;
    }

    public String getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(String fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getTotalRegistros() {
        return totalRegistros;
    }

    public void setTotalRegistros(String totalRegistros) {
        this.totalRegistros = totalRegistros;
    }

    public String getTotalRegistrosProcesados() {
        return totalRegistrosProcesados;
    }

    public void setTotalRegistrosProcesados(String totalRegistrosProcesados) {
        this.totalRegistrosProcesados = totalRegistrosProcesados;
    }

    public String getTotalRegistrosRechazados() {
        return totalRegistrosRechazados;
    }

    public void setTotalRegistrosRechazados(String totalRegistrosRechazados) {
        this.totalRegistrosRechazados = totalRegistrosRechazados;
    }

    public String getTotalPuntos() {
        return totalPuntos;
    }

    public void setTotalPuntos(String totalPuntos) {
        this.totalPuntos = totalPuntos;
    }

    public String getTotalPuntosConfirmados() {
        return totalPuntosConfirmados;
    }

    public void setTotalPuntosConfirmados(String totalPuntosConfirmados) {
        this.totalPuntosConfirmados = totalPuntosConfirmados;
    }

    public String getTotalPuntosRechazados() {
        return totalPuntosRechazados;
    }

    public void setTotalPuntosRechazados(String totalPuntosRechazados) {
        this.totalPuntosRechazados = totalPuntosRechazados;
    }

    public String getUsuarioAprobacion() {
        return usuarioAprobacion;
    }

    public void setUsuarioAprobacion(String usuarioAprobacion) {
        this.usuarioAprobacion = usuarioAprobacion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getSecuencial() {
        return secuencial;
    }

    public void setSecuencial(String secuencial) {
        this.secuencial = secuencial;
    }


}
