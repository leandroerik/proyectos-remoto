package ar.com.hipotecario.canal.rewards.modulos.rechazos.model;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class SolicitudRechazos extends ApiObjeto {
    private String programa;
    private String entidad;
    private String novedad;
    private String estado;
    private String fechaDesde;
    private String fechaHasta;
    private String tipo;
    private String idRechazo;
    private String transaccion;

    public SolicitudRechazos() {}

    public SolicitudRechazos( String programa, String entidad, String novedad, String estado,
                           String fechaDesde, String fechaHasta, String tipo, String idRechazo, String transaccion) {
        this.programa = programa;
        this.entidad = entidad;
        this.novedad = novedad;
        this.estado = estado;
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
        this.tipo = tipo;
        this.idRechazo = idRechazo;
        this.transaccion = transaccion;
    }

    public String getPrograma() {
        return programa;
    }

    public void setPrograma(String programa) {
        this.programa = programa;
    }

    public String getEntidad() {
        return entidad;
    }

    public void setEntidad(String entidad) {
        this.entidad = entidad;
    }

    public String getNovedad() {
        return novedad;
    }

    public void setNovedad(String novedad) {
        this.novedad = novedad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getIdRechazo() {
        return idRechazo;
    }

    public void setIdRechazo(String idRechazo) {
        this.idRechazo = idRechazo;
    }

    public String getTransaccion() {
        return transaccion;
    }

    public void setTransaccion(String transaccion) {
        this.transaccion = transaccion;
    }

    /*@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if (programa != null && !programa.isEmpty()) sb.append("programa=").append(programa).append(",");
        if (entidad != null && !entidad.isEmpty()) sb.append("entidad=").append(entidad).append(",");
        if (novedad != null && !novedad.isEmpty()) sb.append("novedad=").append(novedad).append(",");
        if (estado != null && !estado.isEmpty()) sb.append("estado=").append(estado).append(",");
        if (fechaDesde != null && !fechaDesde.isEmpty()) sb.append("fechaDesde=").append(fechaDesde).append(",");
        if (fechaHasta != null && !fechaHasta.isEmpty()) sb.append("fechaHasta=").append(fechaHasta).append(",");
        if (tipo != null && !tipo.isEmpty()) sb.append("tipo=").append(tipo).append(",");
        if (idRechazo != null && !idRechazo.isEmpty()) sb.append("idRechazo=").append(idRechazo).append(",");
        if (transaccion != null && !transaccion.isEmpty()) sb.append("transaccion=").append(transaccion).append(",");

        // Eliminar la última coma y espacio si hay contenido
        if (sb.lastIndexOf(", ") == sb.length() - 2) {
            sb.delete(sb.length() - 2, sb.length());
        }

        return sb.toString();
    }**/
}
