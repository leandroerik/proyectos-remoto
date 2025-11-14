package ar.com.hipotecario.backend.servicio.api.tarjetasCredito;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;

public class ApiTarjetaCredito extends Api {
    /* ========== SERVICIOS ========== */

    public static Futuro<DetalleTarjetasCredito> tarjeta(Contexto contexto, String numeroTarjeta) {
        return futuro(() -> DetalleTarjetasCredito.get(contexto, numeroTarjeta));
    }
}
