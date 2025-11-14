package ar.com.hipotecario.backend.servicio.api.CVU;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

public class AliasCVU extends ApiObjeto {
    public respuesta respuesta;
    public cuenta cuenta;
    public titular titular;
    public alias alias;
    public static class respuesta extends ApiObjeto{
        public String descripcion;
        public String numero;
    }
    public static class cuenta extends ApiObjeto{
        public String tipo_cta;
        public String nro_cbu;
        public String nro_bco;
        public boolean cta_activa;
    }
    public static class titular extends ApiObjeto{
        public String tipo_persona;
        public String cuit;
        public String nombre;
    }
    public static class alias extends ApiObjeto{
        public String valor;
        public String valor_original;
    }
    public String transac;


    public static AliasCVU get(ContextoOB contexto, String cvu){
        ApiRequest request = new ApiRequest("Consulta-Alias","cvu","GET","/v1/alias",contexto);
        request.query("cbu",cvu);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200,204,404),request,response);
        return response.crear(AliasCVU.class);
    }
}
