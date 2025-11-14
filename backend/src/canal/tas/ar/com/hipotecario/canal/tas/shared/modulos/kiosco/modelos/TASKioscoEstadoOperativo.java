package ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.RespuestaTAS;

import java.util.Date;

public class TASKioscoEstadoOperativo extends Objeto {

    private Long estadoId;
    private Integer kioscoId;
    private String initOperational;
    private String impresora;
    private String buzon;
    private String msr;
    private String cim;
    private Date fecha;

    public TASKioscoEstadoOperativo() {
    }

    public TASKioscoEstadoOperativo(Integer kioscoId, String initOperational, String impresora, String buzon,
            String msr, String cim, Date fecha) {
        this.kioscoId = kioscoId;
        this.initOperational = initOperational;
        this.impresora = impresora;
        this.buzon = buzon;
        this.msr = msr;
        this.cim = cim;
        this.fecha = fecha;
    }

    public static boolean verificaParams(Integer kioscoId, String initOperational, String impresora, String buzon,
            String msr, String cim) {
        boolean verifica = kioscoId == -1 || initOperational == "" || impresora == "" || buzon == "" || msr == ""
                || cim == "" ? true : false;
        return verifica;
    }

    public Long getEstadoId() {
        return estadoId;
    }

    public void setEstadoId(Long estadoId) {
        this.estadoId = estadoId;
    }

    public Integer getKioscoId() {
        return kioscoId;
    }

    public void setKioscoId(Integer kioscoId) {
        this.kioscoId = kioscoId;
    }

    public String getInitOperational() {
        return initOperational;
    }

    public void setInitOperational(String initOperational) {
        this.initOperational = initOperational;
    }

    public String getImpresora() {
        return impresora;
    }

    public void setImpresora(String impresora) {
        this.impresora = impresora;
    }

    public String getBuzon() {
        return buzon;
    }

    public void setBuzon(String buzon) {
        this.buzon = buzon;
    }

    public String getMsr() {
        return msr;
    }

    public void setMsr(String msr) {
        this.msr = msr;
    }

    public String getCim() {
        return cim;
    }

    public void setCim(String cim) {
        this.cim = cim;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Objeto toObject(String kioscoId, String initOperational, String impresora, String buzon, String msr,
            String cim, Date fecha) {
        Objeto rta = new Objeto();
        rta.set("KioscoId", kioscoId);
        rta.set("InitOperational", initOperational);
        rta.set("Impresora", impresora);
        rta.set("Buzon", buzon);
        rta.set("MSR", msr);
        rta.set("Cim", cim);
        rta.set("Fecha", fecha);
        return rta;
    }

}
