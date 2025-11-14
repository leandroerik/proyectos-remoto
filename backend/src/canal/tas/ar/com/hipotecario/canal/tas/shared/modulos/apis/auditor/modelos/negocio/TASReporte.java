package ar.com.hipotecario.canal.tas.shared.modulos.apis.auditor.modelos.negocio;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKiosco;
import ar.com.hipotecario.mobile.lib.Util;
import org.apache.http.HttpStatus;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TASReporte extends Objeto {
    private String canal;
    private Integer duracion;
    private Error error;
    private String fin;
    private String idEvento;
    private String idProceso;
    private String idVariante;
    private String inicio;
    private String ipusuario;
    private Integer orden;
    private String resultado;
    private String servicio;
    private String sesion;
    private String subCanal;
    private String sucursal;
    private String tipoEvento;
    private String tipoSesion;
    private String usuario;

    private Objeto mensajeEntrada;

    private Objeto mensajeSalida;



    public TASReporte() {
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }
    public Integer getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public String getFin() {
        return fin;
    }

    public void setFin(String fin) {
        this.fin = fin;
    }

    public String getIdEvento() {
        return idEvento;
    }

    public void setIdEvento(String idEvento) {
        this.idEvento = idEvento;
    }

    public String getIdProceso() {
        return idProceso;
    }

    public void setIdProceso(String idProceso) {
        this.idProceso = idProceso;
    }

    public String getIdVariante() {
        return idVariante;
    }

    public void setIdVariante(String idVariante) {
        this.idVariante = idVariante;
    }

    public String getInicio() {
        return inicio;
    }

    public void setInicio(String inicio) {
        this.inicio = inicio;
    }

    public String getIpusuario() {
        return ipusuario;
    }

    public void setIpusuario(String ipusuario) {
        this.ipusuario = ipusuario;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }

    public String getSesion() {
        return sesion;
    }

    public void setSesion(String sesion) {
        this.sesion = sesion;
    }

    public String getSubCanal() {
        return subCanal;
    }

    public void setSubCanal(String subCanal) {
        this.subCanal = subCanal;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public String getTipoSesion() {
        return tipoSesion;
    }

    public void setTipoSesion(String tipoSesion) {
        this.tipoSesion = tipoSesion;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public Objeto getMensajeEntrada() {
        return mensajeEntrada;
    }

    public void setMensajeEntrada(Objeto mensajeEntrada) {
        this.mensajeEntrada = mensajeEntrada;
    }

    public Objeto getMensajeSalida() {
        return mensajeSalida;
    }

    public void setMensajeSalida(Objeto mensajeSalida) {
        this.mensajeSalida = mensajeSalida;
    }


}
