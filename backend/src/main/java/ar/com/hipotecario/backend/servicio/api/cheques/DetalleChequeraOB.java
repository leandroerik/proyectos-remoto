package ar.com.hipotecario.backend.servicio.api.cheques;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

import java.util.List;

public class DetalleChequeraOB extends ApiObjetos<DetalleChequeraOB.DetalleChequera> {

    public static class DetalleChequera extends ApiObjeto{
        public String TIPO_CHEQUERA;
        public String ID_RUBRO;
        public String DESCRIPCION_CHEQUERA;
        public String COSTO;
    }

    public static DetalleChequeraOB get(ContextoOB contexto,String nroCuenta,String operacion){
        ApiRequest request = new ApiRequest("API-Cheques_ConsultaOpcionChequera_Echeq","cheques","GET","/v1/chequera",contexto);
        request.query("cta_banco",nroCuenta);
        request.query("ente",contexto.sesion().empresaOB.idCobis);
        request.query("operacion",operacion);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200,204,404), request, response);
        return response.crear(DetalleChequeraOB.class);
    }
}
