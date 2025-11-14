package ar.com.hipotecario.backend.servicio.api.personas;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

import ar.com.hipotecario.backend.servicio.api.personas.Documentos.*;
public class Documentos extends ApiObjetos<Documento>{
    public static String GET_PERSONA = "Personas";
    public static class Documento extends ApiObjeto {
        public String idCliente;
        public String tipoDocumento;
        public String numeroDocumento;

        public String numeroIdentificacionTributaria;
        public String apellido;

        public String nombre;

        public String sexo;

        public String fechaNacimiento;
        public String tipoPersona;
    }
    public static Documentos get(Contexto contexto, String documento) {
        ApiRequest request = new ApiRequest(GET_PERSONA, ApiPersonas.API, "GET", "/personas", contexto);
        request.query("nroDocumento", documento);
        request.cache = true;
        ApiResponse response = request.ejecutar();
        ApiException.throwIf("PERSONA_NO_ENCONTRADA", response.contains("no fue encontrada en bup o core"), request, response);
        ApiException.throwIf("NO EXISTE CLIENTE", response.contains("no fue encontrada en bup o core"), request, response);        
        ApiException.throwIf(!response.http(200), request, response);
        return response.crear(Documentos.class);
    }
}