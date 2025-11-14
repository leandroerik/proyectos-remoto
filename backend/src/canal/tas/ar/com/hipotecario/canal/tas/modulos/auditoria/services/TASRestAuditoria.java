package ar.com.hipotecario.canal.tas.modulos.auditoria.services;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.TASAuditoria;
import ar.com.hipotecario.canal.tas.shared.modulos.kiosco.modelos.TASKiosco;

public class TASRestAuditoria {

    public static Objeto postRegistroCierreLote(ContextoTAS contexto,TASKiosco kiosco, TASAuditoria adminAuditoria, String fechaAuditoria, String tipoCierre){
        try {
            ApiRequest request = new ApiRequest("RegistroCierreLote", "tas", "POST", "/v1/cierre-lote", contexto);
            /**
             oficina:"15"
             tas:"206"
             lote:2003
             tipoCierre:"T"
            precinto:"01035418-00000001"
             */
            request.body("fecha", fechaAuditoria);
            request.body("tas", adminAuditoria.getKioscoId());
            request.body("oficina", kiosco.getSucursalId());
            request.body("tipoCierre", tipoCierre.equalsIgnoreCase("diario") ? "T" : "P");
            request.body("lote", Integer.valueOf(adminAuditoria.getLote()));
            request.body("precinto", adminAuditoria.getPrecinto1() + "-" + adminAuditoria.getPrecinto2());
            request.cache = false;
            LogTAS.loguearRequest(contexto, request, "REQUEST_REGISTRO_CIERRE_LOTE");
            ApiResponse response = request.ejecutar();
            LogTAS.loguearResponse(contexto, response, "RESPONSE_REGISTRO_CIERRE_LOTE");
            ApiException.throwIf(!response.http(200,201,202), request, response);
            Objeto respuesta = new Objeto();
            respuesta.set("estado", "OK");
            respuesta.set("respuesta", response);
            return respuesta;
        } catch (Exception e) {
            Objeto error = new Objeto();
            error.set("estado","ERROR");
            error.set("error", e);
            return error;
        }
    }

}
