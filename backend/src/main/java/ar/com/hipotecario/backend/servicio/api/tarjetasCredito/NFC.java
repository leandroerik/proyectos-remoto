package ar.com.hipotecario.backend.servicio.api.tarjetasCredito;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

import java.util.List;
import java.util.Map;

public class NFC extends ApiObjeto {

  // GET /v1/configuracionnotificacion
  public static NFC getConfiguracionNotificacionNFC(Contexto contexto) {
    ApiRequest request = new ApiRequest("ConfigNotificacionNFC", "tarjetascredito", "GET", "/v1/configuracionnotificacion", contexto);
    request.query("codigoBanco", "044");

    ApiResponse response = request.ejecutar();
    ApiException.throwIf(!response.http(200), request, response);
    return response.crear(NFC.class, response);
  }

  // POST /v1/configuracionnotificacion
  public static NFC postConfiguracionNotificacionNFC(Contexto contexto, List<Map<String, Object>> notificationMethods) {
    ApiRequest request = new ApiRequest("ConfigNotificacionNFC", "tarjetascredito", "POST", "/v1/configuracionnotificacion", contexto);
    request.body("notification_methods", notificationMethods);

    ApiResponse response = request.ejecutar();
    ApiException.throwIf(!response.http(200), request, response);
    return response.crear(NFC.class, response);
  }
}
