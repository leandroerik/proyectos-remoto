package ar.com.hipotecario.backend.servicio.api.recaudaciones;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.DetalleConveniosHabilitacionOB.DetalleConveniosHabilitacion;
import ar.com.hipotecario.canal.officebanking.ContextoOB;


public class DetalleConveniosHabilitacionOB extends ApiObjetos<DetalleConveniosHabilitacion> {

    public static class DetalleConveniosHabilitacion extends ApiObjeto {
        public int empresa;
        public int convenio;
        public String echeq;
        public String transf;
        public String deblot;
        public String tipo_Consulta;
    }

    public static DetalleConveniosHabilitacion patch(ContextoOB contexto, int convenio, String echeq, String transf, String debin, String tipoConsulta) {
        ApiRequest request = new ApiRequest("HabilitarConvenioRecaudaciones", "recaudaciones", "PATCH", "/v1/convenios/{convenio}/recaudacion/habilitacion", contexto);
        request.body("convenio", String.valueOf(convenio));
        request.body("empresa", contexto.sesion().empresaOB.idCobis);
        request.body("habEcheq", echeq);
        request.body("habTransferencia", transf);
        request.body("habDebin", debin);
        request.body("tipoConsulta", tipoConsulta);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);

        return response.crear(DetalleConveniosHabilitacion.class);
    }

}
