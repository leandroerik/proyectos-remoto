package ar.com.hipotecario.canal.buhobank.api;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.buhobank.ContextoBB;
import ar.com.hipotecario.canal.buhobank.LogBB;

import ar.com.hipotecario.canal.homebanking.Respuesta;

public class BBSalesforce {

    public static Respuesta registrarEventoSalesforce(ContextoBB contexto, String eventDefinitionKey, Objeto data, String idCobis) {
        if (contexto.sesion().idCobis == null)
            return Respuesta.estado("SIN_PSEUDO_SESION");

        ApiRequest request = new ApiRequest("RegistrarEventoSalesforce", "notificaciones", "POST", "/v1/marketing/evento", contexto);
        request.body("ContactKey", idCobis);
        request.body("EventDefinitionKey", eventDefinitionKey);
        request.body("Data", data);

        LogBB.evento(contexto, "SALESFORCE_REQUEST", data);
        ApiResponse response = request.ejecutar();
        LogBB.evento(contexto, "SALESFORCE_RESPONSE", response);

        Respuesta respuesta = new Respuesta();
        respuesta.set("idProceso", request.idProceso());
        respuesta.set("idEvento", response.string("eventInstanceId"));
        respuesta.set("data", data);
        return respuesta;
    }

}

