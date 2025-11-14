package ar.com.hipotecario.backend.servicio.api.tarjetascredito;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.tarjetascredito.TarjetasCreditoBB.TarjetaCreditoBB;

public class TarjetasCreditoBB extends ApiObjetos<TarjetaCreditoBB> {

    /* ========== ATRIBUTOS ========== */
    public static class TarjetaCreditoBB extends ApiObjeto {
        public boolean esPagableUS;
        public String altaPuntoVenta;
        public int idPaquete;
        public boolean muestraPaquete;
        public int sucursal;
        public String descSucursal;
        public String estado;
        public String descEstado;
        public String fechaAlta;
        public String tipoTitularidad;
        public String descTipoTitularidad;
        public String numero;
        public String cuenta;
        public String tipoTarjeta;
        public String descTipoTarjeta;
        public String formaPago;
        public String denominacionTarjeta;
        public String modeloLiquidacion;
        public String descModeloLiquidacion;
    }

    /* ========== SERVICIOS ========== */
    public static TarjetasCreditoBB get(Contexto contexto, String idCobis) {
        ApiRequest request = new ApiRequest("GetTarjetasCredito", ApiTarjetasCreditoBB.API, "GET", "/v2/tarjetascredito", contexto);
        request.query("adicionales", true);
        request.query("cancelados", false);
        request.query("tipoestado", "vigente");
        request.query("idcliente", idCobis);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        return response.crear(TarjetasCreditoBB.class);
    }


    /* ========== TEST ========== */
    public static void main(String[] args) {
        Contexto contexto = contexto("BB", "desarrollo");
        TarjetasCreditoBB tarjetasCredito = get(contexto, "4040");
        imprimirResultado(contexto, tarjetasCredito);
    }

}