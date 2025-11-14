package ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos;

import ar.com.hipotecario.backend.base.Objeto;

public class TASKiosco extends Objeto {

    private Integer kioscoId;
    private String kioscoNombre;
    private String kioscoIp;
    private String kioscoDescripcion;
    private Integer sucursalId;
    private Integer ubicacionId;
    private String flagHabilitado;
    private boolean esInteligente;
    private String horaInicio;
    private String horaFin;
    private String nombreUbicacion;
    private String telefono;
    private String direccion1;
    private String direccion2;
    private String direccion3;

    private String reporte;

    public TASKiosco() {
    }
    public Integer getKioscoId() {
        return kioscoId;
    }

    public void setKioscoId(Integer kioscoId) {
        this.kioscoId = kioscoId;
    }

    public String getKioscoNombre() {
        return kioscoNombre;
    }

    public void setKioscoNombre(String kioscoNombre) {
        this.kioscoNombre = kioscoNombre;
    }

    public String getKioscoIp() {
        return kioscoIp;
    }

    public void setKioscoIp(String kioscoIp) {
        this.kioscoIp = kioscoIp;
    }

    public String getKioscoDescripcion() {
        return kioscoDescripcion;
    }

    public void setKioscoDescripcion(String kioscoDescripcion) {
        this.kioscoDescripcion = kioscoDescripcion;
    }

    public Integer getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Integer sucursalId) {
        this.sucursalId = sucursalId;
    }

    public Integer getUbicacionId() {
        return ubicacionId;
    }

    public void setUbicacionId(Integer ubicacionId) {
        this.ubicacionId = ubicacionId;
    }

    public String getFlagHabilitado() {
        return flagHabilitado;
    }

    public void setFlagHabilitado(String flagHabilitado) {
        this.flagHabilitado = flagHabilitado;
    }

    public boolean getEsInteligente() {
        return esInteligente;
    }

    public void setEsInteligente(boolean esInteligente) {
        this.esInteligente = esInteligente;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public String getNombreUbicacion() {
        return nombreUbicacion;
    }

    public void setNombreUbicacion(String nombreUbicacion) {
        this.nombreUbicacion = nombreUbicacion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion1() {
        return direccion1;
    }

    public void setDireccion1(String direccion1) {
        this.direccion1 = direccion1;
    }

    public String getDireccion2() {
        return direccion2;
    }

    public void setDireccion2(String direccion2) {
        this.direccion2 = direccion2;
    }

    public String getDireccion3() {
        return direccion3;
    }

    public void setDireccion3(String direccion3) {
        this.direccion3 = direccion3;
    }

    public boolean isEsInteligente() {
        return esInteligente;
    }

    public String getReporte() {
        return reporte;
    }

    public void setReporte(String reporte) {
        this.reporte = reporte;
    }

    public static TASKiosco armarModeloKiosco(Objeto Taskiosco, Objeto TasKioscoDbHelper){
        TASKiosco dto = new TASKiosco();
        dto.kioscoId = Taskiosco.objetos().get(0).integer("KioscoId",0);
        dto.kioscoNombre = Taskiosco.objetos().get(0).string("Nombre","");
        dto.kioscoIp = Taskiosco.objetos().get(0).string("DireccionIP","");
        dto.kioscoDescripcion = Taskiosco.objetos().get(0).string("Descripcion","");
        dto.sucursalId = Taskiosco.objetos().get(0).integer("SucursalId",0);
        dto.ubicacionId = Taskiosco.objetos().get(0).integer("UbicacionId",0);
        dto.flagHabilitado = Taskiosco.objetos().get(0).string("FlagHabilitado","");
        dto.esInteligente = Taskiosco.objetos().get(0).integer("TipoTas").equals(3) ? true : false;
        dto.horaInicio = TasKioscoDbHelper.string("Hora.Inicio", "");
        dto.horaFin = TasKioscoDbHelper.string("Hora.Fin", "");
        dto.nombreUbicacion = TasKioscoDbHelper.string("Nombre", "");
        dto.telefono = TasKioscoDbHelper.string("Telefono", "");
        dto.direccion1 = TasKioscoDbHelper.string("Direccion1", "");
        dto.direccion2 = TasKioscoDbHelper.string("Direccion2", "");
        dto.direccion3 = TasKioscoDbHelper.string("Direccion3", "");
        dto.reporte = "false";
        return dto;
    }
    public static Objeto armarModeloObjeto(TASKiosco kiosco){
        Objeto objeto = new Objeto();
        objeto.set("KioscoId",kiosco.getKioscoId());
        objeto.set("Nombre",kiosco.getKioscoNombre());
        objeto.set("DireccionIP",kiosco.getKioscoIp());
        objeto.set("Descripcion",kiosco.getKioscoDescripcion());
        objeto.set("SucursalId",kiosco.getSucursalId());
        objeto.set("UbicacionId",kiosco.getUbicacionId());
        objeto.set("FlagHabilitado",kiosco.getFlagHabilitado());
        objeto.set("esInteligente",kiosco.getEsInteligente());
        objeto.set("Hora.Inicio", kiosco.getHoraInicio());
        objeto.set("Hora.Fin", kiosco.getHoraFin());
        objeto.set("Nombre", kiosco.getNombreUbicacion());
        objeto.set("Telefono", kiosco.getTelefono());
        objeto.set("Direccion1", kiosco.getDireccion1());
        objeto.set("Direccion2", kiosco.getDireccion2());
        objeto.set("Direccion3", kiosco.getDireccion3());
        objeto.set("reporte", !kiosco.getReporte().contains("false") ? true : false);
        return objeto;
    }

    //Parseo Objeto de BBDD o Sesion a TasKioscoAdmin
    public static TASKiosco fillDatos(Objeto objeto){
        TASKiosco kioscoAdmin = new TASKiosco();
        kioscoAdmin.kioscoId = objeto.integer("KioscoId");
        kioscoAdmin.kioscoNombre = objeto.string("Nombre");
        kioscoAdmin.kioscoIp = objeto.string("DireccionIP");
        kioscoAdmin.kioscoDescripcion = objeto.string("Descripcion");
        kioscoAdmin.sucursalId = Integer.valueOf(objeto.string("SucursalId"));
        kioscoAdmin.ubicacionId = objeto.integer("UbicacionId");
        kioscoAdmin.flagHabilitado = objeto.string("FlagHabilitado");
        kioscoAdmin.esInteligente = objeto.bool("esInteligente");
        kioscoAdmin.horaInicio = objeto.string("Hora.Inicio");
        kioscoAdmin.horaFin = objeto.string("Hora.Fin");
        kioscoAdmin.nombreUbicacion = objeto.string("NombreUbicacion");
        kioscoAdmin.telefono = objeto.string("Telefono");
        kioscoAdmin.direccion1 = objeto.string("Direccion1");
        kioscoAdmin.direccion2 = objeto.string("Direccion2");
        kioscoAdmin.direccion3 = objeto.string("Direccion3");
        return kioscoAdmin;
    }
}
