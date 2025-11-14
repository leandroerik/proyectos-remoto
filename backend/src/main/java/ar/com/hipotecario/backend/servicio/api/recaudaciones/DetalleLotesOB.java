package ar.com.hipotecario.backend.servicio.api.recaudaciones;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.DetalleLotesOB.DetallePlanSueldo;

public class DetalleLotesOB extends ApiObjetos<DetallePlanSueldo> {

    /* ========== ATRIBUTOS ========== */

    public static class DetallePlanSueldo extends ApiObjeto {

        public String numeroCuenta;
        public String estado;
        public double importe;
        public String descripcion;
        public String secuencial;
        public String cuil;
        public String codigoServicio;

    }

    /* ========== SERVICIOS ========== */

    public static DetalleLotesOB get(Contexto contexto, String lote, String convenio,String secuencial,Integer idProceso) {
        ApiRequest request = new ApiRequest("LinkGetDetalleLotes", "recaudaciones", "GET", "/v1/plansueldo/detalle", contexto);
        request.query("convenio", convenio);
        request.query("lote", lote);
        request.query("interfaz","");
        request.query("secuencial", Integer.valueOf(secuencial));
        request.body("idProceso", idProceso);
        request.header("x-idProceso", idProceso.toString());
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204, 404), request, response);

        return response.crear(DetalleLotesOB.class);

    }
    public static DetalleLotesOB get(Contexto contexto, String lote, String convenio) {
        ApiRequest request = new ApiRequest("LinkGetDetalleLotes", "recaudaciones", "GET", "/v1/plansueldo/detalle", contexto);
        request.query("lote", lote);
        request.query("convenio", convenio);
        request.query("secuencial", 0);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204, 404), request, response);

        return response.crear(DetalleLotesOB.class);

    }



}
