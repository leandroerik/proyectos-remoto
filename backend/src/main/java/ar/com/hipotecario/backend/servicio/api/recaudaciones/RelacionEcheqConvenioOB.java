package ar.com.hipotecario.backend.servicio.api.recaudaciones;


import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.RelacionEcheqConvenioOB.RelacionEcheqConvenio;
import ar.com.hipotecario.canal.officebanking.ContextoOB;


public class RelacionEcheqConvenioOB extends ApiObjetos<RelacionEcheqConvenio> {

    public static class RelacionEcheqConvenio extends ApiObjeto {
    	String idEcheq;
        int codConv;
        String formaPago;
        Long cuitCliente;
        String nomLar;
    }

    public static RelacionEcheqConvenioOB post(ContextoOB contexto, int convenio, String idEcheq, String formaPago, Long cuitCliente, String nomLar) {
        ApiRequest request = new ApiRequest("PersistirRelacionEcheqConvenio", "recaudaciones", "POST", "/v1/convenios/echeqs/depositocustodia", contexto);
        
        request.body("idEcheq", idEcheq);
        request.body("codigoConvenio", convenio);
        request.body("formaPago", formaPago);
        request.body("cuitCliente", cuitCliente);
        request.body("nombreCliente", nomLar);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);

        return response.crear(RelacionEcheqConvenioOB.class);
    }

}
