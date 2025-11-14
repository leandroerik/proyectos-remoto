package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

import java.util.List;

public class Vendedor extends ApiObjeto {
    public String cbu;
    public Cliente cliente;
    public String codigo;
    public Contacto contacto;
    public List<Cuenta> cuenta;
    public String cuit;
    public String loteId;
    public String nombre_fantasia;
    public String rubro;
    public String sucursal;
    public String terminal;
    public String titular;
}
