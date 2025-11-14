package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class TarjetaDebito extends ApiObjeto {
    public String banco;
    public String categoria;
    public String codigo;
    public Boolean contratoTransferencia;
    //public Object cuenta;
    public boolean cuentaCobro;
    public int cuentaSolidaria;
    public String descripcion;
    public Boolean esTitular;
    public Estado estado;
    public String fechaAlta;
    public String fechaBaja;
    public String grupo;
    public Integer id;
    public Moneda moneda;
    public String numero;
    public Integer oficial;
    public String paquete;
    public Sucursal sucursal;
    //public Object tarjetadebito;
    public String tipo;
    public String visualizar;
}
