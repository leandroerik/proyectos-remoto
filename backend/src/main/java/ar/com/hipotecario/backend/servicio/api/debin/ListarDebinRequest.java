package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class ListarDebinRequest extends ApiObjeto {
    public Comprador comprador;
    public Debin debin;
    public Listado listado;
    public String tipo;
    public Vendedor vendedor;

    public ListarDebinRequest(Comprador comprador, Debin debin, Listado listado, String tipo, Vendedor vendedor) {
        this.comprador = comprador;
        this.debin = debin;
        this.listado = listado;
        this.tipo = tipo;
        this.vendedor = vendedor;
    }
}


