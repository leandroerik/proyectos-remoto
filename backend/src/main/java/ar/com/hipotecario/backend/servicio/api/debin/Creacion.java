package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class Creacion extends ApiObjeto {
    public String fechaDesde;
    public String fechaHasta;
    public String fecha_desde;
    public String fecha_hasta;

    public Creacion(String fechaDesde, String fechaHasta) {
        this.fechaDesde = fechaDesde;
        this.fechaHasta = fechaHasta;
    }
}
