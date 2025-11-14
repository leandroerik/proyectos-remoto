package ar.com.hipotecario.backend.servicio.api.tarjetasCredito;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

public class StopDebit extends ApiObjeto {

    public String endtime;
    public String retcode;
    public String retdescription;
    public String startTime;

    public static StopDebit postStopDebit(ContextoOB contexto, String numeroCuenta, String idcuenta) {
        ApiRequest request = new ApiRequest("stopdebit", "tarjetascredito", "POST", "/v1/cuentas/{idcuenta}/stopdebit", contexto);
        request.path("idcuenta", idcuenta);
        request.body("cuenta", numeroCuenta);
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        StopDebit stopDebit = response.crear(StopDebit.class);


        return stopDebit;

    }
}
