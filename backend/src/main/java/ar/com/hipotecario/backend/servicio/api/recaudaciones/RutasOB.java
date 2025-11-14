package ar.com.hipotecario.backend.servicio.api.recaudaciones;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

public class RutasOB extends ApiObjeto {
    public String rutaEntrada;
    public String rutaListado;
    public String rutaSalida;


    // API-Recaudaciones_ConsultaRutasRecaudaciones
    public static RutasOB get(ContextoOB contexto, String convenio, String codigoGrupoRecaudacion, String servicioGrupoRecaudacion) {
        ApiRequest request = new ApiRequest("ConsultaRutasRecaudaciones", "recaudaciones", "GET", "/v1/convenio/{convenio}/rutas", contexto);
        request.path("convenio", convenio);

        if (codigoGrupoRecaudacion != null) request.query("codigo", codigoGrupoRecaudacion);
        if (servicioGrupoRecaudacion != null) request.query("servicio", servicioGrupoRecaudacion);

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);

        return  response.crear(RutasOB.class);
    }
}
