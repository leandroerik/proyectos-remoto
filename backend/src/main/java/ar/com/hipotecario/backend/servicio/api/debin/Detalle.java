package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class Detalle extends ApiObjeto {
    public String concepto;
    public String descripcion;
    public String devolucion;
    public String fecha;
    public String fechaExpiracion;
    public String idComprobante;
    public String idOperacionOriginal;
    public String idUsuario;
    public String importe;
    public String mismoTitular;
    public Moneda moneda;
    public String motivo;
    public String ori_adicional;
    public String ori_terminal;
    public String ori_trx;
    public String tiempoExpiracion;
}
