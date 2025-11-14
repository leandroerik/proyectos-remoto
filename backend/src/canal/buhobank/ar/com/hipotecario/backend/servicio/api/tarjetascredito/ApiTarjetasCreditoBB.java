package ar.com.hipotecario.backend.servicio.api.tarjetascredito;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.Api;

public class ApiTarjetasCreditoBB extends Api {
    public static String API = "tarjetascredito";

    public static Futuro<TarjetasCreditoBB> get(Contexto contexto, String idCobis) {
        return futuro(() -> TarjetasCreditoBB.get(contexto, idCobis));
    }
}
