package ar.com.hipotecario.backend.servicio.api.cheques;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

public class DetalleChequeraActivaOB extends ApiObjetos<DetalleChequeraActivaOB.DetalleChequeraActiva> {

    public static class DetalleChequeraActiva extends ApiObjeto {
        public String NUMERO_CHEQUERA;
        public String ESTADO;
        public String DES_ESTADO;
        public int CANT_CHEQUES;
        public int CANT_CHEQUES_DISPONIBLE;
        public String CHEQUE_INICIAL;
        public String CHEQUE_FINAL;
        public String FECHA_INICIAL;
        public String TIPO_CHEQUERA;
    }

    public static DetalleChequeraActivaOB get(ContextoOB contexto, String nroCuenta, String operacion){
        ApiRequest request = new ApiRequest("API-Cheques_ConsultaOpcionChequera_Echeq","cheques","GET","/v1/chequera",contexto);
        request.query("cta_banco",nroCuenta);
        request.query("ente",contexto.sesion().empresaOB.idCobis);
        request.query("operacion",operacion);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200,204,404), request, response);
        return response.crear(DetalleChequeraActivaOB.class);
    }
}
