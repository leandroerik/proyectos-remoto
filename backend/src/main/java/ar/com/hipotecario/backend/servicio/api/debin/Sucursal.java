package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class Sucursal extends ApiObjeto {
    public Integer codigo;
    public String descripcion;
    public String id;
    public String terminal;
    
    public Sucursal(String id, String descripcion) {
        this.id = id;
        this.descripcion = descripcion;
    }
}
