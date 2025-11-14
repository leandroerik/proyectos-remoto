package ar.com.hipotecario.canal.tas.modulos.inicio.login.modelos;

import ar.com.hipotecario.backend.base.Objeto;

public class TASUsuariosAdministradores extends Objeto {

    private String numeroDocumento;
    private String tipoDocumento;
    private String administradorId;
    private Integer sucursalId;
    private boolean flagAdministrador;

    public TASUsuariosAdministradores() {
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getAdministradorId() {
        return administradorId;
    }

    public void setAdministradorId(String administradorId) {
        this.administradorId = administradorId;
    }

    public Integer getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Integer sucursalId) {
        this.sucursalId = sucursalId;
    }

    public boolean isFlagAdministrador() {
        return flagAdministrador;
    }

    public void setFlagAdministrador(boolean flagAdministrador) {
        this.flagAdministrador = flagAdministrador;
    }

    public static TASUsuariosAdministradores fillDatos(Objeto objeto){
        TASUsuariosAdministradores usuariosAdministradores = new TASUsuariosAdministradores();
        if(objeto.string("estado").equals("SIN_RESULTADOS")){
            usuariosAdministradores.flagAdministrador = false;
        } else {
            usuariosAdministradores.numeroDocumento = objeto.objetos().get(0).string("NumeroDocumento", "");
            usuariosAdministradores.tipoDocumento = objeto.objetos().get(0).string("TipoDocumento", "");
            usuariosAdministradores.administradorId = objeto.objetos().get(0).string("AdministradorId", "");
            usuariosAdministradores.sucursalId = objeto.objetos().get(0).integer("SucursalId", 0);
            usuariosAdministradores.flagAdministrador = usuariosAdministradores.numeroDocumento != "" && usuariosAdministradores.tipoDocumento != "" ? true : false;
        }
        return usuariosAdministradores;
    }

    public static Objeto toObjeto(TASUsuariosAdministradores usuariosAdministradores){
        Objeto obj = new Objeto();
        obj.set("numeroDocumento", usuariosAdministradores.getNumeroDocumento());
        obj.set("tipoDocumento", usuariosAdministradores.getTipoDocumento());
        obj.set("administradorId", usuariosAdministradores.getAdministradorId());
        obj.set("sucursalId", usuariosAdministradores.getSucursalId());
        obj.set("flagAdministrador", usuariosAdministradores.isFlagAdministrador());

        return obj;
    }
}
