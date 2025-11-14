package ar.com.hipotecario.backend.servicio.api.cuentas;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasBB.CuentaBB;

import java.time.LocalDate;
import java.util.Comparator;

public class CuentasBB extends ApiObjetos<CuentaBB> {

    /* ========== ATRIBUTOS ========== */
    public static class CuentaBB extends ApiObjeto {
        public String numeroProducto;
        public String cbu;
        public String descEstado;
        public String moneda;
        public String descMoneda;
        public double disponible;
        public String tipoTitularidad;
        public String descTipoTitularidad;
        public String idPaquete;
        public String categoria;
        public String sucursal;
        public String descSucursal;
        public boolean muestraPaquete;
        public String tipoProducto;
        public String idProducto;
        public String fechaAlta;
        public int idDomicilio;
        public String tipoOperacion;
        public boolean adicionales;
        public String estadoCuenta;
        public boolean esTransaccional;
        public String adelantoCuentaAsociada;
        public String estado;

        public String alias(Contexto contexto) {
            CuentaCoelsa cuentaCoelsa = ApiCuentas.cuentaCoelsa(contexto, cbu).tryGet();
            return cuentaCoelsa != null ? cuentaCoelsa.nuevoAlias : "";
        }
    }

    public CuentasBB getCuentas() {
        CuentasBB filtradas = new CuentasBB();
        for (CuentaBB c : this){
            if ("AHO".equals(c.tipoProducto) && "T".equals(c.tipoTitularidad) && "V".equals(c.estado)) {
                filtradas.add(c);
            }
        }
        return  filtradas;
    }

    public CuentaBB obtenerUltimaCajaDeAhorro(String moneda) {
        return this.stream()
                .filter(c -> "AHO".equals(c.tipoProducto) && moneda.equals(c.moneda) && "T".equals(c.tipoTitularidad) && "V".equals(c.estado))
                .max(Comparator.comparing(c -> LocalDate.parse(c.fechaAlta)))
                .orElse(null);
    }

    public static CuentasBB get(Contexto contexto, String idCobis){
        ApiRequest request = new ApiRequest("Cuentas", "cuentas", "GET", "/v1/cuentas", contexto);
        request.query("idcliente", idCobis);
        request.query("consultaalias", false);

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response.crear(CuentasBB.class);
    }

    /* ========== TEST ========== */
    public static void main(String[] args) {
        Contexto contexto = contexto("BB", "desarrollo");
        CuentasBB cuentas = get(contexto, "4040");
        //CuentaBB cuentaPesos = cuentas.obtenerUltimaCajaDeAhorro("80");
        //CuentaBB cuentaDolares = cuentas.obtenerUltimaCajaDeAhorro("2");
        imprimirResultado(contexto, cuentas);
    }

}
