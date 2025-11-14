package ar.com.hipotecario.backend.servicio.api.cheques;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

public class DetalleRazonSocialOB extends ApiObjetos<DetalleRazonSocialOB.Result> {

    public Result result;
    public static class Respuesta extends ApiObjeto{
        public String codigo;
        public String descripcion;
    }

    public static class Result extends ApiObjeto{
        public Respuesta respuesta;
        public String beneficiario_documento_tipo;
        public String beneficiario_documento;
        public String beneficiario_razon_social;
    }

    public static DetalleRazonSocialOB get(ContextoOB contexto, String cuit){
        ApiRequest request = new ApiRequest("Cheques","cheques","GET","/v1/cheque/bancarizado/{tipo_documento}/{documento}",contexto);
        request.path("tipo_documento","cuit");
        request.path("documento",cuit);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(DetalleRazonSocialOB.class);
    }
}
