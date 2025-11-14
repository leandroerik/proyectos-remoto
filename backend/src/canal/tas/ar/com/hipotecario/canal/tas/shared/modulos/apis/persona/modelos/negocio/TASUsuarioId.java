package ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio;

import ar.com.hipotecario.backend.base.Objeto;

public class TASUsuarioId extends Objeto {
    private String codigoCliente;
    private String tipoCliente;
    private String numeroDocumento;
    private String tipoDocumento;
    private String sexo;
    private String secuencialId;
    private String sucursalCliente;
    private String sucursalTAS;
    private String idTAS;
    private String daysUnitlExpiration = "365";

    public TASUsuarioId() {
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public String getTipoCliente() {
        return tipoCliente;
    }

    public void setTipoCliente(String tipoCliente) {
        this.tipoCliente = tipoCliente;
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

    public String getSexo() {
        return sexo;
    }

    public void setSexo(String sexo) {
        this.sexo = sexo;
    }

    public String getSecuencialId() {
        return secuencialId;
    }

    public void setSecuencialId(String secuencialId) {
        this.secuencialId = secuencialId;
    }

    public String getSucursalCliente() {
        return sucursalCliente;
    }

    public void setSucursalCliente(String sucursalCliente) {
        this.sucursalCliente = sucursalCliente;
    }

    public String getSucursalTAS() {
        return sucursalTAS;
    }

    public void setSucursalTAS(String sucursalTAS) {
        this.sucursalTAS = sucursalTAS;
    }

    public String getIdTAS() {
        return idTAS;
    }

    public void setIdTAS(String idTAS) {
        this.idTAS = idTAS;
    }

    public String getDaysUnitlExpiration() {
        return daysUnitlExpiration;
    }

    public void setDaysUnitlExpiration(String daysUnitlExpiration) {
        this.daysUnitlExpiration = daysUnitlExpiration;
    }

    public TASUsuarioId fillDatos(String usuarioId){
        TASUsuarioId usuario = new TASUsuarioId();
        String exc = "";
        exc = usuarioId.indexOf("codigoCliente") == -1 ? exc + "codigoCliente nulo - " : "";
        exc = usuarioId.indexOf("tipoCliente") == -1 ? exc + "tipoCliente nulo" : "";
        exc = usuarioId.indexOf("idTAS") == -1 ? exc + "TAS ID nulo" : "";
        if("".equals(exc)){
            usuario.codigoCliente = parseUsuarioId(usuarioId, "codigoCliente");
            usuario.tipoCliente = parseUsuarioId(usuarioId, "tipoCliente");
            usuario.numeroDocumento = usuarioId.indexOf("numeroDocumento") != -1 ? parseUsuarioId(usuarioId, "numeroDocumento") : "";
            usuario.tipoDocumento = usuarioId.indexOf("tipoDocumento") != -1 ? parseUsuarioId(usuarioId, "tipoDocumento") : "";
            usuario.sexo = usuarioId.indexOf("sexo") != -1 ? parseUsuarioId(usuarioId, "sexo") : "";
            usuario.secuencialId = usuarioId.indexOf("secuencialId") != -1 ? parseUsuarioId(usuarioId, "secuencialId") : "";
            usuario.sucursalCliente = usuarioId.indexOf("sucursalCliente") != -1 ? parseUsuarioId(usuarioId, "sucursalCliente") : "";
            usuario.sucursalTAS = usuarioId.indexOf("sucursalTAS") != -1 ? parseUsuarioId(usuarioId, "SucursalTAS") : "";
            usuario.idTAS = usuarioId.indexOf("idTAS") != -1 ? parseUsuarioId(usuarioId, "idTAS") : "";
            usuario.daysUnitlExpiration = usuarioId.indexOf("daysUnitlExpiration") != -1 ? parseUsuarioId(usuarioId, "daysUnitlExpiration") : "";
        }

        return usuario;
    }
    public String parseUsuarioId(String cuentaId, String campo){
        String cuenta = cuentaId;
        int pos = cuentaId.indexOf(campo)+campo.length()+1;
        String sub = cuentaId.substring(pos, cuentaId.length());
        int fin = pos+sub.indexOf("|");
        return cuenta.substring(pos, fin);
    }
}
