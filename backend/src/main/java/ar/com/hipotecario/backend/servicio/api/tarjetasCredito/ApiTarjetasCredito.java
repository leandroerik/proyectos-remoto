package ar.com.hipotecario.backend.servicio.api.tarjetasCredito;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;

import java.util.List;
import java.util.Map;

public class ApiTarjetasCredito extends Api {
  // Esta clase es como "TarjetasCredito" pero estaría bueno que se unifique en esta clase
  // El tema es revisar si es posible que sean futuros las de la otra clase
  // Eso significa revisar quienes consumen las apis en todos los canales

  public static Futuro<NFC> getConfiguracionNotificacionNFC(Contexto contexto) {
    return futuro(() -> NFC.getConfiguracionNotificacionNFC(contexto));
  }

  public static Futuro<NFC> postConfiguracionNotificacionNFC(Contexto contexto, List<Map<String, Object>> notificationMethods) {
    return futuro(() -> NFC.postConfiguracionNotificacionNFC(contexto, notificationMethods));
  }
}
