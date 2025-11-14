package ar.com.hipotecario.backend.servicio.api.tarjetasdebito;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.tarjetasdebito.TarjetasDebitoBB.TarjetaDebitoBB;

public class TarjetasDebitoBB extends ApiObjetos<TarjetaDebitoBB> {

    /* ========== ATRIBUTOS ========== */
    public static class TarjetaDebitoBB extends ApiObjeto {
        public boolean muestraPaquete;
        public String tipoProducto;
        public String numeroProducto;
        public String idProducto;
        public int sucursal;
        public String descSucursal;
        public String estado;
        public String descEstado;
        public String fechaAlta;
        public int idDomicilio;
        public String tipoTitularidad;
        public boolean adicionales;
        public String moneda;
        public String monedaDesc;
        public String numeroTarjeta;
        public String estadoTarjeta;
        public String nroSolicitud;
        public double limiteExtraccionMonto;
        public String fechaVencimiento;
        public boolean activacionTemprana;
        public boolean virtual;
        public String pausada;
    }

    /* ========== SERVICIOS ========== */
    // API-TarjetasDebito_ConsultaConsolidadaTarjetaDeDebito
    public static TarjetasDebitoBB get(Contexto contexto, String idcliente) {
        ApiRequest request = new ApiRequest("TarjetasDebito", ApiTarjetasDebitoBB.API, "GET", "/v1/tarjetasdebito", contexto);
        request.query("idcliente", idcliente);
        request.query("cancelados", false);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response.crear(TarjetasDebitoBB.class);
    }

    /* ========== TEST ========== */
    public static void main(String[] args) {
        Contexto contexto = contexto("BB", "desarrollo");
        TarjetasDebitoBB tarjetasDebito = get(contexto, "4040");
        imprimirResultado(contexto, tarjetasDebito);
    }
}
