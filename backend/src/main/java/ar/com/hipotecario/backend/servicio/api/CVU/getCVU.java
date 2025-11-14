package ar.com.hipotecario.backend.servicio.api.CVU;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

public class getCVU extends ApiObjeto {
    public cvu cvu;
    public respuesta respuesta;
    public static class cvu extends ApiObjeto{
        public int psp_id;
        public String cbu_cta;
        public String cvu;
        public String tipo;
        public String cuit;
        public String titular;
        public String moneda;
        public String persona_tipo;
        public String nombre_fantasia;
        public String fecha_alta;
        public estado estado;
    }
    public static class estado extends ApiObjeto{
        public String codigo;
        public String descripcion;
    }
    public static class respuesta extends ApiObjeto{
        public String codigo;
        public String descripcion;
    }
    public static getCVU get(ContextoOB contexto,String cvu){
        ApiRequest request = new ApiRequest("CuentaCoelsa", "cvu", "GET", "/v1/cvu/{cvu}", contexto);
        request.path("cvu", cvu);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204, 404), request, response);
        return response.crear(getCVU.class);
    }
}
