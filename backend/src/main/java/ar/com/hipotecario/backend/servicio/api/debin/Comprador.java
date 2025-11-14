package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class Comprador extends ApiObjeto {
    public Cliente cliente;
    public Estado estado;

    public Comprador(Cliente cliente, Estado estado) {
        this.cliente = cliente;
        this.estado = estado;
    }

    public Comprador(Cliente cliente) {
        this.cliente = cliente;
    }

    public Comprador() {
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
}
