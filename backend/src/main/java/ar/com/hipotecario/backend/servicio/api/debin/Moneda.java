package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class Moneda extends ApiObjeto {
    public String descripcion;
    public String id;
    public String signo;

    public Moneda(String id, String descripcion, String signo) {
        this.id = id;
        this.descripcion = descripcion;
        this.signo = signo;
    }
}
