package ar.com.hipotecario.backend.servicio.api.cheques;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

import java.util.List;

public class EntidadesMercadoOB extends ApiObjetos<EntidadesMercadoOB.result> {
    public result result;
    public class result extends ApiObjeto {
        public respuesta respuesta;
        public List<infraestructurasMercado> infraestructurasMercado;
    }

    public class respuesta extends ApiObjeto {
        public String codigo;
        public String descripcion;
    }

    public class infraestructurasMercado extends ApiObjeto {
        public String documento_tipo;
        public String documento;
        public String nombre;
    }

    public static EntidadesMercadoOB get(ContextoOB contexto){
        ApiRequest request = new ApiRequest("Listado entidades mercado","cheques","GET","/v1/cheque/imf",contexto);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(EntidadesMercadoOB.class);
    }
}
