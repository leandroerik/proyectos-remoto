package ar.com.hipotecario.backend.servicio.api.empresas;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

import java.text.SimpleDateFormat;
import java.util.Date;

public class StopPaymentPapOB extends ApiObjeto {

    public respuestaStopPaymentOP respuestaStopPaymentOP;
    public static class respuestaStopPaymentOP extends ApiObjeto{
        public int nroOrden;
        public int subNroOrden;
        public String stopAplicado;
    }

    public static StopPaymentPapOB post(ContextoOB contexto, int nroOrden, int subNroOrden){
        ApiRequest apiRequest = new ApiRequest("ApiEmpresas - Stop Payment","empresas","POST","/v1/sat/suspensionOP",contexto);

        apiRequest.body("codigoEntidad",44);
        SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String dateString = timestamp.format(new Date());
        apiRequest.body("fechaHoraConsulta", dateString);
        apiRequest.body("frontEnd", 2);
        apiRequest.body("frontEndID", "OB");
        apiRequest.body("id", contexto.sesion().idCobis);
        apiRequest.body("idUsrLog", contexto.sesion().idCobis);
        apiRequest.body("tipoID", 1);
        apiRequest.body("nroOrden",nroOrden);
        apiRequest.body("subNroOrden",subNroOrden);

        ApiResponse apiResponse = apiRequest.ejecutar();
        StopPaymentPapOB stopPaymentPapOB = apiResponse.crear(StopPaymentPapOB.class);
        ApiException.throwIf(!apiResponse.http(200, 204, 404), apiRequest, apiResponse);

        return stopPaymentPapOB;


    }
}
