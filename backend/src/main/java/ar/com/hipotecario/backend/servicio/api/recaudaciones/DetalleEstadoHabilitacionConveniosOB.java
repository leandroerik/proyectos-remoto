package ar.com.hipotecario.backend.servicio.api.recaudaciones;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

import java.util.List;

public class DetalleEstadoHabilitacionConveniosOB extends ApiObjetos<DetalleEstadoHabilitacionConveniosOB.DetalleEstadoHabilitacionConvenios> {

    public List<DetalleEstadoHabilitacionConvenios> convenios;

    public static class DetalleEstadoHabilitacionConvenios extends ApiObjeto {
        public int convenio;
        public String descripcion;
        public String habEcheq;
        public String habTransf;
        public String habDebin;
        public String nroCuenta;
        public String cbu;
        public String grupo;
        public String idServicio;
    }

    public static DetalleEstadoHabilitacionConveniosOB get(ContextoOB contexto, int convenio, String medioRecaudacion) {
        ApiRequest request = new ApiRequest("ConsultaConvenioHabilitado", "recaudaciones", "GET", "/v1/convenios/recaudacion/habilitacion", contexto);
        request.query("convenio", String.valueOf(convenio));
        request.query("empresa", contexto.sesion().empresaOB.idCobis);
        request.query("medioRecaudacion", medioRecaudacion);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);

        return response.crear(DetalleEstadoHabilitacionConveniosOB.class);
    }
    
    public static DetalleEstadoHabilitacionConveniosOB getTodosConvenios(ContextoOB contexto, String medioRecaudacion) {
        ApiRequest request = new ApiRequest("ConsultaConvenioHabilitado", "recaudaciones", "GET", "/v1/convenios/recaudacion/habilitacion", contexto);
        request.query("empresa", contexto.sesion().empresaOB.idCobis);
        request.query("medioRecaudacion", medioRecaudacion);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);

        return response.crear(DetalleEstadoHabilitacionConveniosOB.class);
    }
}
