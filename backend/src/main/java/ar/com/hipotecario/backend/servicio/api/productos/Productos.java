package ar.com.hipotecario.backend.servicio.api.productos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.productos.Productos.Producto;

public class Productos extends ApiObjetos<Producto> {

    public static String PRODUCTO_VIGENTE = "V";
    public static String PRODUCTO_ACTIVO = "A";
    public static String PRODUCTO_ADICIONAL = "ADICIONAL";
    public static String PRODUCTO_CANCELADO = "C";
    public static String TIPO_TARJETA_CREDITO = "ATC";
    public static String TIPO_TARJETA_DEBITO = "ATM";
    public static String TIPO_CAJA_AHORRO = "AHO";
    public static String TIPO_CUENTA_CORRIENTE = "CTE";
    public static String TIPO_PRESTAMO_HIPOTECARIO = "CCA";
    public static String TIPO_PLAZO_FIJO = "PFI";
    public static String TIPO_CAJA_SEGURIDAD = "CSG";
    public static String TIPO_PRESTAMO_PERSONAL = "NSP";
    public static String TIPO_UNITRADE = "UNI";
    public static String TIPO_PAQUETE = "PAQ";
    public static String ID_MONEDA_PESOS = "80";
    public static String ID_MONEDA_USD = "2";
    public static String CATEGORIA_CGU = "CGU";

    public static String TARJETA_NORMAL = "20";
    public static String TARJETA_CON_PROBLEMAS = "25";
    public static String TARJETA_DADA_DE_BAJA = "29";

    /* =========== ATRIBUTOS =========== */
    public static class Producto extends ApiObjeto {
        public String id;
        public String adicionales;
        public String categoria;
        public Boolean esTitular;
        public String tipoTarjeta;
        public Estado estado;
        public Long paquete;
        public Boolean contratoTransferencia;
        public Boolean cuentaCobro;
        public String numero;
        public String tipo;
        public Fecha fechaAlta;
        public Rol rol;
        public Sucursal sucursal;
        public Moneda moneda;

        public static class Estado extends ApiObjeto {
            public String codigo;
            public String descripcion;
        }

        public static class Rol extends ApiObjeto {
            public String tipo;
            public String descripcion;
        }

        public static class Sucursal extends ApiObjeto {
            public String codigo;
            public String descripcion;
        }

        public static class Moneda extends ApiObjeto {
            public String id;
            public String descripcion;
        }

        /* ========== METODOS ========== */
        public Boolean esTitular() {
            return esTitular != null && esTitular;
        }

        public Boolean esAdicional() {

            Boolean adicional = !empty(rol) && PRODUCTO_ADICIONAL.equals(rol.descripcion);
            return adicional;

        }

        public boolean esCajaAhorroPesos() {
            return TIPO_CAJA_AHORRO.equals(tipo) && ID_MONEDA_PESOS.equals(moneda.id);
        }

        public boolean esCajaAhorroUsd() {
            return TIPO_CAJA_AHORRO.equals(tipo) && ID_MONEDA_USD.equals(moneda.id);
        }

        public Boolean productoActivo() {
            if (estado == null || estado.codigo == null)
                return false;

            Boolean esActivo = !PRODUCTO_CANCELADO.equals(estado.codigo);
            esActivo &= PRODUCTO_ACTIVO.equals(estado.codigo) || PRODUCTO_VIGENTE.equals(estado.codigo) || TARJETA_NORMAL.equals(estado.codigo);

            return esActivo;
        }

        public Boolean tipoProductoActivo(String tipoProducto) {

            if (!productoActivo()) {
                return false;
            }

            Boolean esActivo = tipoProducto.equals(tipo);
            esActivo &= esTitular();
            esActivo &= !TARJETA_DADA_DE_BAJA.equals(estado.codigo);

            return esActivo;
        }

        public Boolean tipoProductoReciente(String tipoProducto) {

            Boolean esActivo = tipoProducto.equals(tipo);
            esActivo &= esTitular();

            if (esActivo && fechaAlta != null) {

                Fecha fechaDesde = Fecha.ahora().restarMeses(3);
                if (fechaDesde.esAnterior(fechaAlta)) {
                    return true;
                }
            }

            return false;
        }

        public Boolean cajaPesosActiva() {
            return esCajaAhorroPesos() && tipoProductoActivo(TIPO_CAJA_AHORRO);
        }

        public Boolean cajaUsdActiva() {
            return esCajaAhorroUsd() && tipoProductoActivo(TIPO_CAJA_AHORRO);
        }

        public boolean esCGU() {
            return CATEGORIA_CGU.equals(categoria);
        }

    }

    public Producto buscarCajaPesosActiva() {

        for (Producto producto : this) {

            if (producto.tipoProductoActivo(TIPO_CAJA_AHORRO) && producto.esCajaAhorroPesos()) {
                return producto;
            }
        }

        return null;
    }

    public Producto buscarCajaUsdActiva() {

        for (Producto producto : this) {

            if (producto.tipoProductoActivo(TIPO_CAJA_AHORRO) && producto.esCajaAhorroUsd()) {
                return producto;
            }
        }

        return null;
    }

    public Producto buscarNumero(String numero) {

        for (Producto producto : this) {

            if (producto.numero.equals(numero)) {
                return producto;
            }
        }

        return null;
    }

    public static Objeto getDatosProducto(Producto producto) {
        Objeto respuesta = new Objeto();
        respuesta.set("tipo", producto.tipo);
        respuesta.set("rol", producto.rol.descripcion);
        respuesta.set("estado", producto.estado.descripcion);
        if (!empty(producto.moneda))
            respuesta.set("moneda", producto.moneda.descripcion);

        return respuesta;
    }

    /* =========== SERVICIOS ============ */
    // API-Productos_Listado
    static Productos get(Contexto contexto, String idCobis, Boolean cache) {
        ApiRequest request = new ApiRequest("Productos", "productos", "GET", "/v1/productos", contexto);
        request.query("idCliente", idCobis);
        request.cache = cache;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(Productos.class);
    }

    static Productos getVigentes(Contexto contexto, String idCobis) {
        ApiRequest request = new ApiRequest("Productos", "productos", "GET", "/v1/productos", contexto);
        request.query("idCliente", idCobis);
        request.query("vigente", true);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(Productos.class);
    }

    /* ========== TEST ========== */
    public static void main(String[] args) {
        Contexto contexto = contexto("BB", "homologacion");
        Productos datos = get(contexto, "7333941", true);
        imprimirResultado(contexto, datos);
    }

}
